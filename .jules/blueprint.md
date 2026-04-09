## 2024-04-01 - [Multi-Pass Dynamic Grammar Loading]
**Learning:** `GrammarLoaderService.java` implements a robust multi-pass process for dynamically loading ANTLR grammars at runtime, handling split (Lexer/Parser) grammars, combined grammars, and import directories via an inferred Lexer generation and generic tool resolution.
**Action:** Document the grammar loading sequence in `docs/grammar_loading_workflow.md` and `docs/diagrams/grammar_loading.puml`.
## 2024-04-05 - [Self-Correcting LLM Rule Generation]
**Learning:** `RuleGenerationService.java` and `AntlrValidationTool.java` implement an Agentic Loop (using LangChain4j and ADK) where an LLM drafts DSL code and iteratively validates it against the actual ANTLR parser, self-correcting based on customized, token-aware syntax error feedback until successful.
**Action:** Documented this workflow in `docs/rule_generation_workflow.md` and `docs/diagrams/rule_generation.puml` to explain how the Agentic Loop interacts with the ANTLR runtime.
## 2024-04-09 - [Dynamic Syntax Highlighting Architecture]
**Learning:** `TokenHighlightMappingService` handles the conversion of ANTLR token attributes (symbolic name, literal, etc.) into CSS classes mapped to Darcula colors. `EditorViewModel` manages the background parsing and style building, while `EditorViewController` dynamically builds `RichParagraph`s using the resulting `TokenStyle` records to render styling into the `CodeArea`.
**Action:** Documented the full MVVM pipeline for syntax highlighting in `docs/syntax-highlighting.md` and added a sequence diagram `docs/diagrams/syntax_highlighting.puml`.
