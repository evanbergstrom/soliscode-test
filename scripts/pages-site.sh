#!/bin/sh
# Copyright 2025 Evan Bergstrom
# SPDX-License-Identifier: Apache-2.0

# The pages job's work (.gitlab-ci.yml), moved out of the YAML in #41 so that a test can run it: unpacks each stored
# Javadoc version into public/<version>/, sends latest/ to the newest (a 200 rewrite in public/_redirects for a deep
# link, and a small index.html for latest/ itself, not a copy), writes the index and the files publish-javadoc.sh
# reads, then measures the site and refuses one over GitLab.com's Pages caps before GitLab does
# (docs/design/reference/documentation.md, section 3.8). POSIX sh, since the job's alpine image has busybox's ash
# and no Bash. It builds nothing. A child that publishes has a copy, installed by soliscode:docs with
# publish-javadoc.sh and .gitlab-ci.yml from the tag of its parent version.
#
# Usage: VERSIONS="<version> ..." sh scripts/pages-site.sh   (in the pages job, with GitLab's CI_ variables)

set -eu

say() { echo "pages-site.sh: $*"; }
refuse() { echo "pages-site.sh: refused: $*"; exit 1; }

# GitLab.com's caps on a Pages site: "1 GB", read as the smaller 10^9 bytes and applied to the extracted files, which
# for Javadoc are larger than the archive; and 200,000 file entries, directories included (section 3.8). The two
# variables are for the test: raising one only moves the refusal back to GitLab. Set but empty, one refuses.
cap=${PAGES_SITE_CAP-1000000000}
entry_cap=${PAGES_SITE_ENTRY_CAP-200000}
case $cap in '' | *[!0-9]*) refuse "PAGES_SITE_CAP ('$cap') is not a whole number of bytes" ;; esac
case $entry_cap in '' | *[!0-9]*) refuse "PAGES_SITE_ENTRY_CAP ('$entry_cap') is not a whole number of entries" ;; esac
# Leading zeros go, or sh's arithmetic would read the caps as octal.
cap=${cap#"${cap%%[!0]*}"}
entry_cap=${entry_cap#"${entry_cap%%[!0]*}"}
cap=${cap:-0}
entry_cap=${entry_cap:-0}
[ "$cap" -gt 0 ] || refuse "PAGES_SITE_CAP is 0"
[ "$entry_cap" -gt 0 ] || refuse "PAGES_SITE_ENTRY_CAP is 0"

[ -n "${VERSIONS:-}" ] || refuse "no versions given"
[ -n "${CI_PAGES_URL:-}" ] || refuse "CI_PAGES_URL is not set"

# The site's index page, for the versions $2..., the newest $1, to standard output.
write_index() {
    newest=$1
    shift
    echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8">'
    echo "<title>$CI_PROJECT_TITLE API reference</title></head><body>"
    echo "<h1>$CI_PROJECT_TITLE API reference</h1><ul>"
    echo "<li><a href=\"latest/\">latest</a> ($newest)</li>"
    for version in "$@"; do echo "<li><a href=\"$version/\">$version</a></li>"; done
    echo '</ul></body></html>'
}

# The bytes (bytes), the entries (entries) or the bytes of the entries' paths, a newline each (paths), under public/,
# public/ itself included, leaving out each path after $1
# (and, for a directory, all under it). It prints the count, or nothing and returns 1 if it could not measure: POSIX
# sh has no pipefail, so find's success is recorded apart from the pipe, as an ok in a marker file of the measure's
# own outside public/, which counts only if find exited 0 and the marker was written; wc must succeed, with digits.
# busybox's find was seen to exit 0 when a cat it ran for a batch of files failed (alpine:3.22.2, 6,000 files), so
# each batch's cat records its own failure by removing a file made beside the marker before the walk, whose absence
# refuses too: a removal needs no new file, so a full disk or no free inode cannot hide it.
measure() {
    kind=$1
    shift
    prune=""
    for path in "$@"; do prune="$prune -path $path -prune -o"; done
    marker=$(mktemp) || return 1
    : > "$marker" && [ ! -s "$marker" ] && : > "$marker.clean" || { rm -f "$marker" "$marker.clean"; return 1; }
    # $prune is unquoted on purpose: its words are find's arguments, and the paths are public/ and version numbers.
    # shellcheck disable=SC2086
    if [ "$kind" = bytes ]; then
        count=$({ find public $prune -type f -exec sh -c 'cat -- "$@" || rm -f "$0"' "$marker.clean" {} + \
            && echo ok > "$marker"; } | wc -c) || count=""
    elif [ "$kind" = paths ]; then
        count=$({ find public $prune -print && echo ok > "$marker"; } | wc -c) || count=""
    else
        count=$({ find public $prune -print && echo ok > "$marker"; } | wc -l) || count=""
    fi
    written=$(cat "$marker" 2> /dev/null || true)
    [ -e "$marker.clean" ] || written=failed
    rm -f "$marker" "$marker.clean"
    # macOS's wc pads its count with spaces; busybox's does not.
    count=$(echo $count)
    [ "$written" = ok ] || return 1
    case $count in '' | *[!0-9]*) return 1 ;; esac
    echo "$count"
}

# A fresh site each run: the job's checkout has none, and a run here must not unzip over an old one.
rm -rf public
mkdir public
latest=""
for version in $VERSIONS; do
    wget -q --header "JOB-TOKEN: ${CI_JOB_TOKEN:-}" -O javadoc.zip \
        "$CI_API_V4_URL/projects/$CI_PROJECT_ID/packages/generic/javadoc/$version/javadoc.zip"
    mkdir -p "public/$version"
    unzip -q javadoc.zip -d "public/$version"
    # Each version's own marker, a name Javadoc never writes: read through latest/, it shows where the rewrite points.
    echo "$version" > "public/$version/soliscode-version.txt"
    rm javadoc.zip
    latest=$version
done

# latest/: two real files, which Pages serves before any rule, and one rule for everything else under it. The rule's
# prefix is the site's path, which a site on the default domain puts before every rule (/soliscode-parent here), and
# which is empty with the unique domain on.
mkdir -p public/latest
echo "$latest" > public/latest/VERSION
{
    echo '<!DOCTYPE html><html lang="en"><head><meta charset="utf-8">'
    echo "<meta http-equiv=\"refresh\" content=\"0; url=../$latest/\">"
    echo "<title>$CI_PROJECT_TITLE API reference, latest</title></head><body>"
    echo "<p>The newest release is <a href=\"../$latest/\">$latest</a>.</p>"
    echo '</body></html>'
} > public/latest/index.html
site=${CI_PAGES_URL#*://}
case $site in */*) prefix=/${site#*/} ;; *) prefix="" ;; esac
prefix=${prefix%/}
echo "$prefix/latest/* $prefix/$latest/:splat 200" > public/_redirects

