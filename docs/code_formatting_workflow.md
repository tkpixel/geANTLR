# Code Formatting Workflow

This document outlines the workflow and mechanics of the `CodeFormattingService` in the `geantlr` application.

**Source Code:**
* [`src/main/java/org/geantlr/services/CodeFormattingService.java`](../src/main/java/org/geantlr/services/CodeFormattingService.java)

## Overview

The `CodeFormattingService` provides a generic, grammar-independent mechanism for formatting source code strings. It relies on the generic structural elements common to most programming languages (like braces, brackets, and statement separators) combined with the parsed `CommonTokenStream` produced by ANTLR.

The service performs non-destructive code formatting. It accomplishes this by selectively modifying, inserting, or removing *hidden channel* tokens (whitespace and newlines) around the visible syntax tokens, without altering the underlying logic of the abstract syntax tree.

## Sequence Diagram

The following sequence diagram illustrates the workflow of the `CodeFormattingService`:

![Code Formatting Workflow](diagrams/code_formatting.svg)

*(If viewing the source `.puml`, render using the PlantUML tool.)*

## Key Mechanics

### 1. Token Stream and Rewriter Setup
The input code string is transformed into a `CharStream` and passed through a `LexerInterpreter` to generate a `CommonTokenStream`. This token stream is then processed by a `ParserInterpreter` to build the `ParseTree`.
Crucially, a `TokenStreamRewriter` is instantiated using the `CommonTokenStream`. The rewriter allows the service to buffer modifications to the token stream, rather than altering the string directly or rebuilding it manually.

### 2. The `FormatterListener`
A `ParseTreeWalker` navigates the abstract syntax tree with a custom `FormatterListener` (an implementation of `ParseTreeListener`).

The listener is entirely focused on `visitTerminal(TerminalNode)`. When a terminal node (a token) is visited, it checks the token's text.

#### Indentation Logic
*   **Opening Brackets (`{`, `[`):** Increments the internal `indentLevel`. It inspects the hidden tokens immediately to the *right* of the opening bracket. If a newline and the correct indentation string are not present, it issues an instruction to the `TokenStreamRewriter` to insert or replace them.
*   **Closing Brackets (`}`, `]`):** Decrements the internal `indentLevel`. It inspects the hidden tokens immediately to the *left* of the closing bracket. If a newline and the correct indentation string are not present, the `TokenStreamRewriter` adjusts the whitespace.

#### Statement Separation
*   **Separators (`;`, `,`):** Inspects hidden tokens to the *right*. Ensures a newline and the current `indentLevel` are applied immediately following statement or list separators.

#### Token Spacing
*   **Generic Tokens:** For most other tokens, the listener inspects the next *visible* (non-hidden) token. If the next token is not punctuation (like `;`, `,`, `.`, `)`, `]`, `}`), and there is currently *no* whitespace separating them, it tells the rewriter to insert a single space character.

### 3. Execution and Fallback
Once the `ParseTreeWalker` completes its traversal, the modified text is requested via `rewriter.getText()`.

Finally, a simple regex fallback (`replaceAll("\\n\\s*\\n\\s*\\n", "\n\n")`) is applied to remove excessive consecutive blank lines, which sometimes occur as an artifact of LLM output or the generic rewriter logic.