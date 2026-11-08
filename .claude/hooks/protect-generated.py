"""PreToolUse hook: refuse edits to build output and generated code.

They are rebuilt by every build and never committed, so an edit there is lost and hides the real
fix. Exit 2 blocks the call and shows Claude the reason.
"""
import json
import re
import sys

GENERATED = [
    (re.compile(r"(^|/)target/"), "Maven build output"),
    (re.compile(r"(^|/)generated-(test-)?sources/"), "generated source"),
    (re.compile(r"(^|/)\.flattened-pom\.xml$"), "a POM the flatten plugin writes"),
]

try:
    event = json.load(sys.stdin)
except ValueError:
    sys.exit(0)  # never block on a malformed event
tool_input = event.get("tool_input") or {}
path = tool_input.get("file_path") or tool_input.get("notebook_path") or ""
for pattern, what in GENERATED:
    if pattern.search(path):
        print(f"{path} is {what}: it is rebuilt and never committed, so change its source instead.",
              file=sys.stderr)
        sys.exit(2)
sys.exit(0)
