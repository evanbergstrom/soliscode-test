#!/usr/bin/env bash
# Copyright 2026 Evan Bergstrom
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makes a local release, 0.1.N, of soliscode-test (docs/design/reference/build-and-test.md, "release.sh"; RELEASES.md).
# Installs it into ~/.m2, writes its entry in RELEASES.md, commits "Release 0.1.N" and tags v0.1.N.
# Nothing is pushed.
#
# Usage: scripts/release/release.sh --changes <file> [--trailer <line>]
#   <file> holds the release's changes as Markdown bullets, without a heading.
#   <line> is a trailer for the release commit, such as the Co-Authored-By line of the session
#   that runs the release; given more than once, each is added.

set -euo pipefail

die() { echo "release.sh: $*" >&2; exit 1; }

changes=""
trailers=""   # one paragraph of trailer lines
while [[ $# -gt 0 ]]; do
    case "$1" in
        --changes) [[ $# -ge 2 ]] || die "--changes needs a file"; changes="$2"; shift 2 ;;
        --trailer) [[ $# -ge 2 && -n "$2" ]] || die "--trailer needs a line"; trailers+="${trailers:+$'\n'}$2"; shift 2 ;;
        *) die "unknown argument '$1'. Usage: release.sh --changes <file> [--trailer <line>]" ;;
    esac
done
[[ -n "$changes" ]] || die "usage: release.sh --changes <file>"
[[ -s "$changes" ]] || die "the changes file '$changes' does not exist or is empty"
if grep -qE '^ {0,3}#' "$changes"; then
    die "the changes file has a heading; it holds the bullets only (the script adds ### Changes)"
fi
changes=$(cd "$(dirname "$changes")" && pwd)/$(basename "$changes")

# As verify.sh: nothing from the environment may change the release build.
[[ -z "${MAVEN_ARGS:-}" ]] || die "MAVEN_ARGS is set ('$MAVEN_ARGS'); Maven would add it to the build. Unset it."
for var in MAVEN_OPTS JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS; do
    [[ "${!var:-}" != *-D* ]] || die "$var sets a property ('${!var}'), which the JVM passes to Maven and could change the build. Unset it."
done

root=$(git rev-parse --show-toplevel)
cd "$root"
# shellcheck source=../lib/records.sh
source "$root/scripts/lib/records.sh"

# 1. On main, with a clean working copy.
[[ "$(git symbolic-ref --short -q HEAD || true)" == "main" ]] || die "not on main"
[[ -z "$(git status --porcelain)" ]] || die "the working copy is not clean (git status)"

# 2. The version: one past the highest v0.1.<digits> tag. The old v1.0.* tags are not counted.
last=$(git tag --list 'v0.1.*' --sort=-v:refname | grep -E '^v0\.1\.[0-9]+$' | head -1 || true)
if [[ -z "$last" ]]; then
    n=1
else
    n=$(( ${last#v0.1.} + 1 ))
fi
version="0.1.$n"
tag="v$version"
git rev-parse -q --verify "refs/tags/$tag" > /dev/null && die "the tag $tag exists already"

# 3. A green full verify on record for HEAD's tree.
tree=$(git rev-parse 'HEAD^{tree}')
record="$(records_dir)/verified/$tree"
[[ -f "$record" ]] || die "no green full verify on record for HEAD's tree $tree. Run scripts/verify.sh on main first."
tests=$(sed -n 's/^tests=//p' "$record")
commit=$(git rev-parse HEAD)
short=$(git rev-parse --short HEAD)

# 4. Build and install the release. The record shows the tests green for this tree.
echo "release.sh: building and installing $version from $short"
mvn -B -Drevision="$version" -DskipTests -Djacoco.check.skip=true clean install

# 5. The entry in RELEASES.md. From here, a failure restores RELEASES.md from HEAD.
restore() {
    echo "release.sh: failed; restoring RELEASES.md from HEAD" >&2
    git checkout HEAD -- RELEASES.md
}
trap restore ERR INT TERM

python3 -I - "$root/RELEASES.md" "$changes" "$version" "$(date +%Y-%m-%d)" "$short" "$tests" <<'EOF'
import re, sys
path, changes_path, version, date, commit, tests = sys.argv[1:]
text = open(path, encoding="utf-8").read()
changes = open(changes_path, encoding="utf-8").read().strip()
entry = (
    f"## {version} — {date}\n\n"
    f"Commit `{commit}`. Full verify: Tests run: {tests}, Failures: 0, Errors: 0.\n\n"
    f"Install coordinates: `com.soliscode:soliscode-test:{version}`.\n\n"
    f"### Changes\n{changes}\n\n"
)
text = text.replace("No release has been made yet.\n\n", "", 1)
unreleased = re.search(r"^## Unreleased\n.*?(?=^## |\Z)", text, flags=re.M | re.S)
if unreleased:
    # An empty "## Unreleased" stays above the new entry, for the next tasks' changes, so the
    # entries stay newest first.
    text = text[:unreleased.start()] + "## Unreleased\n\n" + entry + text[unreleased.end():]
else:
    newest = re.search(r"^## 0\.1\.\d+ ", text, flags=re.M)
    if newest:
        text = text[:newest.start()] + entry + text[newest.start():]
    else:
        text = text.rstrip("\n") + "\n\n" + entry
open(path, "w", encoding="utf-8").write(text.rstrip("\n") + "\n")
EOF

# 6. Commit and tag, locally.
git add RELEASES.md
if [[ -n "$trailers" ]]; then
    git commit -q -m "Release $version" -m "$trailers"
else
    git commit -q -m "Release $version"
fi
trap - ERR INT TERM
release_commit=$(git rev-parse HEAD)
if ! git tag -a "$tag" -m "Release $version" "$release_commit"; then
    die "the release commit is made, but tagging failed. Tag it by hand: git tag -a $tag -m \"Release $version\" $release_commit"
fi
echo "release.sh: released $version (commit $(git rev-parse --short HEAD), tag $tag), built from $commit. Nothing pushed."
