# Installation

## Requirements

| Requirement | Version | Notes |
|---|---|---|
| Java | 25 | A JDK on the `PATH` |
| Maven | 3.9 or later | |
| JUnit | 6 | It comes with SolisCode Test: you need not declare it |
| Operating system | macOS or Linux | |
| Git | any recent version | To check out the release you install |

## Install

Before 1.0.0, no SolisCode release is deployed to a remote repository. You build each one from its release commit and install it into your local Maven repository, `~/.m2/repository`. Do it in this order, since each build needs the one before it.

### 1. The SolisCode parent

SolisCode Test 0.1.6 builds on the SolisCode parent 0.1.12 (`main` builds on 0.1.17). Install the parent as its guide says, in [Installing a release on another machine](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/08-maintainers.md#installing-a-release-on-another-machine), at the version the release you install names. The parent's install also installs `com.soliscode:soliscode-performance-api`, which SolisCode Test depends on.

### 2. SolisCode Test

```sh
git clone https://gitlab.com/soliscode/soliscode-test.git soliscode-test
cd soliscode-test
git checkout 24a68aa
mvn -B -Drevision=0.1.6 install
```

`24a68aa` is the commit of the 0.1.6 release, which [RELEASES.md](../../../RELEASES.md) names in its entry; the release's tag, `v0.1.6`, is not on GitLab yet. The POM at a release says `0.1-SNAPSHOT`. `-Drevision=0.1.6` gives the build the release's own version, so that it installs `0.1.6`. The build runs every test and gate first, and takes about five minutes.

The project is public, so anyone can clone it over HTTPS. With a GitLab key you can clone over SSH instead: `git clone git@gitlab.com:soliscode/soliscode-test.git soliscode-test`.

### 3. Add it to your POM

SolisCode Test is for your tests, so add it in the `test` scope:

```xml
<dependency>
  <groupId>com.soliscode</groupId>
  <artifactId>soliscode-test</artifactId>
  <version>0.1.6</version>
  <scope>test</scope>
</dependency>
```

It brings JUnit 6 (`junit-jupiter`, with its engine and `junit-jupiter-params`), JSpecify, OpenTest4J, HdrHistogram and `soliscode-performance-api`, so your tests need no other dependency to run a contract. Maven's Surefire runs them as it runs any JUnit test.

A dependency does not bring its build settings. If your project is not on the SolisCode parent, which sets both, compile for Java 25, and use a Surefire that runs JUnit 6 (3.6.0 does; the version Maven picks by default depends on your Maven):

```xml
<properties>
  <maven.compiler.release>25</maven.compiler.release>
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>3.6.0</version>
    </plugin>
  </plugins>
</build>
```

### From source

To build `main` rather than a release, see [Building](../../developer-guide/getting-started/building.md) in the developer guide. `main` installs as `0.1-SNAPSHOT`.

## Verify the installation

```sh
ls ~/.m2/repository/com/soliscode/soliscode-test/0.1.6/
```

It lists the installed jar and POM, beside Maven's own bookkeeping file:

```text
_remote.repositories
soliscode-test-0.1.6.jar
soliscode-test-0.1.6.pom
```

## Uninstall

```sh
rm -rf ~/.m2/repository/com/soliscode/soliscode-test/
```

That removes every version you installed. The clone you built it from stays where you made it; delete it too if you no longer need it.

## See also

- [First steps](first-steps.md)
- [Troubleshooting](../troubleshooting.md#installation)
