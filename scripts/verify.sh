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

# The full verify, from clean, and the record of it that release.sh needs
# (docs/design/reference/build-and-test.md, "The record of a green build").
#
# Usage: scripts/verify.sh        (no arguments)

set -euo pipefail

die() { echo "verify.sh: $*" >&2; exit 1; }

[[ $# -eq 0 ]] || die "takes no arguments: the record must stand for the full verify"
[[ -z "${MAVEN_ARGS:-}" ]] || die "MAVEN_ARGS is set ('$MAVEN_ARGS'); Maven would add it to the build. Unset it."
for var in MAVEN_OPTS JAVA_TOOL_OPTIONS _JAVA_OPTIONS JDK_JAVA_OPTIONS; do
    [[ "${!var:-}" != *-D* ]] || die "$var sets a property ('${!var}'), which the JVM passes to Maven and could change the build. Unset it."
done

root=$(git rev-parse --show-toplevel)
cd "$root"
# shellcheck source=lib/records.sh
source "$root/scripts/lib/records.sh"

records=$(records_dir)
mkdir -p "$records/verified"
log="$records/verify.log"

tree=$(working_tree)
commit=$(git rev-parse HEAD)
echo "verify.sh: mvn -B clean verify, for tree $tree (HEAD $commit); log in $log"

if ! mvn -B clean verify > "$log" 2>&1; then
    grep -E '\[ERROR\]|Tests run:.*(Failures: [1-9]|Errors: [1-9])' "$log" | head -30 >&2 || true
    die "the build failed; nothing recorded. Log: $log"
fi

# The test totals, from Surefire's XML reports.
totals=$(python3 -I - "$root" <<'EOF'
import glob, sys, xml.etree.ElementTree as ET
t = f = e = s = 0
for p in glob.glob(sys.argv[1] + "/target/surefire-reports/TEST-*.xml"):
    r = ET.parse(p).getroot()
    t += int(r.get("tests", 0)); f += int(r.get("failures", 0))
    e += int(r.get("errors", 0)); s += int(r.get("skipped", 0))
print(t, f, e, s)
EOF
)
read -r tests failures errors skipped <<< "$totals"
[[ "$tests" -gt 0 ]] || die "the build ran no tests; nothing recorded"
[[ "$failures" -eq 0 && "$errors" -eq 0 ]] || die "the reports show $failures failures and $errors errors; nothing recorded"

# The coverage check ran and was met; it was not skipped (no jacoco.exec, a skip flag).
met=$(grep -c 'All coverage checks have been met.' "$log" || true)
[[ "$met" -eq 1 ]] || die "the coverage check was met $met times, not once; nothing recorded"
if grep -q 'Skipping JaCoCo execution' "$log"; then
    die "a JaCoCo goal was skipped; nothing recorded"
fi

after=$(working_tree)
[[ "$after" == "$tree" ]] || die "the working copy changed while the build ran ($tree, now $after); nothing recorded"

cat > "$records/verified/$tree" <<EOF
date=$(date -u +%Y-%m-%dT%H:%M:%SZ)
commit=$commit
tree=$tree
tests=$tests
failures=$failures
errors=$errors
skipped=$skipped
EOF
echo "verify.sh: green. Tests run: $tests, Failures: 0, Errors: 0, Skipped: $skipped. Recorded for tree $tree."
