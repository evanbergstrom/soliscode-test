#!/usr/bin/env bash
# Copyright 2025 Evan Bergstrom
# SPDX-License-Identifier: Apache-2.0

# Publishes a release's Javadoc to the project's GitLab Pages site, built on this machine: the release's site is
# built in a worktree of its tag, its aggregate Javadoc is stored in the project's generic package registry, and a
# pipeline whose one job, pages (.gitlab-ci.yml), unpacks every stored version into the site
# (docs/design/reference/documentation.md, section 3). CI builds nothing. A child that publishes has a copy of this
# file, which soliscode:docs installs from the tag of its parent version. It needs glab (logged in), zip, curl and
# python3 3.11 or later, and for the pipeline's job a project runner on this machine: gitlab-runner, registered for
# the project as <project>-<id>-pages-<host> with the tag soliscode-pages, and Docker running where the runner reaches
# it. The script runs that runner for the one job it queued (gitlab-runner run-single), so nothing runs in the
# background and GitLab's compute minutes are not used. --check prints what the project's settings would let it
# publish, and whether the runner is ready, and changes nothing. --store-only builds and stores the version, and
# starts no pipeline (for a project whose default branch has no .gitlab-ci.yml yet). The pipeline runs on the
# project's default branch, as GitLab gives it. --register-runner, once per machine and project, creates that runner in GitLab and
# registers it here (Docker executor, the job's image), printing no token.
#
# Usage: scripts/publish-javadoc.sh [--check | --store-only] <version>
#        scripts/publish-javadoc.sh --register-runner

set -euo pipefail

# The site build's goals and switches, after -B and -Drevision: no test runs, neither a module's own, nor one whose
# POM sets skipTests itself (which -DskipTests cannot reach, but skipExec does), nor the test phase the site's
# Surefire report forks (documentation.md, section 3.2, step 4). The child project publish-javadoc-build builds with
# the same line, which scripts/tests/test-publish-javadoc.sh checks.
site_build=(-DskipTests -Dmaven.test.skip.exec=true package site)

# The limits on reading the Pages site (#38), in seconds: each read's connect and total timeout, and the wait for
# this pipeline's deployment, retried every interval up to the limit in all; and how often the pipeline's status is
# read while it runs. Globals, not environment variables, so that the test, sourcing this file, cuts them and a
# publisher's shell cannot.
site_connect_timeout=10
site_max_time=30
site_poll_limit=120
site_poll_interval=10
pipeline_poll_interval=10

# How many of the newest stored versions the site keeps when the root POM sets no soliscode.javadoc.site.versions:
# the parent's own value, which the test checks against the parent's pom.xml (#41; documentation.md, section 3.8).
site_versions_default=3

die() { echo "publish-javadoc.sh: $*" >&2; exit 1; }
say() { echo "publish-javadoc.sh: $*"; }

# Why the project's settings refuse publishing, or nothing: a library's Javadoc is never readable more widely than
# its source. Arguments: the project's visibility, pages_access_level, package_registry_access_level and
# repository_access_level. Each of the source, the Pages site and the registry reaches everyone (3), logged-in users
# (2) or members (1); neither the site nor the registry may reach wider than the source, which a public project can
# keep members only (GitLab sets the repository's access apart from the project's visibility).
exposure_refusal() {
    local visibility=$1 pages=$2 registry=$3 repository=$4
    local level='^(disabled|private|enabled|public)$'
    # A value GitLab did not give, or one this check does not know, refuses: the check never passes blind.
    if [[ ! "$visibility" =~ ^(public|internal|private)$ ]]; then
        echo "GitLab gave no known visibility ('$visibility')"
    elif [[ ! "$pages" =~ $level || ! "$registry" =~ $level || ! "$repository" =~ $level ]]; then
        echo "GitLab gave no known Pages, package registry or repository access level" \
            "('$pages', '$registry', '$repository')"
    elif [[ "$repository" == disabled ]]; then
        echo "the repository is disabled for this project: there is no source to publish the Javadoc of"
    elif [[ "$registry" == disabled ]]; then
        echo "the package registry is disabled for this project: enable it, since the Javadoc is stored there"
    elif [[ "$pages" == disabled ]]; then
        echo "Pages is disabled for this project: enable it (Settings > General > Visibility) first"
    else
        local source site store
        source=$(reach "$visibility" "$repository")
        site=$(reach "$visibility" "$pages")
        store=$(reach "$visibility" "$registry")
        if (( site > source )); then
            echo "Pages is readable by $(reach_name "$site"), but the source by $(reach_name "$source")." \
                "Narrow Pages to match"
        elif (( store > source )); then
            echo "the package registry is readable by $(reach_name "$store"), but the source by" \
                "$(reach_name "$source"). Narrow the registry to match"
        fi
    fi
}

# How far a feature's access level reaches on a project of this visibility: 3 everyone, 2 logged-in users,
# 1 members. public is everyone; enabled is as far as the project's visibility; private is members.
reach() {
    case "$2" in
        public) echo 3 ;;
        private) echo 1 ;;
        *) case "$1" in public) echo 3 ;; internal) echo 2 ;; *) echo 1 ;; esac ;;
    esac
}

