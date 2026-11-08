"""PreToolUse hook: refuse shell commands that could print, change or remove ~/.m2/settings.xml.

It holds the user's credentials. Maven reads it itself and needs no command that names it. The
deny rules in settings.json cover the Read, Edit and Write tools and two command prefixes; this
hook covers the shell. A list of reading commands is not enough (rm, /bin/cat, an interpreter, a
glob, a recursive grep or a cd into ~/.m2 all pass one), so the hook refuses:
- any command that names both ".m2" and "settings", in any order;
- the effective-settings goal, which prints the settings;
- any command that refers to the .m2 directory itself, or to anything in it outside repository/
  (with ~, $HOME, an absolute home path, or relative): that covers `grep -r ... ~/.m2`,
  `cat ~/.m2/*.xml`, `cd ~/.m2`, `find ~/.m2 ...` and `rm -rf ~/.m2`, while
  `ls ~/.m2/repository/...` and `-Dmaven.repo.local=~/.m2/repository` stay allowed;
- a path that climbs out of repository/ with "..".
A false refusal costs a rephrased command; a missed one can expose or destroy the credentials.
Exit 2 blocks the call.
"""
import json
import re
import sys

NAMES_SETTINGS = re.compile(r"\.m2\b.*settings|settings.*\.m2\b", re.IGNORECASE | re.DOTALL)
EFFECTIVE = re.compile(r"\beffective-settings\b")
# ".m2" as a path component, then the end of the path or a "/" not followed by "repository/".
M2_OUTSIDE_REPOSITORY = re.compile(
    r"(?:^|[\s'\"=:(`/~}])\.m2(?:$|[\s'\"*?\[{;|&)`]|/(?!repository(?:/|$|[\s'\";|&)`]))|/repository/(?:\S*/)?\.\.)",
    re.MULTILINE)

try:
    event = json.load(sys.stdin)
except ValueError:
    sys.exit(0)  # never block on a malformed event
command = (event.get("tool_input") or {}).get("command") or ""
if NAMES_SETTINGS.search(command) or EFFECTIVE.search(command) or M2_OUTSIDE_REPOSITORY.search(command):
    print("~/.m2 outside its repository/ directory holds the user's credentials (settings.xml): never print, "
          "change, search or remove it, and never name it in a command. Maven reads it without being told to; "
          "ask the user if something in it seems wrong.",
          file=sys.stderr)
    sys.exit(2)
sys.exit(0)
