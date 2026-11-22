# Troubleshooting the development environment

## First steps

```sh
java -version              # 25
mvn -version               # 3.9 or later
mvn -B clean
scripts/verify.sh
```

## Setup

### Maven cannot find `com.soliscode:soliscode-parent:0.1.14`, or `soliscode-performance-api`

**Cause:** the parent is not in `~/.m2`. No SolisCode release is deployed before 1.0.0, and the POM's empty `<relativePath/>` keeps Maven from looking in a sibling directory. `com.soliscode:soliscode-performance-api` is installed by the parent's release, so it is missing for the same reason.
**Fix:** install the parent at 0.1.14 as its guide says, in [Installing a release on another machine](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/08-maintainers.md#installing-a-release-on-another-machine).

### `verify.sh` refuses to run

**Cause:** `MAVEN_ARGS` is set, or a `-D` is set in `MAVEN_OPTS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS` or `JDK_JAVA_OPTIONS`, which would change the build without showing it.
**Fix:** unset the variable it names, and run it again.

### A push to `main` is refused

**Cause:** the pre-push hook found a commit whose tree the pre-commit hook did not build: one made with `--no-verify`, or before the hooks were installed.
**Fix:** install the hooks (`scripts/install-git-hooks.sh`), and amend the commit without `--no-verify`, so that the pre-commit hook builds it.

## Build

### A gate fails the build

**Cause:** the code breaks one of the parent's rules.
**Fix:** find the gate's message in the output. The parent's [Troubleshooting](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/06-troubleshooting.md) page explains each one, and how to fix it. Don't turn the gate off: the switches that would are on [`CLAUDE.md`](../../CLAUDE.md)'s list, and need the maintainer's approval, and the build refuses a change to a gate's plugin. If it fires on code your change did not touch, say so on the issue ([Coding standards](contributing/coding-standards.md#the-gates)).

### The pre-commit hook fails, or two builds clash

**Cause:** the hook starts Maven. Two Maven builds at once share `target/` and `~/.m2`, and break each other: a build fails with missing classes, or a test with a file that another build deleted.
**Fix:** wait for the other build to end, then commit again, or run the build again. Never start a second build while one runs in the background.

### `verify.sh` is green but records nothing

**Cause:** the working copy changed while the build ran (an edit, or an editor writing `.settings/` or `.classpath`), so the tree it built is not the tree that is there. The record is keyed by the tree.
**Fix:** run it again, and change nothing until it ends. Run it last, once nothing else is to change.

### The install gate passes, but the full verify fails the architecture rules

**Cause:** the architecture gate runs at `prepare-package`, with the tests, so neither `mvn test` nor the install gate (`-DskipTests`) runs it.
**Fix:** run `mvn -B verify`, and fix what it reports ([the parent's architecture page](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/03-gates/architecture.md)). A new package dependency that makes a cycle is the usual cause ([Code layout](architecture/code-layout.md#dependency-rules)).

## Tests

### A test is slower than it should be

**Cause:** the coverage agent, which every build runs unless it is given `-Djacoco.skip=true`, slows the tests, the thread-safety ones most.
**Fix:** measure a test's time with `-Djacoco.skip=true`, in a run of that test alone ([Running tests](getting-started/running-tests.md#measuring-test-time)), never in a full verify.

### A thread-safety test times out, or misses a break, on a busy machine

**Cause:** under heavy load (other builds, other projects' builds) the threads of a contention test are not scheduled together, and a run takes longer than its bound, or a race the test relies on does not happen. [build-and-test.md](../design/reference/build-and-test.md#contract-coverage), "Contract coverage", records which tests were measured under load, and how they did.
**Fix:** run it again on its own, on a quieter machine. If it fails again, it is a finding: report it on an issue with the load average, and don't raise its bound to make it pass.

### The build refuses Surefire's `argLine`

**Cause:** a plain `<argLine>` in Surefire's configuration would replace JaCoCo's agent, and the coverage check would then pass without measuring anything. The build refuses one at `validate`.
**Fix:** put extra JVM flags in the `argLine` property, or after `@{argLine}` ([the parent's page](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/04-everyday-work.md#extra-jvm-flags-for-your-tests)).

## Docs build

### A class or method is missing from the API reference

Check that it is public or protected, and has a comment. See [Documentation standards](contributing/documentation.md#the-api-reference). Build the Javadoc with `mvn -B site`, and look in `target/site/en/apidocs/`.

## Still stuck?

Ask in an [issue](https://gitlab.com/soliscode/soliscode-test/-/issues), with the output of `java -version`, `mvn -version` and the failing build.

## See also

- [Development environment](getting-started/dev-environment.md)
- [Building](getting-started/building.md)
