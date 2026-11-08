"""Change a task issue's description without touching anything else in it.

Run from the repository root:

  python3 -I .claude/skills/gitlab-task/scripts/issue_body.py show <N>
      print the description
  python3 -I .claude/skills/gitlab-task/scripts/issue_body.py tick <N>
      tick every acceptance box ("- [ ]" to "- [x]"); print how many
  python3 -I .claude/skills/gitlab-task/scripts/issue_body.py outcome <N> <file>
      append the file's text as the closing "Outcome" paragraph

Only "- [ ]" becomes "- [x]", inside code blocks too; every other byte stays. Uses glab's stored login.
"""
import json
import subprocess
import sys

PROJECT = "soliscode%2Fsoliscode-test"


def glab(*args: str) -> str:
    r = subprocess.run(["glab", "api", *args], capture_output=True, text=True)
    if r.returncode:
        sys.exit(r.stderr or r.stdout)
    return r.stdout


def description(n: str) -> str:
    return json.loads(glab(f"projects/{PROJECT}/issues/{n}"))["description"] or ""


def put(n: str, text: str) -> None:
    glab("-X", "PUT", f"projects/{PROJECT}/issues/{n}", "-f", f"description={text}")


def main() -> None:
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    action, n = sys.argv[1], sys.argv[2]
    text = description(n)
    if action == "show":
        print(text)
    elif action == "tick":
        count = text.count("- [ ]")
        if count:
            put(n, text.replace("- [ ]", "- [x]"))
        print(f"ticked {count}; {text.count('- [x]') + count} ticked in all")
    elif action == "outcome":
        if len(sys.argv) < 4:
            sys.exit(__doc__)
        with open(sys.argv[3], encoding="utf-8") as f:
            outcome = f.read().strip()
        if not outcome.startswith("Outcome"):
            outcome = "Outcome: " + outcome
        put(n, text.rstrip("\n") + "\n\n" + outcome + "\n")
        print("outcome added")
    else:
        sys.exit(__doc__)


main()
