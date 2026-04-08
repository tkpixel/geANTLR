## 2024-04-01 - [Multi-Pass Dynamic Grammar Loading]
**Learning:** `GrammarLoaderService.java` implements a robust multi-pass process for dynamically loading ANTLR grammars at runtime, handling split (Lexer/Parser) grammars, combined grammars, and import directories via an inferred Lexer generation and generic tool resolution.
**Action:** Document the grammar loading sequence in `docs/grammar_loading_workflow.md` and `docs/diagrams/grammar_loading.puml`.
## 2024-04-05 - [Self-Correcting LLM Rule Generation]
**Learning:** `RuleGenerationService.java` and `AntlrValidationTool.java` implement an Agentic Loop (using LangChain4j and ADK) where an LLM drafts DSL code and iteratively validates it against the actual ANTLR parser, self-correcting based on customized, token-aware syntax error feedback until successful.
**Action:** Documented this workflow in `docs/rule_generation_workflow.md` and `docs/diagrams/rule_generation.puml` to explain how the Agentic Loop interacts with the ANTLR runtime.
## 2024-04-08 - [Non-Destructive Code Formatting]
**Learning:** `CodeFormattingService.java` implements non-destructive code formatting using ANTLR's `TokenStreamRewriter` and `ParseTreeWalker`. Instead of recreating the source string from the AST (which loses comments and original layout), it selectively modifies the hidden token channels (whitespace) to enforce indentation and spacing rules, preserving the exact original structure of visible tokens.
**Action:** Documented this pattern in `docs/code_formatting_workflow.md` and `docs/diagrams/code_formatting.puml` to explain the interaction between the Parser, Walker, and Rewriter.