reach_name() {
    case "$1" in 3) echo everyone ;; 2) echo "logged-in users" ;; *) echo "members only" ;; esac
}

# Who can read the project's Pages site: everyone, or restricted (logged-in users or members, who an anonymous
# request is redirected to sign in for). Arguments: the project's visibility and pages_access_level; enabled means
# everyone only on a public project.
pages_audience() {
    if [[ "$2" == public || "$1" == public && "$2" == enabled ]]; then
        echo everyone
    else
        echo restricted
    fi
}

# Why the branch named $1 cannot carry the pipeline, or nothing: GitLab gave none (a project with no branch yet). Any
# name GitLab gives is passed on as it is, inside the JSON body (pipeline_body).
branch_refusal() {
    [[ -n "$1" ]] || echo "GitLab gave no default branch for this project: push one first"
}

# The JSON body that starts the pipeline on the branch $1 with the input versions, the list $2.
pipeline_body() {
    python3 -I -c 'import json, sys; print(json.dumps({"ref": sys.argv[1], "inputs": {"versions": sys.argv[2]}}))' \
        "$1" "$2"
}

# The JSON field of standard input's object, read with Python (no jq is assumed).
json_field() {
    python3 -I -c 'import json, sys; v = json.load(sys.stdin).get(sys.argv[1]); print("" if v is None else v)' "$1"
}

# The versions of the generic packages named exactly javadoc, sorted, one per line, from glab's paginated output
# (one JSON array per page, one after the other).
javadoc_versions() {
    python3 -I -c '
import json, re, sys
text, decoder, at, found = sys.stdin.read(), json.JSONDecoder(), 0, set()
while True:
    while at < len(text) and text[at].isspace():
        at += 1
    if at >= len(text):
        break
    page, at = decoder.raw_decode(text, at)
    for package in page:
        if package.get("name") == "javadoc" and re.fullmatch(r"[0-9]+(\.[0-9]+)*", package.get("version", "")):
            found.add(package["version"])
for version in sorted(found, key=lambda v: [int(part) for part in v.split(".")]):
    print(version)
'
}

# K, the number of the newest stored versions the site keeps, from the root POM $1, with where it came from: "<K> pom"
# or "<K> default", or "refused <why>". It reads the root POM's own <properties> with ElementTree, without Maven, in
# any namespace or none, the value with the whitespace around it removed; an element of that name anywhere else in
# the POM (a profile's <properties>) refuses, as does a value that is not a whole number from 1 to 10000, a bound far
# above any site GitLab would take, which keeps tail's offset in range (#41).
site_versions() {
    python3 -I -c '
import re, sys
import xml.etree.ElementTree as ET
name, default = "soliscode.javadoc.site.versions", sys.argv[2]
try:
    root = ET.parse(sys.argv[1]).getroot()
except (OSError, ET.ParseError) as e:
    print("refused the root POM %s cannot be read: %s" % (sys.argv[1], e))
    sys.exit(0)
local = lambda e: e.tag.rsplit("}", 1)[-1] if isinstance(e.tag, str) else ""
props = [p for p in root if local(p) == "properties"]
own = [e for p in props for e in p if local(e) == name]
others = [e for e in root.iter() if local(e) == name and not any(e is o for o in own)]
if others:
    print("refused %s is set in the root POM outside its <properties> (a profile, say), which this script does not read" % name)
elif len(own) > 1:
    print("refused %s is set twice in the root POM" % name)
elif not own:
    print(default + " default")
else:
    value = (own[0].text or "").strip()
    if re.fullmatch(r"[0-9]{1,5}", value) and 1 <= int(value) <= 10000:
        print("%d pom" % int(value))
    else:
        print("refused %s in the root POM is %r, not a whole number from 1 to 10000" % (name, value))
' "$1" "$site_versions_default"
}

# The id of the pages job of pipeline $2 in project $1, or nothing.
pages_job_id() {
    glab api "projects/$1/pipelines/$2/jobs" 2> /dev/null | python3 -I -c '
import json, sys
print(next((j["id"] for j in json.load(sys.stdin) if j.get("name") == "pages"), ""))' 2> /dev/null || true
}

# The pages-site.sh lines of job $2's trace in project $1, cleaned (#41): a raw trace holds ANSI escapes and section
# markers, may end lines with \r and may put a timestamp first, so a line counts where it holds "pages-site.sh: "
# anywhere, and is printed from there. The command's echo, "$ sh scripts/pages-site.sh", has no such marker. Returns 1
# if GitLab gives no trace.
job_lines() {
    local trace
    trace=$(glab api "projects/$1/jobs/$2/trace" 2> /dev/null) || return 1
    python3 -I -c '
import re, sys
text = re.sub(r"\x1b\[[0-9;]*[A-Za-z]", "", sys.stdin.read())
for line in re.split(r"[\r\n]+", text):
    at = line.find("pages-site.sh: ")
    if at >= 0:
        print(line[at:].rstrip())' <<< "$trace"
}

