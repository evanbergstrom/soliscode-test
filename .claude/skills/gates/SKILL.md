---
name: gates
description: Run the build and quality gates for what the current branch changed — the install gate (enforcer, licences, Checkstyle, compile with ErrorProne and NullAway, Javadoc) with its ErrorProne count, the tests of the touched classes, and the full verify through scripts/verify.sh (every test, the coverage check, Javadoc) — one Maven build at a time, and report green or the failures. Use before saying a task is done or before "commit and merge".
argument-hint: "[install | verify | <TestClass>…]"
---

# Run the gates

Checkstyle, Javadoc, ErrorProne, NullAway and the JaCoCo coverage check are gates, not advisories: a finding is a failure, like a compile error. Never pass `-Dcheckstyle.skip`, `-P '!error-prone'` or an ErrorProne option that demotes or turns off a check, `-Dmaven.test.skip`, `-Dmaven.test.failure.ignore` or `-Djacoco.haltOnFailure=false` (`-DskipTests` is fine for the install gate), and add no per-file suppression or `@SuppressWarnings` beyond what `java-conventions.md`, "Suppressions", allows, unless the user approves it in this session. If a gate fires on code the branch did not touch, stop and tell the user which gate, file and count, and offer: (a) fix in line, (b) file a clean-up task (recommended), (c) a narrow suppression paired with a filed task to remove it.

**Which gates exist today.** `CLAUDE.md`, "Static-analysis gates", marks each gate Built or Designed with the issue that builds it. Run what is built; for a gate still Designed, say in the report that it does not run yet. Don't treat a Designed gate's silence as a pass.

**Never run two Maven builds at once.** They share `target/` directories and `~/.m2`. Run each in the background with output to a log in the scratchpad, and wait for the notification.

1. **What changed**: `git diff --name-only main...HEAD` plus `git status --short`.
2. **Select**:

   | Changed | Run |
   |---|---|
   | Markdown, `.claude/**` only | Nothing from Maven; check links, and that the commands CLAUDE.md and build-and-test.md give still exist |
   | Java in a few classes, while working | The install gate, then the tests of those classes and of what tests them (`mvn -B -Dtest='Class1Test,Class2Test' test`; a contract or a breakable is also tested by `ContractCoverageTest`) |
   | Any Java, before "commit and merge"; `pom.xml`, `.mvn/**`, `.errorprone`, `scripts/**` | The install gate, then the full verify |

3. **Commands**:

   ```bash
   # The install gate: the enforcer, the licence gate and Checkstyle (validate), compile everything, tests compiled but not run,
   # ErrorProne and NullAway (compile, main sources, Built #9), Javadoc (verify). Checkstyle passes (Built #10): a finding
   # fails the gate like any other.
   # Checkstyle findings print as [ERROR] <file>:<line>:<col>: ... [Rule]; Javadoc's as <file>:<line>: warning|error:
   mvn -B -Djacoco.check.skip=true -DskipTests install > <log> 2>&1; echo "exit $?"
   grep -c 'errorprone.info' <log>    # ErrorProne findings print with this link: must be 0. javac stops at 100 warnings and 100 errors, so fix and re-run

   # The full verify, from clean: Checkstyle, every test, the coverage check, Javadoc.
   # verify.sh writes its own log and, when green, the record release.sh needs.
   scripts/verify.sh > <log> 2>&1; echo "exit $?"
   tail -3 <log>; grep -E 'Tests run:.*Fail|BUILD|ERROR|Rule violated' "$(git rev-parse --git-common-dir)/soliscode/verify.log" | tail -20
   ```

   Run the full verify last, once nothing else is to change: its record is keyed by the working copy's tree, so an edit after it leaves the merge without a record. A coverage check that fails (`Rule violated for bundle soliscode-test: lines covered ratio is ...`) is a gate failure: add tests, never lower `jacoco.minimum.coverage` without the user's word.

4. **Report** each gate green or red with its first failing lines and the ErrorProne count. Re-run a failed test once on its own (`mvn -B -Dtest='Class#method' test`); if it then passes, report it as flaky with the failure, never as green. A thread-safety test that fails once in many runs is a finding, not noise.
