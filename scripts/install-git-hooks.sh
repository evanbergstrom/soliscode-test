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

# Points this clone at the hooks in scripts/git-hooks/ (docs/design/reference/build-and-test.md, "Git hooks").
# Run once per clone.

set -euo pipefail

root=$(git rev-parse --show-toplevel)
cd "$root"
chmod +x scripts/git-hooks/* scripts/verify.sh scripts/release/release.sh
git config core.hooksPath scripts/git-hooks
echo "install-git-hooks.sh: core.hooksPath is scripts/git-hooks (pre-commit, pre-push)"
