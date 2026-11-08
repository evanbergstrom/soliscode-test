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

# Shared by verify.sh, release.sh and the git hooks: where the build records live, and how a
# working copy's tree is named (docs/design/reference/build-and-test.md, "Releases" and "Git hooks").
# Sourced, not run.

# The directory that holds the records: inside .git, shared by the clone's worktrees.
records_dir() {
    local common
    common=$(git rev-parse --git-common-dir)
    echo "$(cd "$common" && pwd)/soliscode"
}

# The tree of the working copy: every tracked file, and every untracked file .gitignore does not
# exclude. Built in a temporary index, so the real index is not touched.
working_tree() {
    local tmp tree
    tmp=$(mktemp -d)
    GIT_INDEX_FILE="$tmp/index" git read-tree HEAD
    GIT_INDEX_FILE="$tmp/index" git add -A
    tree=$(GIT_INDEX_FILE="$tmp/index" git write-tree)
    rm -rf "$tmp"
    echo "$tree"
}
