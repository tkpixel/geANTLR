## 2023-10-27 - [Accessible Icon Buttons]
**Learning:** When using AtlantaFX, removing text from buttons to save space requires adding `atlantafx.base.theme.Styles.BUTTON_ICON` to the style classes. More importantly, removing the text breaks screen reader support, so `setAccessibleText`, `setAccessibleHelp`, and a `Tooltip` must be explicitly added to maintain accessibility and provide visual hover context.
**Action:** When creating icon-only buttons, always apply `Styles.BUTTON_ICON` and immediately add `setAccessibleText()`, `setAccessibleHelp()`, and `setTooltip()`.
