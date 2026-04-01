## 2024-05-24 - [Avoid Regex on Single-Line Tokens]
**Learning:** During syntax highlighting in `EditorViewModel.computeTokenStyles`, performing `String.split("\r?\n", -1)` on every single token causes heavy regex compilation and array allocation overhead, especially because the vast majority of tokens are single-line strings.
**Action:** Use a fast-path check `text.indexOf('\n') == -1` to completely bypass regex processing and array allocation for single-line tokens.
