## 2026-03-26 - PMD False Positives on FXML
**Learning:** PMD's `UnusedPrivateMethod` and `UnusedPrivateField` rules flag `@FXML` annotated methods and fields (e.g., in `MainViewController`) as unused because they are injected/invoked reflectively by JavaFX `FXMLLoader` and not called directly in Java code.
**Action:** Ignore these specific PMD warnings for `@FXML` annotated members. Do not delete or change `@FXML` members just because PMD flags them as unused.
