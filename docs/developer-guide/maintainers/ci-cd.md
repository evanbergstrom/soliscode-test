# CI

SolisCode Test has **no pipeline**. The gates and the tests run on the developer's machine, with `scripts/verify.sh`, whose green record a release needs. No merge request, push or schedule starts a pipeline, and the repository has no `.gitlab-ci.yml`: the old one, which included a file the SolisCode parent does not have, failed before any job ran, and was removed (#11).

## What takes CI's place

| Check | Where it runs | What it guards |
|---|---|---|
| The pre-commit hook | Your machine, on each commit that changes `src/`, `pom.xml`, `.mvn/` or `.errorprone` | The gates before the tests: the enforcer, the licence check, Checkstyle, ErrorProne and NullAway |
| The pre-push hook | Your machine, on a push to `main` | Refuses a commit whose tree the pre-commit hook did not build (one made with `--no-verify`) |
| `scripts/verify.sh` | Your machine, before a branch is reviewed and before a release | Every gate and every test, from clean, recorded for the tree |
| `release.sh` | The maintainer's machine | Refuses to release a tree without a green record |

[build-and-test.md](../../design/reference/build-and-test.md#git-hooks), "Git hooks" and "The record of a green build", has the detail.

## The Javadoc site

The Javadoc is not published yet. Build it with `mvn -B site`, into `target/site/en/apidocs/`. Publishing it to the project's GitLab Pages, with the SolisCode parent's `scripts/publish-javadoc.sh` and a pages-only `.gitlab-ci.yml`, is [#77](https://gitlab.com/soliscode/soliscode-test/-/issues/77); this page will then describe that one pipeline, which builds nothing and runs no gate.

## Secrets

No secret is stored in the project, and none is needed: nothing runs on GitLab.

## Dependency updates

No bot opens update requests. Versions of plugins and managed libraries come from the SolisCode parent, and change when `<parent>` moves to a new release ([Moving to a new release](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/05-new-release.md); in this project the parent's release is in five places, which [`CLAUDE.md`](../../../CLAUDE.md), "Moving the pin", lists). The vulnerability check runs on request: `mvn -B -P dependency-check -DskipTests verify`, with an NVD API key in `NVD_API_KEY` ([the parent's dependency check](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.14/docs/guide/03-gates/dependency-check.md)). It has not been run here yet.

## See also

- [Release process](release-process.md)
- [Running tests](../getting-started/running-tests.md)
