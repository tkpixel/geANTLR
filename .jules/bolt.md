## 2024-05-24 - [Avoid Regex on Single-Line Tokens]
**Learning:** During syntax highlighting in `EditorViewModel.computeTokenStyles`, performing `String.split("\r?\n", -1)` on every single token causes heavy regex compilation and array allocation overhead, especially because the vast majority of tokens are single-line strings.
**Action:** Use a fast-path check `text.indexOf('\n') == -1` to completely bypass regex processing and array allocation for single-line tokens.
## 2026-05-31 - [Memory leaks via unbounded JavaFX Listeners]
**Learning:** Attaching anonymous `ListChangeListener`s (like `Scene.getStylesheets().addListener()`) from short-lived nodes (like temporary Dialog stages) to long-lived objects (`Scene`) creates strong references that prevent garbage collection of the short-lived nodes, leading to memory leaks.
**Action:** Always store a strong reference to the listener and explicitly remove it when the short-lived node's lifecycle ends (e.g. `stage.setOnHidden(...)` or `sceneProperty().addListener((_, oldScene, _) -> oldScene...removeListener(...))`).
