## 2024-05-24 - [Avoid ListChangeListener for Full Redraws]
**Learning:** [When observing JavaFX `ObservableList` properties where change details (added/removed items) are not needed (e.g., when the UI triggers a complete redraw or rebuilds its state), using `ListChangeListener` introduces unnecessary CPU overhead for eager difference computation.]
**Action:** [Use `InvalidationListener` instead of `ListChangeListener` when full UI redraws or updates are performed for list changes.]
