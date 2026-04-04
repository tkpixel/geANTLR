# 🏛️ Kay's Architectural Audit Report
Date: 2024-04-03

## 1. 🧹 Dead Code & Clutter
**Unused Imports:**
* `src/main/java/org/geantlr/viewmodels/MainViewModel.java`: `javafx.beans.property.IntegerProperty`, `javafx.beans.property.SimpleIntegerProperty`, `org.geantlr.services.TokenHighlightMappingService`
* `src/main/java/org/geantlr/views/MainViewController.java`: `javafx.stage.DirectoryChooser`

**Unused Methods:**
* Found no obviously unused methods through initial static analysis, however some internal private helper methods might be lightly used or dead due to FXML reflective injections masquerading as unused code in some static analysis tools. No severe unused public methods found.

## 2. 🧼 Clean Code Violations
* `PlantUmlParsingService.java`:
  - `parseDomainModelViaRegex()` is extremely massive, deeply nested, and performs complex string manipulations which harms readability. It lacks clear documentation about why some of the complex regex patterns are necessary.
  - `cleanMember()` uses a very manual index-based string slicing method instead of simpler Regex or modern string APIs.

## 3. 📐 MVVM Violations
* `src/main/java/org/geantlr/viewmodels/MainViewModel.java`:
  - Imports `org.geantlr.services.TokenHighlightMappingService` which is a View layer styling service handling CSS mappings. Although currently unused, its presence in the ViewModel is a violation.

## 4. 🚀 Static Analysis & Modernization
* `PlantUmlParsingService.java`: `reader.getBlocks().get(0).getDiagram();`
  -> `reader.getBlocks().getFirst().getDiagram();`
* `MainViewController.java`: `viewModel.getActiveEditors().get(0)`
  -> `viewModel.getActiveEditors().getFirst()`

## 5. 📦 Dependency Audit (pom.xml)
* `org.junit.jupiter:junit-jupiter-api` is at 5.10.1, consider updating to 5.11.x
* `org.mockito:mockito-core` is at 5.11.0, consider updating to 5.12.x or later.

## 6. 🧪 Testability & Coverage
Missing test coverage:
* `CodeCompletionService`
* `CodeFormattingService`
* `RuleGenerationService`
* `AntlrGrammarService`

## 🏛️ Kay's Final Verdict
The codebase generally adheres to the MVVM structure, but struggles with clean code in complex parsing services and has notable gaps in test coverage for essential services. Modernization to Java 21+ features (like `.getFirst()`) can further clean up the codebase.
