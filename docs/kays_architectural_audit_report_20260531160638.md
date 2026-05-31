🏛️ Kay's Architectural Audit Report
Date: $(date +%Y-%m-%d)

1. 🧹 Dead Code & Clutter
Unused Imports:
- `org.geantlr.viewmodels.MainViewModel`: `import javafx.beans.property.IntegerProperty;`, `import javafx.beans.property.SimpleIntegerProperty;`
- `org.geantlr.viewmodels.MainViewModel`: `import org.geantlr.services.TokenHighlightMappingService;`

Unused Methods:
- None found explicitly, but several model accessors in ViewModels appear unused within the provided FXML scopes.

2. 🧼 Clean Code Violations
MainViewController:
- Uses hardcoded integers like `400, 250` and `600, 800` for dialog sizes instead of named constants.

3. 📐 MVVM Violations
MainViewModel:
- Imports `TokenHighlightMappingService` (a UI/styling mapping service), which shouldn't be injected or known to the ViewModel.
EditorViewController:
- Contains significant logic for parsing nodes (`findTextFlow`, `updateBracketLine`) which belongs to a custom control or UI helper, cluttering the view controller.

4. 🚀 Static Analysis & Modernization
PlantUmlParsingService.java:
```java
var diagram = reader.getBlocks().get(0).getDiagram();
```
->
```java
var diagram = reader.getBlocks().getFirst().getDiagram();
```

MainViewController.java:
```java
saveEditorContent(viewModel.getActiveEditors().get(0));
```
->
```java
saveEditorContent(viewModel.getActiveEditors().getFirst());
```

5. 📦 Dependency Audit (pom.xml)
- `jacoco-maven-plugin`: Currently `0.8.12`. Must be updated to `0.8.13` or higher to support Java 25 class files.
- `junit-jupiter-api`: Currently `5.10.1`, could be updated.
- `mockito-core`: Currently `5.11.0`, could be updated.

6. 🧪 Testability & Coverage
The following critical public classes and ViewModels lack corresponding tests in `src/test/java`:
- `org.geantlr.viewmodels.MainViewModel`
- `org.geantlr.services.AntlrGrammarService`
- `org.geantlr.services.TokenHighlightMappingService`
- `org.geantlr.services.CodeFormattingService`
- `org.geantlr.services.RuleGenerationService`
- All `*Controller` classes.

🏛️ Kay's Final Verdict
The codebase demonstrates a good understanding of MVVM, but contains significant test coverage gaps, especially for the core ViewModels and Services. The use of Java 25 is forward-looking, but requires immediate modernization of `.get(0)` to `.getFirst()` and a bump to the JaCoCo plugin to prevent build failures. Additionally, UI styling services must be strictly decoupled from the ViewModels to satisfy pure MVVM separation.
