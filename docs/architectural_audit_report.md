🏛️ Kay's Architectural Audit Report
Date: 2024-04-02

1. 🧹 Dead Code & Clutter
Unused Imports:
* `MainViewModel.java`: `import org.geantlr.services.TokenHighlightMappingService;`
* `MainViewModel.java`: `import javafx.beans.property.IntegerProperty;`
* `MainViewModel.java`: `import javafx.beans.property.SimpleIntegerProperty;`

Unused Methods:
* `TokenHighlightMappingService.java`: Empty constructor `public TokenHighlightMappingService() {}` can be safely removed.

2. 🧼 Clean Code Violations
`EditorViewModel.java`: The `computeTokenStyles` method contains complex line splitting and loop logic for multi-line tokens instead of utilizing a cleaner, more readable tokenizer approach or stream abstractions.
`TokenHighlightMappingService.java`: The `buildVocabularyMapping` method uses deep nesting and repeatedly performs `contains()` and string replacements on `symbolicName` inside a loop. This logic is overly verbose and hard to read.

3. 📐 MVVM Violations
`MainViewModel.java`: Unused import of `TokenHighlightMappingService` breaks separation of concerns. The ViewModel layer should have zero knowledge of UI styling or CSS mapping services.
`MainViewController.java`: The controller listens directly to the asynchronous `Task<DynamicGrammar>` and retrieves the domain data (`task.getValue()`). Ideally, the `MainViewModel` should handle the successful task completion internally and update an observable `DynamicGrammar` property that the View binds to or observes.

4. 🚀 Static Analysis & Modernization
`TokenHighlightMappingService.java`: Deep `if/else if` chains on `symbolicName.contains(...)`.
-> *Modernization:* Utilize Java enhanced switch statements or at least `var` type inference:
```java
// Old code
String name = symbolicName.toUpperCase();
if (name.endsWith("_KW") || name.endsWith("_KEYWORD")) { ... }

// Modern approach
var name = symbolicName.toUpperCase();
cssClass = switch (name) {
    case String n when n.endsWith("_KW") || n.endsWith("_KEYWORD") -> "keyword";
    case String n when n.contains("COMMENT") -> "comment";
    case String n when n.contains("INT") || n.contains("FLOAT") -> "number";
    case String n when n.contains("STRING") || n.contains("LITERAL") -> "string";
    default -> null;
};
```

`EditorViewModel.java`: Legacy HashMap instantiation.
-> *Modernization:*
```java
// Old code
java.util.Map<Integer, java.util.List<TokenStyle>> styles = new java.util.HashMap<>();

// Modern approach
var styles = new java.util.HashMap<Integer, java.util.List<TokenStyle>>();
```

5. 📦 Dependency Audit (pom.xml)
* `org.junit.jupiter:junit-jupiter-api` is at `5.10.1` -> Consider updating to `5.11.x`.
* `org.mockito:mockito-core` is at `5.11.0` -> Consider updating to `5.14.x`.
* `org.assertj:assertj-core` is at `3.25.3` -> Consider updating to `3.26.x`.
* `org.apache.maven.plugins:maven-compiler-plugin` is correctly set to release `25`.
* `org.jacoco:jacoco-maven-plugin` is skipped due to Java 25 compatibility issues, which is acceptable but should be monitored as Java 25 matures.

6. 🧪 Testability & Coverage
Missing test coverage identified for the following critical classes:
* `MainViewModel.java`
* `TokenHighlightMappingService.java`
* `CodeCompletionService.java`
* `AntlrGrammarService.java`
* `CodeFormattingService.java`
* `RuleGenerationService.java`

🏛️ Kay's Final Verdict
The codebase demonstrates a solid foundational attempt at the MVVM pattern utilizing modern JavaFX and Micronaut, but it is marred by superficial layer bleeding—most notably View-centric styling concerns creeping into ViewModels and tight coupling in task handling. While the use of Java 25 virtual threads for async logic is commendable, the heavy reliance on procedural, heavily nested parsing logic in the services betrays a lack of modern declarative programming. Strict enforcement of test coverage on the ViewModels and a refactoring pass to eradicate legacy syntax will elevate this architecture from functional to robust.
