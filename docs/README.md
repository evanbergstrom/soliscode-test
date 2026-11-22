# SolisCode Test documentation

## I want to…

| Goal | Go to |
|---|---|
| Try SolisCode Test for the first time | [Getting started](user-guide/getting-started/installation.md) |
| Do a specific task | [How-to guides](user-guide/how-to/README.md) |
| Look up a setting or an error | [Reference](user-guide/reference/configuration.md) |
| Understand how it works and why | [Concepts](user-guide/concepts/README.md) |
| Look up a class or a method | [API reference](user-guide/reference/api.md) (the packages, and how to build the Javadoc) |
| Move to a newer release | [Upgrading](user-guide/upgrading.md) |
| Build from source or contribute | [Developer guide](developer-guide/README.md) |
| Read why the library is built the way it is | [Design documents](design/) |

## Guides

- **[User guide](user-guide/README.md):** using and configuring SolisCode Test
- **[Developer guide](developer-guide/README.md):** building, the code layout, contributing and maintaining
- **[Design documents](design/):** the [constitution](design/system/constitution.md) (what the library is, its goals and its rules), the [architecture](design/system/architecture.md), and the reference documents on [Java conventions](design/reference/java-conventions.md), [building and testing](design/reference/build-and-test.md) and [dependencies](design/reference/dependency-management.md)

## Which version these pages describe

These pages describe `main`, the next release.

The Javadoc is not published yet. Build it from a clone with `mvn -B site`, which writes it to `target/site/en/apidocs/`. Publishing it to GitLab Pages is [#77](https://gitlab.com/soliscode/soliscode-test/-/issues/77).

The pages as they were at a release are in the repository at its tag, `v<version>`. What changed in each release is in [RELEASES.md](../RELEASES.md).

## See also

- [README](../README.md)
- [Contributing](../CONTRIBUTING.md)
