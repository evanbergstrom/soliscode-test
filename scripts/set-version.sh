#!/usr/bin/env bash
set -euo pipefail
mvn -q versions:set -DnewVersion="$1" -DgenerateBackupPoms=false