# The versions on standard input, one per line, sorted as javadoc_versions sorts them, by every part as a number, with
# blank lines and repeats dropped: the order the newest K is taken in, before the upload and after it (#41).
sort_versions() {
    python3 -I -c '
import sys
found = {line.strip() for line in sys.stdin if line.strip()}
for version in sorted(found, key=lambda v: [int(part) for part in v.split(".")]):
    print(version)
'
}

# The EXIT trap, set once at the start, whatever ends the script (a refusal, a failure, Ctrl-C): removes a build's
# worktree, and cancels a pipeline whose job has not finished, so that it does not block the next publish.
pending_project=""
pending_pipeline=""
on_exit() {
    remove_worktree
    if [[ -n "$pending_pipeline" ]]; then
        local job
        job=$(job_status "$pending_project" "$pending_pipeline")
        # At exit the script's runner has exited, so a running job is an orphan too.
        [[ ! "$job" =~ ^(created|waiting_for_resource|preparing|pending|running|unknown)$ ]] \
            || cancel_pipeline "$pending_project" "$pending_pipeline"
    fi
}

# Removes a build's temporary worktree, its entry under .git/worktrees/, and the work directory.
cleanup_root=""
cleanup_work=""
remove_worktree() {
    [[ -n "$cleanup_work" ]] || return 0
    git -C "$cleanup_root" worktree remove --force "$cleanup_work/src" > /dev/null 2>&1 || true
    git -C "$cleanup_root" worktree prune || true
    rm -rf "$cleanup_work"
}

# The name of this machine's runner for the project named $1 with the ID $2: <project>-<id>-pages-<host>. The ID
# keeps two projects of one name in different groups apart; the host is macOS's LocalHostName, which does not change
# with the network as hostname -s can.
runner_name() {
    local host
    host=$(scutil --get LocalHostName 2> /dev/null || hostname -s)
    echo "$1-$2-pages-$host"
}

# The status of the pages job of pipeline $2 in project $1, asked up to three times; "unknown" if GitLab does not
# answer, "missing" if the pipeline has no such job.
job_status() {
    local tries answer
    for tries in 1 2 3; do
        if answer=$(glab api "projects/$1/pipelines/$2/jobs" 2> /dev/null); then
            python3 -I -c '
import json, sys
print(next((j["status"] for j in json.load(sys.stdin) if j.get("name") == "pages"), "missing"))' <<< "$answer" \
                && return 0
        fi
        sleep 5
    done
    echo unknown
}

# What the config file $2 holds for the runner named $1, read with Python's tomllib (3.11 or later): "ok <docker
# host>" for one entry with a URL and a token, else "missing", "duplicate" or "unreadable <why>". It prints no
# token, and always exits 0, so that a caller under set -e gets the verdict. The Docker host is the entry's
# [runners.docker] host, else DOCKER_HOST, else the socket gitlab-runner uses
# by default, which is not where the Docker CLI's context may point.
runner_lookup() {
    [[ -e "$2" ]] || { echo missing; return 0; }
    [[ -r "$2" ]] || { echo "unreadable: $2 exists but cannot be read"; return 0; }
    python3 -I -c '
import os, sys
try:
    import tomllib
except ImportError:
    print("unreadable: reading the runner config needs Python 3.11 or later (tomllib)")
    sys.exit(0)
try:
    with open(sys.argv[1], "rb") as f:
        runners = [r for r in tomllib.load(f).get("runners", []) if r.get("name") == sys.argv[2]]
except (OSError, ValueError) as e:
    print("unreadable: %s" % e)
    sys.exit(0)
if not runners:
    print("missing")
elif len(runners) > 1:
    print("duplicate")
elif not runners[0].get("url") or not runners[0].get("token"):
    print("unreadable: the entry has no URL or token")
else:
    host = runners[0].get("docker", {}).get("host") or os.environ.get("DOCKER_HOST") or "unix:///var/run/docker.sock"
    print("ok " + host)
' "$2" "$1"
}

# Whether GitLab still has a runner described as $2 for project $1, and takes jobs: a runner deleted in GitLab but left
# in the local config would take no job, nor would one paused there (#38). Prints yes, paused (if any runner of that
# description is: paused true, or active false, so that a paused duplicate is not hidden), no or unknown.
runner_in_gitlab() {
    local listed
    listed=$(glab api --paginate "projects/$1/runners?type=project_type&per_page=100" 2> /dev/null) || { echo unknown; return 0; }
    python3 -I -c '
import json, sys
text, decoder, at, found = sys.stdin.read(), json.JSONDecoder(), 0, []
while True:
    while at < len(text) and text[at].isspace():
        at += 1
    if at >= len(text):
        break
    page, at = decoder.raw_decode(text, at)
    found += [r for r in page if r.get("description") == sys.argv[1]]
if any(r.get("paused") is True or r.get("active") is False for r in found):
    print("paused")
else:
    print("yes" if found else "no")' "$2" <<< "$listed" 2> /dev/null || echo unknown
}

