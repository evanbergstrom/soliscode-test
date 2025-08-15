# SolisCode Test

[![Maven Central](https://img.shields.io/maven-central/v/soliscode/soliscode-test.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/GROUP_ID/ARTIFACT_ID)
[![javadoc](https://javadoc.io/badge2/org.soliscode/soliscode-test/javadoc.svg)](https://javadoc.io/doc/org.soliscode/soliscode-test)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](./LICENSE.txt)

SolisCode Test is a Java library that provides tests of classes that implement the standard 
interfaces in the JDK. this includes all of the collection classes, numeric classes, as well as
other interfaces. 

This library is designed to work exclusively with JUnit5, and requires it at compile time. It is meant to be used in
the test scope, unless it is being used by another library that is expanding its functionality to interfaces outside
the JDK.

This library provides support for:\
- **Contracts** - Classes that test the functionality of individual methods in the standard interfaces.
- **Interface Restriction** - WRapper classes that restrict the methods of an object to only those in an interface.
- **Assertions** - Assertions that help test collection methods.
- **Providers** - Provider classes that produce elements for collection tests.
- **Breakables** - Implementations of the standard collection classes hthat can be programmatically broken in various ways.

> [!NOTE]
> This library is currently a work in progress. It currently supports testing for the following
> interfaces:
> - Object 
> - Iterator 
> - Iterable 
> - Collection 
> - SequencedCollection 
> - List 

**Docs:** https://evanbergstrom.github.io/soliscode-test/

**API Reference:** https://evanbergstrom.github.io/soliscode-test/apidocs

---
## Requirements

- Java **23+**
- Build: Maven

---
## Installation

### Maven
```xml
<dependency>
  <groupId>org.soliscode</groupId>
  <artifactId>soliscode</artifactId>
  <version>0.0.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>

Full dependency information available [here][https://evanbergstrom.github.io/soliscode-test/dependency-info.html]