# shellcheck disable=SC2086
for version in $VERSIONS; do echo "$version"; done > public/versions.txt
echo "${CI_PIPELINE_ID:-}" > public/deployment.txt
: > "public/deployment-${CI_PIPELINE_ID:-}.txt"
# shellcheck disable=SC2086
write_index "$latest" $VERSIONS > public/index.html

# The most the site's artifact can be: GitLab.com caps the compressed archive the job uploads, and a zip of files that
# do not compress is their bytes, each path twice (its local header and the central directory) and 256 bytes for
# each entry's headers, extra fields (GitLab Runner's writer adds several, in both places) and descriptor, about 160
# at most. The byte cap is compared with this bound, which is never
# below the extracted bytes, so it holds whether GitLab measures the archive or the files (section 3.8).
archive_bound() { echo $(($1 + 2 * $2 + 256 * $3)); }

bytes=$(measure bytes) || refuse "the site could not be measured (its bytes)"
entries=$(measure entries) || refuse "the site could not be measured (its entries)"
paths=$(measure paths) || refuse "the site could not be measured (its paths)"
archive=$(archive_bound "$bytes" "$paths" "$entries")
say "the site is $bytes bytes, at most $archive as an archive ($((archive * 100 / cap))% of the cap of $cap), and" \
    "$entries entries ($((entries * 100 / entry_cap))% of the cap of $entry_cap)"

over=""
[ "$archive" -le "$cap" ] || over="$cap bytes"
if [ "$entries" -gt "$entry_cap" ]; then
    over="${over:+$over and }$entry_cap entries"
fi
if [ -z "$over" ]; then
    if [ $((archive * 100)) -gt $((cap * 80)) ] || [ $((entries * 100)) -gt $((entry_cap * 80)) ]; then
        say "warning: the site is near GitLab's cap; lower soliscode.javadoc.site.versions, or leave more out of" \
            "the Javadoc"
    fi
    exit 0
fi

# Over a cap: the site as K=1 would deploy it, so that publish-javadoc.sh can say whether a smaller K helps. Every
# other version's directory is left out, and the index and versions.txt are measured in their one-version forms.
others=""
for version in $VERSIONS; do [ "$version" = "$latest" ] || others="$others public/$version"; done
scratch=$(mktemp -d)
write_index "$latest" "$latest" > "$scratch/index.html"
echo "$latest" > "$scratch/versions.txt"
# shellcheck disable=SC2086
one_bytes=$(measure bytes $others public/index.html public/versions.txt) \
    || refuse "the site could not be measured (the newest alone)"
# shellcheck disable=SC2086
one_entries=$(measure entries $others) || refuse "the site could not be measured (the newest alone)"
# shellcheck disable=SC2086
one_paths=$(measure paths $others) || refuse "the site could not be measured (the newest alone)"
one_bytes=$((one_bytes + $(wc -c < "$scratch/index.html") + $(wc -c < "$scratch/versions.txt")))
rm -rf "$scratch"
one_archive=$(archive_bound "$one_bytes" "$one_paths" "$one_entries")
if [ "$one_archive" -le "$cap" ] && [ "$one_entries" -le "$entry_cap" ]; then
    fits="within the caps"
else
    fits="also over the cap"
fi
refuse "the site of versions $VERSIONS is $bytes bytes, at most $archive as an archive, and $entries entries, over" \
    "GitLab.com's Pages cap of $over; a site of the newest, $latest, alone would be $one_bytes bytes, at most" \
    "$one_archive as an archive, and $one_entries entries, $fits"
