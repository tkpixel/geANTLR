## 2024-04-01 - [Multi-Pass Dynamic Grammar Loading]
**Learning:** `GrammarLoaderService.java` implements a robust multi-pass process for dynamically loading ANTLR grammars at runtime, handling split (Lexer/Parser) grammars, combined grammars, and import directories via an inferred Lexer generation and generic tool resolution.
**Action:** Document the grammar loading sequence in `docs/grammar_loading_workflow.md` and `docs/diagrams/grammar_loading.puml`.
