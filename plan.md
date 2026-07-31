1. **Map:** The `TokenHighlightMappingService`, `EditorViewModel`, and `EditorViewController` interact to provide dynamic syntax highlighting using a strict MVVM pattern. The documentation exists but can be improved with a sequence diagram.
2. **Draft:** I will create a `syntax_highlighting.puml` file showing the asynchronous text parsing, token style computation, and UI rendering. I will update `docs/syntax-highlighting.md` to link to this diagram. I will add an entry to `.jules/blueprint.md` documenting this workflow.
3. **Verify:** The diagram strictly reflects the code (using actual method names like `parseText`, `computeTokenStyles`, and `createSyntaxDecorator`). I'll run `plantuml.jar` to ensure the SVG compiles correctly.
4. **Pre-commit:** I will run `pre_commit_instructions` to ensure proper checks are run.
5. **Submit:** I will submit a PR with the '📏 Blueprint: [Added diagram/doc for syntax highlighting workflow]' format.
