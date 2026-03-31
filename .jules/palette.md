## 2023-10-27 - [Accessible Icon Buttons]
**Learning:** When using AtlantaFX, removing text from buttons to save space requires adding `atlantafx.base.theme.Styles.BUTTON_ICON` to the style classes. More importantly, removing the text breaks screen reader support, so `setAccessibleText`, `setAccessibleHelp`, and a `Tooltip` must be explicitly added to maintain accessibility and provide visual hover context.
**Action:** When creating icon-only buttons, always apply `Styles.BUTTON_ICON` and immediately add `setAccessibleText()`, `setAccessibleHelp()`, and `setTooltip()`.

## 2026-03-31 - [Accessible Form Fields]
**Learning:** Standard form fields in FXML, such as `TextField`, may have `promptText` that is visible to users, but it is important to explicitly add `accessibleText` and `accessibleHelp` attributes to these fields and related buttons to ensure screen readers provide full context and guidance.
**Action:** When creating JavaFX forms and dialogs, explicitly set `accessibleText` and `accessibleHelp` attributes on standard form fields and buttons in the FXML.