# What to do about a runner GitLab lists as paused, named $1.
paused_advice() {
    echo "GitLab lists runner $1 as paused, so it would take no job: resume it in the project's Settings > CI/CD >" \
        "Runners, or delete it there if it is a duplicate no machine uses"
}

# Why this machine cannot run the pages job, or nothing. Arguments: the runner's name, its config file and the
# project's ID.
runner_refusal() {
    local name=$1 config=$2 id=$3 found listed
    if ! command -v gitlab-runner > /dev/null; then
        echo "gitlab-runner is not installed (brew install gitlab-runner)"
        return 0
    fi
    found=$(runner_lookup "$name" "$config")
    case "$found" in
        missing) echo "no runner named $name in $config: register it (scripts/publish-javadoc.sh --register-runner)" ;;
        duplicate) echo "$config has two runners named $name: remove one (gitlab-runner unregister --name ...)" ;;
        "ok "*)
            # Docker as the runner reaches it, not as the Docker CLI's context does.
            listed=$(runner_in_gitlab "$id" "$name")
            if [[ "$listed" == paused ]]; then
                paused_advice "$name"
            elif [[ "$listed" != yes ]]; then
                echo "GitLab has no runner $name for this project (deleted, or GitLab did not answer): remove the" \
                    "local entry (gitlab-runner unregister --name $name), then scripts/publish-javadoc.sh --register-runner"
            elif ! env -u DOCKER_CONTEXT DOCKER_HOST="${found#ok }" docker info > /dev/null 2>&1; then
                echo "Docker is not running at ${found#ok }, where the runner looks for it"
            fi ;;
        *) echo "the runner config $config cannot be read: ${found#unreadable: }" ;;
    esac
}

# Creates a project runner for the pages job in GitLab (tag soliscode-pages, no untagged jobs, locked to the
# project) and registers it on this machine with the Docker executor, the job's image and the Docker host of the
# CLI's current context (colima's socket, for one, is not where gitlab-runner looks by default). The token GitLab
# returns goes from one command to the next in the environment, and is never printed; a runner GitLab made that
# cannot be registered here is deleted again. Arguments: the project's ID, the runner's name and the config file.
register_runner() {
    local id=$1 name=$2 config=$3 found listed
    command -v gitlab-runner > /dev/null || die "gitlab-runner is not installed (brew install gitlab-runner)"
    found=$(runner_lookup "$name" "$config")
    case "$found" in
        missing) ;;
        "ok "*)
            listed=$(runner_in_gitlab "$id" "$name")
            [[ "$listed" != paused ]] || die "not registering: $(paused_advice "$name")"
            [[ "$listed" == yes ]] || die "$config has a runner $name that GitLab no longer" \
                "has: remove the local entry (gitlab-runner unregister --name $name) and run this again"
            say "runner $name is registered already, in $config"
            return 0 ;;
        *) die "not registering: $(runner_refusal "$name" "$config" "$id")" ;;
    esac
    local docker_host
    docker_host=$(docker context inspect --format '{{.Endpoints.docker.Host}}' 2> /dev/null) \
        || die "the Docker CLI has no current context: start Docker first"
    env -u DOCKER_CONTEXT DOCKER_HOST="$docker_host" docker info > /dev/null 2>&1 \
        || die "Docker is not running at $docker_host"
    local created token runner_id log="${TMPDIR:-/tmp}/publish-javadoc-register.log"
    created=$(glab api --method POST user/runners -f runner_type=project_type -F project_id="$id" \
        -f description="$name" -f tag_list=soliscode-pages -F run_untagged=false -F locked=true) \
        || die "GitLab refused to create the runner (it needs the project's Maintainer role)"
    token=$(json_field token <<< "$created")
    runner_id=$(json_field id <<< "$created")
    if ! CI_SERVER_TOKEN=$token gitlab-runner register --non-interactive --url https://gitlab.com \
        --executor docker --docker-image alpine:3.22.2 --docker-host "$docker_host" --name "$name" > "$log" 2>&1
    then
        glab api --method DELETE "runners/$runner_id" > /dev/null 2>&1 \
            || say "could not delete GitLab runner $runner_id; remove it in the project's CI/CD settings"
        die "gitlab-runner could not register the runner; its log: $log"
    fi
    say "registered runner $name (GitLab runner $runner_id, Docker at $docker_host) in $config"
}

# Cancels pipeline $2 of project $1, so that a pipeline whose job no runner took does not block the next publish.
cancel_pipeline() {
    glab api --method POST "projects/$1/pipelines/$2/cancel" > /dev/null 2>&1 \
        || say "could not cancel pipeline $2; cancel it in GitLab before the next publish"
}

# curl on the Pages site, without credentials and without following a redirect, with a connect timeout and a total
# one of $1 seconds, so that a server that accepts the connection and stalls fails the read rather than hanging the
# script (#38). -q, first, keeps the publisher's .curlrc out of it: a --retry there would start each try with its
# own --max-time and overrun the limits. No read uses -f: an HTTP status is an answer, not a failure. The arguments
# after $1 are curl's.
site_curl() {
    local max=$1
    shift
    curl -q -s --connect-timeout "$site_connect_timeout" --max-time "$max" "$@"
}

