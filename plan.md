1. **Create `MessageDialog.fxml`**
   - Place in `src/main/resources/org/geantlr/views/MessageDialog.fxml`.
   - Layout should use `BorderPane` with a `HeaderBar` at the top, a `VBox` in the center containing labels for title, header, and content, and an "OK" button to close it.

2. **Create `MessageDialogController.java`**
   - Place in `src/main/java/org/geantlr/views/MessageDialogController.java`.
   - Inject `@FXML` elements: `titleLabel`, `headerLabel`, `contentLabel`.
   - Add `setDialogInfo(String title, String header, String content)` method to configure text.
   - Add `close()` method to close the dialog stage.
   - Annotate with Micronaut `@Prototype`.

3. **Update `MainViewController.java` to use the Custom Dialog**
   - Add a private helper method `showMessageDialog(String title, String header, String content)` which loads `MessageDialog.fxml`, uses `context.getBean(FxmlControllerFactory.class)` to create the controller, creates a new `Stage`, and applies styles from the current scene.
   - Update `loadGrammar()` success/failure handlers to call `showMessageDialog` instead of using `Alert`.
   - Update `loadGrammar()` exception handler to call `showMessageDialog` instead of using `Alert`.
   - Update `viewGrammar()` exception handler to call `showMessageDialog` instead of using `Alert`.

4. **Update `EditorViewModel.java` for PlantUML Loading Alerts**
   - Currently, `EditorViewModel.java` handles `loadDomainModel()` using `plantUmlParsingService`. It only logs errors (`LOG.severe`) and toggles a parsing property.
   - We need to show an alert to the user. Since `EditorViewModel` is a ViewModel, it shouldn't show UI directly. We should use an event or a callback, or maybe modify `MainViewController` since `loadDomainModel` is triggered from there.
   - Wait, `MainViewController.java` triggers `editor.loadDomainModel(selectedFile)` for all editors:
     ```java
     for (EditorViewModel editor : viewModel.getActiveEditors()) {
         editor.loadDomainModel(selectedFile);
     }
     ```
   - A better approach: Modify `EditorViewModel`'s `loadDomainModel` to return the `Task<Void>`, so `MainViewController` can attach success/failure handlers to show the custom dialog, or we can add properties to `EditorViewModel` for errors.
   - Let's change `EditorViewModel.java`: `public Task<Void> loadDomainModelAsync(java.io.File file)` that returns the task.
   - Then in `MainViewController.loadDomainModel()`: iterate over editors, wait for tasks, or just attach `addEventHandler` to show the custom message dialog on success or failure.

5. **Pre-commit Checks**
   - Run tests, fix PMD / checkstyle, etc.
