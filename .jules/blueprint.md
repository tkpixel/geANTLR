## 2024-04-01 - [Multi-Pass Dynamic Grammar Loading]
**Learning:** `GrammarLoaderService.java` implements a robust multi-pass process for dynamically loading ANTLR grammars at runtime, handling split (Lexer/Parser) grammars, combined grammars, and import directories via an inferred Lexer generation and generic tool resolution.
**Action:** Document the grammar loading sequence in `docs/grammar_loading_workflow.md` and `docs/diagrams/grammar_loading.puml`.
## 2024-04-05 - [Self-Correcting LLM Rule Generation]
**Learning:** `RuleGenerationService.java` and `AntlrValidationTool.java` implement an Agentic Loop (using LangChain4j and ADK) where an LLM drafts DSL code and iteratively validates it against the actual ANTLR parser, self-correcting based on customized, token-aware syntax error feedback until successful.
**Action:** Documented this workflow in `docs/rule_generation_workflow.md` and `docs/diagrams/rule_generation.puml` to explain how the Agentic Loop interacts with the ANTLR runtime.
## 2024-05-31 - [Dynamic Syntax Highlighting & Token Class Mapping]
**Learning:** The dynamic syntax highlighting engine maps generic ANTLR token attributes to UI CSS classes automatically, without explicit grammar definitions. The `TokenHighlightMappingService` caches heuristics-based generic mappings (such as classifying symbol-only literal tokens as 'operator'), and the View uses `RichParagraph` builder segmentation to apply these theme-aware `.css` classes.
**Action:** Documented this mapping flow, added a new sequence diagram `syntax_highlighting.puml`, and updated the `docs/syntax-highlighting.md` file to reflect the architecture accurately.
