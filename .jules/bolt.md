## 2024-05-14 - Optimize regex splitting in hot loop
**Learning:** In `EditorViewModel.computeTokenStyles`, calling `text.split("\r?\n", -1)` for every single token introduced severe overhead due to unnecessary regex compilation and array allocations in a hot loop that executes on every keystroke, especially since most tokens are single-line.
**Action:** Implement a fast-path check `text.indexOf('\n') == -1` to avoid the regex evaluation entirely for single-line tokens.
