## 2024-05-24 - [Avoid Regex on Single-Line Tokens]
**Learning:** During syntax highlighting in `EditorViewModel.computeTokenStyles`, performing `String.split("\r?\n", -1)` on every single token causes heavy regex compilation and array allocation overhead, especially because the vast majority of tokens are single-line strings.
**Action:** Use a fast-path check `text.indexOf('\n') == -1` to completely bypass regex processing and array allocation for single-line tokens.

## 2026-04-14 - Fix Scene Listener Memory Leaks
**Learning:** In JavaFX, adding an anonymous or lambda listener to `Scene.getStylesheets()` inside an element's `sceneProperty()` listener (or any short-lived UI dialog) creates a hard reference from the globally long-lived `Scene` back to the short-lived node/dialog. Because `getStylesheets().addListener(...)` expects a `ListChangeListener`, which is strongly held by the observable list, the entire Controller/UI (and all its bindings) is kept alive in memory forever unless explicitly removed.
**Action:** Always store `ListChangeListener`s (like stylesheet synchronizers) in strongly-referenced class fields and explicitly remove them using `.removeListener()` when the node's `sceneProperty` changes or when the dialog window/Stage is closed (`stage.setOnHidden(...)`).
