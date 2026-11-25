# Documentation standards

Documentation is part of the change, not a follow-up. A change that a user sees is not complete without the user guide page for it, and a change to how the library is built is not complete without the developer guide page for it. No gate checks this; the review does.

## Where documentation lives

| Kind | Where | Written | For |
|---|---|---|---|
| README | `README.md` | By hand, with its example as a test class in `src/test/java/com/soliscode/test/guide/` | Someone deciding whether to use the library |
| User guide | `docs/user-guide/` | By hand, in Markdown, with its examples as test classes in `src/test/java/com/soliscode/test/guide/` | Users |
| **API reference** | `target/site/en/apidocs/`, built with `mvn -B site`, and each release's published at [https://soliscode.gitlab.io/soliscode-test/latest/](https://soliscode.gitlab.io/soliscode-test/latest/) | **Generated from the Javadoc comments** | Users |
| Developer guide | `docs/developer-guide/` | By hand, in Markdown | Contributors and maintainers |
| Design | `docs/design/` | By hand, in Markdown, with each task | Contributors and reviewers |
| Agent instructions | `CLAUDE.md`, `AGENTS.md`, `.claude/` | By hand, with each task that changes what they say | Claude Code and Codex |
| Releases | `RELEASES.md` | Under `## Unreleased` by each task; each release's entry by `release.sh` | Everyone |

When the docs and the code disagree, the code says what exists. Fix the document in the task that finds it.

## The API reference

The API reference is the Javadoc, built from the comments in the source. **Never edit the generated output.**

### What the Javadoc gate checks

The [Javadoc gate](https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/03-gates/javadoc.md) runs the `javadoc` tool on the main code with every `doclint` check on, and fails the build on any warning. So every public and protected class, constructor, method and field has a comment, with an `@param` for each parameter, an `@return` and an `@throws` where they apply, valid HTML and links that resolve. Test code is not checked.

### What a good comment has

The gate checks the form. The content is up to you:

- A first sentence that says what the element does, ending with a full stop. It is the summary in the Javadoc's tables.
- More description where the behaviour is not obvious: what it does with `null`, the edge cases, what it costs.
- What each parameter means, and its limits.
- Each exception it throws, and when.
- An example, for an API that is not obvious.
- `@deprecated` with what to use instead, beside `@Deprecated(since = "<version>", forRemoval = <true or false>)`.
- `@apiNote`, `@implSpec`, `@implNote` and `@complexity` where they help. The gate knows these four tags besides the standard ones.
- For a contract: where it reads the JDK's documentation more strictly or more loosely. For a `Break` constant: what normal behaviour it overrides, and how.

Every main package has a `package-info.java` with a comment of its own, which says what the package holds, and marks it `@NullMarked`. A new package gets one. Test packages have none, except `contract.coverage` and `guide`.

### Comment style

Write Markdown `///` comments, as the SolisCode libraries do. Classic `/** … */` comments pass the gate too.

```java
/// Returns the greeting for a name.
///
/// @param name the name to greet
/// @return the greeting
/// @throws IllegalArgumentException if `name` is blank
public String greet(final String name) {
    if (name.isBlank()) {
        throw new IllegalArgumentException("name is blank");
    }
    return "Hello, " + name;
}
```

### Checking it

```sh
mvn -B verify   # the Javadoc gate, with the rest of the build
mvn -B site     # the Javadoc, in target/site/en/apidocs/
```

## The user guide

The user guide follows [Diátaxis](https://diataxis.fr/). Before you write, decide which kind of page you need:

| If the reader wants to… | Write a… | In | Template |
|---|---|---|---|
| Learn by doing | Tutorial | `getting-started/` | follow `first-steps.md` |
| Do a task | How-to guide | `how-to/` | `how-to/_template.md` |
| Look up facts | Reference | `reference/` | match the page that is there |
| Understand why | Explanation | `concepts/` | `concepts/_template.md` |

Don't mix the kinds. If a how-to starts explaining background, move that to a concept page and link to it.

A page's example, and the README's, is a test class in `src/test/java/com/soliscode/test/guide/`, which the build compiles and runs (the performance scenario it only compiles). The page shows the class's code, so change the class and its page together.

## Markdown conventions

- One H1 for each file, matching the page's title.
- Relative links between pages. Link to the Javadoc for the detail of a class or method, through `https://soliscode.gitlab.io/soliscode-test/latest/`. A class newer than the latest release is not there yet: name it in code font, and say which package it is in.
- Link to the SolisCode parent's guide at the parent's tag, `https://gitlab.com/soliscode/soliscode-parent/-/blob/v0.1.17/docs/guide/...`, rather than describing a gate again. When `<parent>` moves to a new release, move these links with it.
- A language on every fenced code block. Commands use `sh`, output uses `text`.
- Diagrams in Mermaid, in fenced `mermaid` blocks, kept small. No semicolons in a diagram: Mermaid reads one as the end of a statement.
- Images in `docs/assets/`, with alt text that describes them.
- End every page with a **See also** section.

## Writing style

- Second person ("you"), active voice, present tense.
- Short, plain sentences. Define a term the first time you use it, and add it to the [glossary](../../user-guide/glossary.md).
- Every code sample must work. Compile and run it before the merge, or keep it as a test class.
- Write for readers whose first language is not English: avoid idioms, and the project's own shorthand.

## These pages' templates

These pages were written from the SolisCode documentation templates, `doc-templates/` in [soliscode-parent](https://gitlab.com/soliscode/soliscode-parent), at the revision in `docs/.doc-templates-version`. In Claude Code, the `soliscode:docs` skill applies a newer release's templates when `<parent>` moves.

## See also

- [API reference](../../user-guide/reference/api.md)
- [Contribution workflow](workflow.md)
