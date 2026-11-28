# Setting up the development environment

## Required tools

| Tool | Version | Purpose |
|---|---|---|
| Git | any recent version | Version control |
| Java | 25, a JDK on the `PATH` | Compiler and runtime. The build refuses an older one |
| Maven | 3.9 or later | The build. The build refuses an older one |
| macOS or Linux | | The scripts are Bash |
| [Claude Code](https://claude.com/claude-code) | | Optional. The task workflow and the reviews use it, with the SolisCode plugin ([`CLAUDE.md`](../../../CLAUDE.md), "Claude Code setup") |
| [`glab`](https://gitlab.com/gitlab-org/cli), the GitLab command line | any recent version | Optional. The task workflow reads and updates the issues with it |
| Codex | | Optional. The second review of each design and branch ([`AGENTS.md`](../../../AGENTS.md) points it at `CLAUDE.md`) |

## Local setup

1. Install the SolisCode parent at 0.1.19 into your local Maven repository, as its guide says in [Installing a release on another machine](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.19/docs/guide/08-maintainers.md#installing-a-release-on-another-machine). The parent's version is the `<parent>` in `pom.xml`. Its install also installs the parent's performance API jar, `com.soliscode:soliscode-performance-api` 0.1.19, released with the parent and not on its own, which the performance test library uses ([architecture.md](../../design/system/architecture.md), section 8).
2. SolisCode Test depends on no SolisCode library besides the parent's release, so there is nothing more to install first. Every other dependency comes from Maven Central ([dependency-management.md](../../design/reference/dependency-management.md)).
3. Clone this repository, and install its git hooks:

   ```sh
   git clone git@gitlab.com:soliscode/soliscode-test.git
   cd soliscode-test
   scripts/install-git-hooks.sh
   ```

   The project is public: anyone can clone it, over HTTPS (`https://gitlab.com/soliscode/soliscode-test.git`) or, with a GitLab key, over SSH as above. The hooks run when you commit and push. The pre-commit hook compiles the main and test code when a commit changes `src/`, `pom.xml`, `.mvn/` or `.errorprone`, which runs the gates that come before the tests. The pre-push hook refuses a push to `main` of a commit that skipped it. [build-and-test.md](../../design/reference/build-and-test.md#git-hooks), "Git hooks", has the detail.

## Verify your setup

```sh
scripts/verify.sh
```

It runs the full build from clean, with every gate, in about five minutes. It should end with a line that starts `verify.sh: green.`, and it records the green build for the tree, which a release needs. If not, see [Troubleshooting](../troubleshooting.md).

## Editor setup

- Import the project as a Maven project. The editor takes the Java version and the dependencies from the POM.
- The Checkstyle rules are `soliscode/checkstyle.xml` in the jar `com.soliscode:soliscode-build-tools`, at the parent's version, in your local Maven repository. An editor's Checkstyle plugin can read them from there. The build is what counts.
- Lines are at most 120 characters, and every Java file starts with the copyright header ([Coding standards](../contributing/coding-standards.md)). There is no formatter configuration: match the surrounding code.

## Environment variables

Don't set a `-D` in `MAVEN_OPTS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS` or `JDK_JAVA_OPTIONS`, and leave `MAVEN_ARGS` unset. They would change the build without showing it, and `scripts/verify.sh` refuses to run with them set.

**Never commit a secret**, and never print or change `~/.m2/settings.xml`, which may hold credentials.

## See also

- [Building](building.md)
- [Troubleshooting the development environment](../troubleshooting.md)