# Why a read of $1 failed with curl's exit status $2, on standard error.
read_failure() {
    if [[ "$2" == 28 ]]; then
        echo "publish-javadoc.sh: $1 did not answer: it timed out (connect ${site_connect_timeout}s, total" \
            "${site_max_time}s)" >&2
    else
        echo "publish-javadoc.sh: $1 could not be read (curl exit $2)" >&2
    fi
}

# The HTTP status of a page. A read that times out or fails says why on standard error and returns 1; the caller ends
# the script (|| exit 1), so that it gives that one message.
http_code() {
    local code status=0
    code=$(site_curl "$site_max_time" -o /dev/null -w '%{http_code}' "$1") || status=$?
    (( status == 0 )) || { read_failure "$1" "$status"; return 1; }
    echo "$code"
}

# The body of a page, as http_code reads it.
site_text() {
    local body status=0
    body=$(site_curl "$site_max_time" "$1") || status=$?
    (( status == 0 )) || { read_failure "$1" "$status"; return 1; }
    printf '%s\n' "$body"
}

# The pipeline id of the deployment the site at $1 serves now, from its deployment.txt, or nothing: no site yet ($1
# empty), a 404 (a first deployment), or a redirect where the audience, $2, is restricted (its sign-in; the probe
# check does not run there). Any other answer (a 5xx, a redirect where it is everyone, a 200 that holds no id) or a
# failed read says why on standard error and returns 1, since step 8's probe check would otherwise be skipped
# without a word.
previous_deployment() {
    [[ -n "$1" ]] || return 0
    local answer status=0 code body
    answer=$(site_curl "$site_max_time" -w '\n%{http_code}' "$1/deployment.txt") || status=$?
    (( status == 0 )) || { read_failure "$1/deployment.txt" "$status"; return 1; }
    code=${answer##*$'\n'}
    # The job writes the id with a newline after it, which the command substitution drops.
    body=$(printf '%s' "${answer%$'\n'*}")
    if [[ "$code" == 200 && "$body" =~ ^[0-9]+$ ]]; then
        echo "$body"
    elif [[ "$code" == 404 || "$2" == restricted && "$code" =~ ^3[0-9][0-9]$ ]]; then
        return 0
    else
        echo "publish-javadoc.sh: $1/deployment.txt answered $code, which names no previous deployment" >&2
        return 1
    fi
}

# Waits until the site at $1 serves the deployment of pipeline $2, since Pages may serve the previous one for a while:
# read every site_poll_interval seconds up to site_poll_limit in all, a deadline, so that the wait ends within it
# whatever the server does. Each read's total timeout is cut to the time left, and with none left it fails without
# reading (curl takes --max-time 0 as no limit). A read that times out or fails counts as a try.
await_deployment() {
    local deadline=$((SECONDS + site_poll_limit)) left got status last=""
    while true; do
        left=$((deadline - SECONDS))
        (( left > 0 )) || die "$1/deployment.txt does not hold pipeline $2's deployment after $site_poll_limit" \
            "seconds (the last read ${last:-was not made})"
        (( left < site_max_time )) || left=$site_max_time
        status=0
        got=$(site_curl "$left" "$1/deployment.txt") || status=$?
        if (( status == 28 )); then
            last="timed out"
        elif (( status != 0 )); then
            last="failed, curl exit $status"
        elif [[ "$got" == "$2" ]]; then
            return 0
        else
            last="held '${got:0:40}'"
        fi
        left=$((deadline - SECONDS))
        (( left < site_poll_interval )) || left=$site_poll_interval
        (( left <= 0 )) || sleep "$left"
    done
}

# Where the audience is everyone, that latest/ at $1 points at the newest version, $2 (#41): latest/ answers the job's
# page naming ../<newest>/, or the newest's own index.html (a child's old job, which copies it); latest without the
# slash redirects to latest/; a deep link under latest/, which the rewrite serves, is the newest's page; and the
# newest's own marker, soliscode-version.txt, read through latest/, names the newest, which a deep link the same in two
# versions cannot show. An old job's copy has no marker: there latest/ is the newest's own index page.
latest_check() {
    local url=$1 newest=$2 page own answer status=0 code location copy=false
    page=$(site_text "$url/latest/") || exit 1
    if [[ "$page" != *"../$newest/"* ]]; then
        copy=true
        own=$(site_text "$url/$newest/index.html") || exit 1
        [[ "$page" == "$own" ]] || die "$url/latest/ neither sends the reader to ../$newest/ nor is $newest's index page"
    fi
    answer=$(site_curl "$site_max_time" -o /dev/null -w '%{http_code} %{redirect_url}' "$url/latest") || status=$?
    (( status == 0 )) || { read_failure "$url/latest" "$status"; exit 1; }
    code=${answer%% *}
    location=${answer#* }
    [[ "$code" =~ ^30[1278]$ && "$location" == */latest/ ]] \
        || die "$url/latest answered $code ${location:-with no redirect}, not a redirect to latest/"
    local deep versioned
    deep=$(site_text "$url/latest/allclasses-index.html") || exit 1
    versioned=$(site_text "$url/$newest/allclasses-index.html") || exit 1
    [[ "$deep" == "$versioned" ]] \
        || die "$url/latest/allclasses-index.html is not $newest's: the rewrite does not point at the newest"
    local marker
    code=$(http_code "$url/latest/soliscode-version.txt") || exit 1
    if [[ "$code" == 200 ]]; then
        marker=$(site_text "$url/latest/soliscode-version.txt") || exit 1
        [[ "$marker" == "$newest" ]] \
            || die "$url/latest/soliscode-version.txt holds '${marker:0:40}', not $newest: the rewrite does not point at the newest"
    elif [[ "$code" != 404 || "$copy" == false ]]; then
        die "$url/latest/soliscode-version.txt answered $code: the rewrite does not serve the newest's marker"
    fi
}

main() {
    trap on_exit EXIT
    local check=false store_only=false register=false version=""
    case "${1:-}" in
        --check) check=true; shift ;;
        --store-only) store_only=true; shift ;;
        --register-runner) register=true; shift ;;
    esac
    if [[ "$register" == true ]]; then
        [[ $# -eq 0 ]] || die "Usage: scripts/publish-javadoc.sh --register-runner"
    else
        [[ $# -eq 1 ]] || die "Usage: scripts/publish-javadoc.sh [--check | --store-only] <version>"
        version=$1
        [[ "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "'$version' is not a release version, such as 0.1.13"
    fi

    local root
    root=$(git rev-parse --show-toplevel)
    cd "$root"
    [[ "$register" == true ]] || git rev-parse -q --verify "refs/tags/v$version" > /dev/null \
        || die "there is no tag v$version"

    # The project, from the remote: git@gitlab.com:<path>.git or https://gitlab.com/<path>.git.
    local remote path encoded project
    remote=$(git remote get-url origin)
    path=$(sed -E -e 's#^(git@|ssh://git@|https://)gitlab\.com[:/]##' -e 's#\.git$##' <<< "$remote")
    [[ "$path" != "$remote" && "$path" =~ ^[A-Za-z0-9._/-]+$ ]] || die "origin ($remote) is not a gitlab.com project"
    encoded=${path//\//%2F}
    project=$(glab api "projects/$encoded") || die "glab could not read the project $path: is glab logged in?"

    local id visibility pages registry repository refusal
    id=$(json_field id <<< "$project")
    if [[ "$register" == true ]]; then
        register_runner "$id" "$(runner_name "${path##*/}" "$id")" "$HOME/.gitlab-runner/config.toml"
        return 0
    fi
    visibility=$(json_field visibility <<< "$project")
    pages=$(json_field pages_access_level <<< "$project")
    registry=$(json_field package_registry_access_level <<< "$project")
    repository=$(json_field repository_access_level <<< "$project")
    # The branch the pipeline runs on and reads .gitlab-ci.yml from: the project's default, not a fixed main (#36).
    local branch unbranched
    branch=$(json_field default_branch <<< "$project")
    unbranched=$(branch_refusal "$branch")
    refusal=$(exposure_refusal "$visibility" "$pages" "$registry" "$repository")
    say "$path (id $id): visibility $visibility, Pages $pages (readable by $(pages_audience "$visibility" "$pages")),"\
        "package registry $registry, repository $repository: ${refusal:-may publish}"
    [[ -z "$refusal" ]] || die "refused: $refusal"
    say "pipeline branch: ${unbranched:-$branch}"

    # K, read and checked before any build, upload or pipeline, --store-only included (#41).
    local kept keep keep_source
    kept=$(site_versions "$root/pom.xml")
    [[ "$kept" != refused* ]] || die "refused: ${kept#refused }"
    keep=${kept%% *}
    [[ "${kept#* }" == pom ]] && keep_source="soliscode.javadoc.site.versions in pom.xml" || keep_source="the default"
    say "site versions: $keep ($keep_source)"
    [[ "$check" == true || "$store_only" == true || -z "$unbranched" ]] || die "refused: $unbranched"

    # The project runner on this machine that runs the pages job (the job's tag, soliscode-pages).
    local runner config unready
    runner=$(runner_name "${path##*/}" "$id")
    config="$HOME/.gitlab-runner/config.toml"
    unready=$(runner_refusal "$runner" "$config" "$id")
    say "runner $runner: ${unready:-ready}"
    [[ "$check" == false ]] || return 0
    [[ "$store_only" == true || -z "$unready" ]] || die "refused: $unready"

    [[ -z "${MAVEN_ARGS:-}" ]] || die "MAVEN_ARGS is set ('$MAVEN_ARGS'); Maven would add it to the build. Unset it."
    local var
    for var in MAVEN_OPTS JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS; do
        [[ "${!var:-}" != *-D* ]] || die "$var sets a property ('${!var}'), which the JVM passes to Maven. Unset it."
    done

    local stored
    stored=$(glab api --paginate "projects/$id/packages?package_type=generic&package_name=javadoc&per_page=100" \
        | javadoc_versions)
    if grep -qxF "$version" <<< "$stored"; then
        say "javadoc $version is stored already; deploying the stored versions"
    else
        # Builds share ~/.m2 and a build's target/, so never two at once.
        ! pgrep -f org.codehaus.plexus.classworlds.launcher.Launcher > /dev/null \
            || die "another Maven build is running; wait for it to finish"
        local work
        work=$(mktemp -d "${TMPDIR:-/tmp}/publish-javadoc.XXXXXX")
        local log="${TMPDIR:-/tmp}/publish-javadoc-$version.log"
        # The worktree goes whatever happens; the log stays, outside it, for a failed build. The trap calls a
        # function that reads the paths when it runs, so no path is parsed again as shell text.
        cleanup_root=$root
        cleanup_work=$work
        git worktree add -q --detach "$work/src" "v$version"
        say "building the site of v$version in $work/src (log: $log)"
        # package first: in this repository the site unpacks soliscode-build-tools, which a reactor build of it
        # must have packaged (MDEP-98); in a child it only builds the jars. The release ran the tests; this runs none.
        (cd "$work/src" && mvn -B -Drevision="$version" "${site_build[@]}") > "$log" 2>&1 \
            || { tail -40 "$log" >&2; die "the site build of v$version failed; its log: $log"; }
        local apidocs="$work/src/target/site/en/apidocs"
        [[ -f "$apidocs/index.html" ]] || die "the site has no Javadoc at target/site/en/apidocs"
        (cd "$apidocs" && zip -qr "$work/javadoc.zip" .)
        glab api --method PUT "projects/$id/packages/generic/javadoc/$version/javadoc.zip" \
            --input "$work/javadoc.zip" > /dev/null || die "the upload of javadoc $version failed"
        say "stored javadoc $version ($(du -h "$work/javadoc.zip" | cut -f1))"
        stored=$(printf '%s\n%s\n' "$stored" "$version" | sort_versions)
    fi
    [[ "$store_only" == false ]] || { say "stored; no pipeline started (--store-only)"; return 0; }
    # The site keeps the newest K stored versions; older ones stay in the registry (#41).
    local deployed_list versions
    deployed_list=$(tail -n "$keep" <<< "$stored")
    versions=$(tr '\n' ' ' <<< "$deployed_list" | sed 's/ *$//')
    grep -qxF "$version" <<< "$deployed_list" \
        || say "javadoc $version is stored, but is not among the newest $keep ($versions); the site keeps those"

    # A deployment with an older list must not finish after one with a newer list (the job's resource_group
    # serialises the jobs; this keeps a stale list from being queued).
    local status busy
    for status in created waiting_for_resource preparing pending running scheduled; do
        busy=$(glab api "projects/$id/pipelines?status=$status&per_page=1")
        [[ "$busy" == "[]" ]] || die "a pipeline of $path is $status; wait for it to finish"
    done

    # The previous deployment's id, whose probe file must be gone once this one is live (step 8).
    local url previous
    url=$(glab api "projects/$id/pages" 2> /dev/null | json_field url || true)
    url=${url%/}
    previous=$(previous_deployment "$url" "$(pages_audience "$visibility" "$pages")") || exit 1

    local body pipeline pid state=created waited=0
    body=$(pipeline_body "$branch" "$versions")
    pipeline=$(glab api --method POST "projects/$id/pipeline" -H "Content-Type: application/json" \
        --input - <<< "$body") || die "GitLab refused the pipeline"
    pid=$(json_field id <<< "$pipeline")
    pending_project=$id
    pending_pipeline=$pid
    say "pipeline $pid on $branch deploys javadoc $versions; running its job on $runner"

    # The runner takes the one job and exits. It reads its URL, token and Docker settings from its own entry in the
    # config (-r), so the token never passes through this script. If it took no job, or failed, the pipeline is
    # cancelled, so that no pending pipeline blocks the next publish (step 7).
    local runlog="${TMPDIR:-/tmp}/publish-javadoc-runner.log"
    # Whether the runner took the job is the job's own status: the pipeline's is recomputed later, and can still
    # read pending after a fast job has run.
    local ran=true job
    gitlab-runner run-single -c "$config" -r "$runner" --max-builds 1 --wait-timeout 120 > "$runlog" 2>&1 \
        || ran=false
    job=$(job_status "$id" "$pid")
    if [[ "$job" == unknown ]]; then
        cancel_pipeline "$id" "$pid"
        die "GitLab did not say whether the runner took the job; if pipeline $pid is still pending, cancel it" \
            "(glab api --method POST projects/$id/pipelines/$pid/cancel); the runner's log: $runlog"
    fi
    if [[ "$job" =~ ^(created|waiting_for_resource|preparing|pending|missing)$ ]]; then
        cancel_pipeline "$id" "$pid"
        [[ "$ran" == true ]] || die "the runner failed; pipeline $pid is cancelled; the runner's log: $runlog"
        die "the runner took no job in two minutes; pipeline $pid is cancelled; the runner's log: $runlog"
    fi
    if [[ "$ran" == false && "$job" =~ ^(running|preparing)$ ]]; then
        # The runner died with the job in hand: GitLab would hold the job running until it misses its heartbeat.
        cancel_pipeline "$id" "$pid"
        die "the runner failed during the job; pipeline $pid is cancelled; the runner's log: $runlog"
    fi
    [[ "$ran" == true ]] || say "the runner exited with an error after the job ended (job $job); its log: $runlog"
    state=created
    while [[ "$state" =~ ^(created|waiting_for_resource|preparing|pending|running)$ ]]; do
        (( waited < 600 )) || die "pipeline $pid has not finished after ten minutes: $(json_field web_url <<< "$pipeline")"
        sleep "$pipeline_poll_interval"
        waited=$((waited + pipeline_poll_interval))
        pipeline=$(glab api "projects/$id/pipelines/$pid")
        state=$(json_field status <<< "$pipeline")
    done
    # The job's own lines, the site's size among them, from its trace (#41).
    local job_id lines="" refusal_line=""
    job_id=$(pages_job_id "$id" "$pid")
    if [[ -n "$job_id" ]] && lines=$(job_lines "$id" "$job_id"); then
        [[ -z "$lines" ]] || printf '%s\n' "$lines"
        refusal_line=$(grep -m1 '^pages-site.sh: refused: the site of versions' <<< "$lines" || true)
    else
        say "GitLab gave no trace of the pages job, so its lines are not shown"
    fi
    if [[ -n "$refusal_line" ]]; then
        # A smaller K helps only where the site of the newest alone fits and more than one version was deployed: K
        # above the number stored deploys the same list (#41).
        local advice deployed_count
        deployed_count=$(wc -l <<< "$deployed_list" | tr -d ' ')
        if [[ "$refusal_line" == *", within the caps" && "$deployed_count" -gt 1 ]]; then
            advice="lower soliscode.javadoc.site.versions (now $keep, $keep_source) in the root pom.xml, below the"
            advice+=" $deployed_count versions deployed"
        else
            advice="no smaller soliscode.javadoc.site.versions helps (now $keep, $keep_source)"
        fi
        die "the pages job refused the site, and GitLab deployed nothing: $advice; or leave more out of the" \
            "Javadoc (soliscode.javadoc.site.excludePackageNames, soliscode.javadoc.site.skippedModules), which" \
            "shrinks the site from the next release on, since a stored version is never rebuilt"
    fi
    [[ "$state" == success ]] \
        || die "pipeline $pid ended $state: $(json_field web_url <<< "$pipeline"); the runner's log: $runlog"

    # A green pipeline alone does not show the site works: read it as a stranger would. The newest version first,
    # retried, since Pages may serve the previous deployment for a while; then latest/, then the older versions,
    # which show that the redeploy kept them.
    local expected newest code page deployed now latest
    url=$(glab api "projects/$id/pages" | json_field url)
    url=${url%/}
    [[ "$(pages_audience "$visibility" "$pages")" == everyone ]] && expected=200 || expected=302
    newest=$(tail -1 <<< "$deployed_list")
    [[ "$expected" != 200 ]] || await_deployment "$url" "$pid"
    code=$(http_code "$url/$newest/index.html") || exit 1
    [[ "$code" == "$expected" ]] || die "$url/$newest/index.html answered $code, not $expected"
    if [[ "$expected" == 200 ]]; then
        latest=$(site_text "$url/latest/VERSION") || exit 1
        [[ "$latest" == "$newest" ]] || die "$url/latest/ does not hold $newest"
        latest_check "$url" "$newest"
    fi
    for page in latest/allclasses-index.html $(sed 's#[^ ]*#&/index.html#g' <<< "$versions"); do
        code=$(http_code "$url/$page") || exit 1
        [[ "$code" == "$expected" ]] || die "$url/$page answered $code, not $expected"
    done
    if [[ "$expected" == 200 ]]; then
        # The whole site: what the job deployed must be what the registry holds now, so that no list taken
        # before the lock stays deployed; and a deployment replaces the site, so the previous probe is gone.
        deployed=$(site_text "$url/versions.txt") || exit 1
        now=$(glab api --paginate "projects/$id/packages?package_type=generic&package_name=javadoc&per_page=100" \
            | javadoc_versions | tail -n "$keep")
        [[ "$deployed" == "$now" ]] \
            || die "the site holds $(tr '\n' ' ' <<< "$deployed")but the registry $(tr '\n' ' ' <<< "$now")(another publish?); run again"
        code=$(http_code "$url/deployment-$pid.txt") || exit 1
        [[ "$code" == 200 ]] || die "this deployment's probe file is not served"
        if [[ -n "$previous" ]]; then
            code=$(http_code "$url/deployment-$previous.txt") || exit 1
            [[ "$code" == 404 ]] || die "the previous deployment's probe answered $code: the site was not replaced"
        fi
    fi
    pending_pipeline=""
    say "published: $url/latest/ (versions: $versions; each answered $expected without credentials)"
}

if [[ "${BASH_SOURCE[0]:-}" == "$0" ]]; then
    main "$@"
fi
