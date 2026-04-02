package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.TextPos;
import org.geantlr.services.SyntaxError;
import org.geantlr.services.TokenHighlightMappingService;
import org.geantlr.viewmodels.EditorViewModel;
import org.antlr.v4.runtime.Token;

@Prototype
public class EditorViewController {

    private static final Logger LOG = Logger.getLogger(EditorViewController.class.getName());

    @FXML
    private CodeArea editorCodeArea;

    @FXML
    private javafx.scene.layout.Pane bracketLineOverlay;

    @FXML
    private FlowPane suggestionsPane;

    @FXML
    private VBox experimentalBox;

    @FXML
    private TextArea promptTextArea;

    @FXML
    private Button generateButton;

    @FXML
    private ProgressIndicator generationProgress;

    @FXML
    private Button selectTemplateButton;

    @FXML
    private Label templateNameLabel;

    @FXML
    private javafx.scene.control.ComboBox<String> modelComboBox;

    @FXML
    private javafx.scene.layout.HBox searchBar;

    @FXML
    private javafx.scene.control.TextField searchTextField;

    @FXML
    private javafx.scene.control.ToggleButton searchMatchCaseToggle;

    @FXML
    private javafx.scene.control.ToggleButton searchWholeWordToggle;

    @FXML
    private javafx.scene.control.ToggleButton searchRegexToggle;

    @FXML
    private Label searchCountLabel;

    @FXML
    private Button searchPrevButton;

    @FXML
    private Button searchNextButton;

    @FXML
    private Button searchCloseButton;

    private final javafx.scene.control.Tooltip errorTooltip = new javafx.scene.control.Tooltip();
    private final PauseTransition hoverPause = new PauseTransition(Duration.millis(300));
    private SyntaxError lastHoveredError = null;
    // Screen coordinates stored on mouse-move, used when the pause fires
    private double lastScreenX, lastScreenY;

    private EditorViewModel viewModel;
    private final TokenHighlightMappingService tokenHighlightMappingService;
    private javafx.scene.shape.Line connectionLine;

    @Inject
    public EditorViewController(TokenHighlightMappingService tokenHighlightMappingService) {
        this.tokenHighlightMappingService = tokenHighlightMappingService;
    }

    @FXML
    public void initialize() {
        if (editorCodeArea != null) {
            editorCodeArea.setLineNumbersEnabled(true);

            // Style tooltip via CSS class for theme-aware colours
            errorTooltip.getStyleClass().add("error-tooltip");
            errorTooltip.setWrapText(true);
            errorTooltip.setMaxWidth(500);

            // Fired after the hover delay – show the tooltip if still over an error
            hoverPause.setOnFinished(_ -> {
                if (viewModel == null || editorCodeArea.getScene() == null) return;

                TextPos pos = editorCodeArea.getTextPosition(lastScreenX, lastScreenY);
                if (pos == null) {
                    LOG.warning("[Hover] getTextPosition returned null for screen=(" + lastScreenX + "," + lastScreenY + ")");
                    return;
                }

                int antlrLine = pos.index() + 1;
                int antlrChar = pos.offset();
                LOG.info("[Hover] TextPos index=" + pos.index() + " offset=" + pos.offset()
                    + " → ANTLR line=" + antlrLine + " char=" + antlrChar
                    + "  errors=" + viewModel.getErrors().size());
                for (SyntaxError err : viewModel.getErrors()) {
                    LOG.info("[Hover]   error: line=" + err.line() + " charPos=" + err.charPositionInLine()
                        + " len=" + err.length() + " msg=" + err.message());
                }

                SyntaxError error = viewModel.getErrorAt(antlrLine, antlrChar);
                if (error != null) {
                    LOG.info("[Hover] MATCH → showing tooltip: " + error.message());
                    errorTooltip.setText(error.message());
                    errorTooltip.show(editorCodeArea, lastScreenX + 2, lastScreenY + 18);
                } else {
                    LOG.info("[Hover] no match at line=" + antlrLine + " char=" + antlrChar);
                }
            });

            // Shared handler for MOUSE_MOVED and MOUSE_DRAGGED
            javafx.event.EventHandler<MouseEvent> mouseHandler = event -> {
                if (viewModel == null) return;

                double screenX = event.getScreenX();
                double screenY = event.getScreenY();

                TextPos pos = editorCodeArea.getTextPosition(screenX, screenY);

                SyntaxError errorUnderCursor = (pos != null)
                    ? viewModel.getErrorAt(pos.index() + 1, pos.offset())
                    : null;

                if (!Objects.equals(errorUnderCursor, lastHoveredError)) {
                    errorTooltip.hide();
                    hoverPause.stop();
                    lastHoveredError = errorUnderCursor;
                    if (errorUnderCursor != null) {
                        lastScreenX = screenX;
                        lastScreenY = screenY;
                        hoverPause.playFromStart();
                    }
                } else if (errorUnderCursor != null) {
                    lastScreenX = screenX;
                    lastScreenY = screenY;
                }
            };

            editorCodeArea.addEventFilter(MouseEvent.MOUSE_MOVED, mouseHandler);
            editorCodeArea.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseHandler);

            editorCodeArea.addEventFilter(MouseEvent.MOUSE_EXITED, _ -> {
                errorTooltip.hide();
                lastHoveredError = null;
                hoverPause.stop();
            });

            editorCodeArea.sceneProperty().addListener((_, _, newScene) -> {
                if (newScene != null) {
                    newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
                        () -> {
                            if (this.viewModel != null) {
                                this.viewModel.formatCode();
                            }
                        }
                    );
                    newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                        () -> {
                            if (searchBar != null && searchTextField != null) {
                                searchBar.setVisible(true);
                                searchBar.setManaged(true);
                                searchTextField.requestFocus();
                                searchTextField.selectAll();
                            }
                        }
                    );
                    // Tooltip-CSS mit der Scene synchron halten
                    syncTooltipStylesheets(newScene);
                    newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) _ ->
                        syncTooltipStylesheets(newScene));
                }
            });
        }

        if (searchCloseButton != null) {
            searchCloseButton.setOnAction(_ -> {
                closeSearch();
            });
        }

        if (searchTextField != null) {
            searchTextField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    closeSearch();
                    event.consume();
                } else if (event.getCode() == KeyCode.ENTER) {
                    if (event.isShiftDown()) {
                        if (viewModel != null) viewModel.searchPrevious();
                    } else {
                        if (viewModel != null) viewModel.searchNext();
                    }
                    event.consume();
                }
            });
        }

        if (searchPrevButton != null) {
            searchPrevButton.setOnAction(_ -> {
                if (viewModel != null) viewModel.searchPrevious();
            });
        }

        if (searchNextButton != null) {
            searchNextButton.setOnAction(_ -> {
                if (viewModel != null) viewModel.searchNext();
            });
        }
    }

    private void closeSearch() {
        if (searchBar != null) {
            searchBar.setVisible(false);
            searchBar.setManaged(false);
        }
        if (viewModel != null) {
            viewModel.setSearchText("");
        }
        if (editorCodeArea != null) {
            editorCodeArea.requestFocus();
        }
    }

    public void setViewModel(EditorViewModel viewModel) {
        this.viewModel = viewModel;

        if (generateButton != null && promptTextArea != null && generationProgress != null && experimentalBox != null) {
            generationProgress.visibleProperty().bind(this.viewModel.isGeneratingProperty());
            generateButton.disableProperty().bind(this.viewModel.isGeneratingProperty().or(this.viewModel.selectedOllamaModelProperty().isNull()));
            selectTemplateButton.disableProperty().bind(this.viewModel.isGeneratingProperty());

            experimentalBox.visibleProperty().bind(this.viewModel.experimentalModeProperty());
            experimentalBox.managedProperty().bind(this.viewModel.experimentalModeProperty());

            templateNameLabel.textProperty().bind(this.viewModel.referenceTemplateNameProperty());
            templateNameLabel.visibleProperty().bind(this.viewModel.referenceTemplateNameProperty().isNotEmpty());
            templateNameLabel.managedProperty().bind(this.viewModel.referenceTemplateNameProperty().isNotEmpty());

            if (modelComboBox != null) {
                modelComboBox.setItems(this.viewModel.getAvailableOllamaModels());
                this.viewModel.selectedOllamaModelProperty().bind(modelComboBox.getSelectionModel().selectedItemProperty());

                this.viewModel.getAvailableOllamaModels().addListener((javafx.collections.ListChangeListener<String>) _ -> {
                    if (!this.viewModel.getAvailableOllamaModels().isEmpty() && modelComboBox.getSelectionModel().isEmpty()) {
                        javafx.application.Platform.runLater(() -> modelComboBox.getSelectionModel().selectFirst());
                    }
                });
            }

            if (searchTextField != null) {
                viewModel.searchTextProperty().bindBidirectional(searchTextField.textProperty());
            }

            if (searchMatchCaseToggle != null) {
                viewModel.searchMatchCaseProperty().bindBidirectional(searchMatchCaseToggle.selectedProperty());
            }

            if (searchWholeWordToggle != null) {
                viewModel.searchWholeWordProperty().bindBidirectional(searchWholeWordToggle.selectedProperty());
            }

            if (searchRegexToggle != null) {
                viewModel.searchRegexProperty().bindBidirectional(searchRegexToggle.selectedProperty());
            }

            if (searchCountLabel != null) {
                viewModel.searchCountTextProperty().addListener((_, _, newVal) -> {
                    searchCountLabel.setText(newVal != null ? newVal : "No results");
                });
            }

            generateButton.setOnAction(_ -> {
                String prompt = promptTextArea.getText();
                if (prompt != null && !prompt.isBlank()) {
                    this.viewModel.generateRuleFromText(prompt);
                }
            });

            selectTemplateButton.setOnAction(_ -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Template Rule File");
                File selectedFile = fileChooser.showOpenDialog(selectTemplateButton.getScene().getWindow());
                if (selectedFile != null) {
                    this.viewModel.loadTemplateAsync(selectedFile);
                }
            });
        }

        if (editorCodeArea != null) {
            editorCodeArea.getModel().addListener(_ -> {
                if (!this.viewModel.isUpdating()) {
                    this.viewModel.setTextContent(editorCodeArea.getText());
                }
            });

            editorCodeArea.setText(this.viewModel.getTextContent());

            this.viewModel.textContentProperty().addListener((_, _, newVal) -> {
                if (!newVal.equals(editorCodeArea.getText())) {
                    editorCodeArea.setText(newVal);
                }
            });

            this.viewModel.replaceTextCommandProperty().addListener((_, _, newVal) -> {
                if (newVal != null) {
                    try {
                        int lastParagraph = editorCodeArea.getModel().size() - 1;
                        editorCodeArea.replaceText(TextPos.ofLeading(0, 0), TextPos.ofLeading(lastParagraph, editorCodeArea.getModel().getPlainText(lastParagraph).length()), newVal.text(), false);
                    } finally {
                        this.viewModel.clearReplaceTextCommand();
                        this.viewModel.setUpdating(false);
                        this.viewModel.setTextContent(editorCodeArea.getText());
                    }
                }
            });

            this.viewModel.insertTextCommandProperty().addListener((_, _, newVal) -> {
                if (newVal != null) {
                    try {
                        TextPos currentPos = editorCodeArea.getCaretPosition();
                        if (currentPos == null) {
                            int lastParagraph = Math.max(0, editorCodeArea.getModel().size() - 1);
                            int textLen = editorCodeArea.getModel().getPlainText(lastParagraph).length();
                            currentPos = TextPos.ofLeading(lastParagraph, textLen);
                        }

                        editorCodeArea.insertText(currentPos, newVal.text(), null);

                        int newCharOffset = currentPos.offset() + newVal.text().length();
                        TextPos afterInsert = TextPos.ofLeading(currentPos.index(), newCharOffset);
                        editorCodeArea.select(afterInsert, afterInsert);
                        editorCodeArea.requestFocus();
                    } finally {
                        this.viewModel.clearInsertTextCommand();
                        this.viewModel.setUpdating(false);
                        this.viewModel.setTextContent(editorCodeArea.getText());
                    }
                }
            });

            this.viewModel.getErrors().addListener((ListChangeListener<SyntaxError>) _ -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            this.viewModel.matchedBracketsProperty().addListener((_, _, newVal) -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            this.viewModel.getTokens().addListener((ListChangeListener<Token>) _ -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            this.viewModel.getSearchMatches().addListener((ListChangeListener<EditorViewModel.SearchMatch>) _ -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            this.viewModel.currentMatchIndexProperty().addListener((_, _, newVal) -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());

                if (newVal != null && newVal.intValue() >= 0 && newVal.intValue() < viewModel.getSearchMatches().size()) {
                    EditorViewModel.SearchMatch match = viewModel.getSearchMatches().get(newVal.intValue());
                    TextPos startPos = computeTextPosFromOffset(match.start());
                    TextPos endPos = computeTextPosFromOffset(match.end());
                    if (startPos != null && endPos != null) {
                        editorCodeArea.select(startPos, endPos);
                    }
                }
            });

            editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());

            updateEditorStyle(this.viewModel.getFontSize());

            this.viewModel.fontSizeProperty().addListener((_, _, newVal) ->
                updateEditorStyle(newVal.intValue()));

            editorCodeArea.setOnScroll(event -> {
                if (event.isControlDown()) {
                    int currentSize = viewModel.getFontSize();
                    if (event.getDeltaY() > 0) {
                        viewModel.setFontSize(currentSize + 1);
                    } else if (event.getDeltaY() < 0 && currentSize > 8) {
                        viewModel.setFontSize(currentSize - 1);
                    }
                    event.consume();
                }
            });

            editorCodeArea.caretPositionProperty().addListener((_, _, newVal) -> {
                if (newVal != null) {
                    int absoluteOffset = computeAbsoluteOffset(newVal);
                    viewModel.onCaretPositionChanged(absoluteOffset);
                }
            });

            if (bracketLineOverlay != null) {
                connectionLine = new javafx.scene.shape.Line();
                connectionLine.setStyle("-fx-stroke: #4c5052; -fx-stroke-width: 1px;");
                connectionLine.setVisible(false);
                bracketLineOverlay.getChildren().add(connectionLine);

                // Track bounds changes to redraw bracket line on scroll/layout changes
                editorCodeArea.needsLayoutProperty().addListener((_, _, _) -> updateBracketLine());
                editorCodeArea.boundsInLocalProperty().addListener((_, _, _) -> updateBracketLine());
                editorCodeArea.widthProperty().addListener((_, _, _) -> updateBracketLine());
                editorCodeArea.heightProperty().addListener((_, _, _) -> updateBracketLine());

                // Since CodeArea's direct properties might not fire constantly on pure scroll
                // we listen to scroll events and layout passes.
                editorCodeArea.addEventHandler(javafx.scene.input.ScrollEvent.ANY, _ -> updateBracketLine());

                this.viewModel.matchedBracketsProperty().addListener((_, _, _) -> updateBracketLine());
            }

            this.viewModel.getSuggestedTokens().addListener((ListChangeListener<String>) _ -> {
                suggestionsPane.getChildren().clear();
                for (String token : this.viewModel.getSuggestedTokens()) {
                    Button btn = new Button(token);
                    btn.getStyleClass().addAll("pill-button");
                    btn.setOnAction(_ -> viewModel.insertBaustein(token));
                    suggestionsPane.getChildren().add(btn);
                }
            });
        }
    }

    private void updateBracketLine() {
        if (connectionLine == null || bracketLineOverlay == null || viewModel == null) {
            return;
        }

        EditorViewModel.MatchedBracketsRecord matchedBrackets = viewModel.matchedBracketsProperty().get();
        if (matchedBrackets == null) {
            connectionLine.setVisible(false);
            return;
        }

        int openIdx = matchedBrackets.openIndex();
        int closeIdx = matchedBrackets.closeIndex();

        TextPos openPos = computeTextPosFromOffset(openIdx);
        TextPos closePos = computeTextPosFromOffset(closeIdx);

        if (openPos == null || closePos == null || openPos.index() == closePos.index()) {
            // Do not draw if on same line
            connectionLine.setVisible(false);
            return;
        }

        // Only draw connection line for curly braces `{}` as requested for IntelliJ-style
        if (matchedBrackets.bracketChar() != '{') {
            connectionLine.setVisible(false);
            return;
        }

        // Must run in runLater because TextFlow nodes might be recreating right now
        javafx.application.Platform.runLater(() -> {
            try {
                javafx.geometry.Rectangle2D openCaret = getCaretBounds(openPos);
                javafx.geometry.Rectangle2D closeCaret = getCaretBounds(closePos);

                if (openCaret != null && closeCaret != null) {
                    // IntelliJ-style: vertical line aligning with the closing brace's X coordinate
                    // that extends from the bottom of the opening brace line to the top of the closing brace line.
                    double verticalX = closeCaret.getMinX();
                    connectionLine.setStartX(verticalX);
                    connectionLine.setStartY(openCaret.getMaxY());
                    connectionLine.setEndX(verticalX);
                    connectionLine.setEndY(closeCaret.getMinY());
                    connectionLine.setVisible(true);
                } else {
                    connectionLine.setVisible(false);
                }
            } catch (Exception e) {
                LOG.warning("Failed to calculate bracket line: " + e.getMessage());
                connectionLine.setVisible(false);
            }
        });
    }

    private javafx.geometry.Rectangle2D getCaretBounds(TextPos pos) {
        if (pos == null) return null;

        int targetPara = pos.index();
        int charIdx = pos.offset();

        // Find all TextFlow elements inside editorCodeArea
        for (javafx.scene.Node node : editorCodeArea.lookupAll("TextFlow")) {
            if (node instanceof javafx.scene.text.TextFlow textFlow) {
                // Determine if this TextFlow belongs to targetPara.
                String modelText = editorCodeArea.getModel().getPlainText(targetPara);

                // Construct the text from this TextFlow
                StringBuilder flowText = new StringBuilder();
                for (javafx.scene.Node child : textFlow.getChildren()) {
                    if (child instanceof javafx.scene.text.Text textNode) {
                        flowText.append(textNode.getText());
                    }
                }

                if (flowText.toString().equals(modelText)) {
                    javafx.scene.text.LayoutInfo layoutInfo = textFlow.getLayoutInfo();
                    if (layoutInfo != null && charIdx <= flowText.length()) {
                        try {
                            javafx.scene.text.CaretInfo caretInfo = layoutInfo.caretInfoAt(charIdx, true);
                            if (caretInfo != null && caretInfo.getSegmentCount() > 0) {
                                javafx.geometry.Rectangle2D localCaret = caretInfo.getSegmentAt(0);
                                // Transform local coordinates of the TextFlow to bracketLineOverlay coordinates
                                javafx.geometry.Bounds localBounds = new javafx.geometry.BoundingBox(localCaret.getMinX(), localCaret.getMinY(), localCaret.getWidth(), localCaret.getHeight());
                                javafx.geometry.Bounds overlayBounds = bracketLineOverlay.sceneToLocal(textFlow.localToScene(localBounds));

                                return new javafx.geometry.Rectangle2D(overlayBounds.getMinX(), overlayBounds.getMinY(), overlayBounds.getWidth(), overlayBounds.getHeight());
                            }
                        } catch (Exception e) {
                            // index out of bounds or unsupported
                        }
                    }
                }
            }
        }

        return null;
    }

    private SyntaxDecorator createSyntaxDecorator() {
        return new SyntaxDecorator() {
            @Override
            public RichParagraph createRichParagraph(CodeTextModel model, int paragraphIndex) {
                String text = model.getPlainText(paragraphIndex);
                RichParagraph.Builder builder = RichParagraph.builder();

                // Note: ANTLR lines are 1-based, paragraphIndex is 0-based
                int antlrLine = paragraphIndex + 1;

                if (viewModel != null) {
                    var vocab = viewModel.getVocabulary();
                    var tokenStyles = viewModel.getTokenStylesForLine(antlrLine);

                    if (vocab != null && tokenStyles != null && !tokenStyles.isEmpty()) {
                        // Ensure token styles are sorted by their start index and do not overlap
                        java.util.List<EditorViewModel.TokenStyle> sortedStyles = new java.util.ArrayList<>(tokenStyles);
                        sortedStyles.sort(java.util.Comparator.comparingInt(EditorViewModel.TokenStyle::startInLine));

                        int currentIndex = 0;
                        for (EditorViewModel.TokenStyle style : sortedStyles) {
                            int start = style.startInLine();
                            int end = style.endInLine();

                            // Ensure strict bounds checking to prevent overlap or out-of-order text duplication
                            if (start >= currentIndex && end <= text.length() && start < end) {
                                // Add any unstyled text before this token
                                if (start > currentIndex) {
                                    builder.addSegment(text.substring(currentIndex, start));
                                }

                                String cssClass = tokenHighlightMappingService.getCssClass(style.tokenType());
                                String tokenText = text.substring(start, end);

                                if (tokenText.startsWith("@")) {
                                    cssClass = "annotation";
                                }

                                if (cssClass != null) {
                                    builder.addWithStyleNames(tokenText, cssClass);
                                } else {
                                    builder.addSegment(tokenText);
                                }

                                currentIndex = end;
                            }
                        }

                        // Add any remaining unstyled text at the end of the line
                        if (currentIndex < text.length()) {
                            builder.addSegment(text.substring(currentIndex));
                        }
                    } else {
                        // No tokens to style, just add the whole text
                        builder.addSegment(text);
                    }

                    EditorViewModel.MatchedBracketsRecord matchedBrackets = viewModel.matchedBracketsProperty().get();
                    if (matchedBrackets != null) {
                        int paraStartOffset = computeAbsoluteOffset(TextPos.ofLeading(paragraphIndex, 0));
                        int paraEndOffset = paraStartOffset + text.length();

                        if (matchedBrackets.openIndex() >= paraStartOffset && matchedBrackets.openIndex() < paraEndOffset) {
                            int localOpenIdx = matchedBrackets.openIndex() - paraStartOffset;
                            // Applying style via addWithStyleNames here would APPEND text, so we can't do that.
                            // We use addHighlight instead to overlay the style on existing segments.
                            // We use Color.web to match the Darcula color requested for brackets #43454a.
                            builder.addHighlight(localOpenIdx, 1, Color.web("#43454a"));
                        }
                        if (matchedBrackets.closeIndex() >= paraStartOffset && matchedBrackets.closeIndex() < paraEndOffset) {
                            int localCloseIdx = matchedBrackets.closeIndex() - paraStartOffset;
                            builder.addHighlight(localCloseIdx, 1, Color.web("#43454a"));
                        }
                    }

                    if (viewModel.getSearchMatches() != null && !viewModel.getSearchMatches().isEmpty()) {
                        int paraStartOffset = computeAbsoluteOffset(TextPos.ofLeading(paragraphIndex, 0));
                        int paraEndOffset = paraStartOffset + text.length();
                        int currentActiveMatch = viewModel.currentMatchIndexProperty().get();

                        for (int i = 0; i < viewModel.getSearchMatches().size(); i++) {
                            EditorViewModel.SearchMatch match = viewModel.getSearchMatches().get(i);
                            // Check if the match intersects with the current paragraph
                            if (match.start() < paraEndOffset && match.end() > paraStartOffset) {
                                int matchStartInLine = Math.max(0, match.start() - paraStartOffset);
                                int matchEndInLine = Math.min(text.length(), match.end() - paraStartOffset);

                                if (matchStartInLine < matchEndInLine) {
                                    if (i == currentActiveMatch) {
                                        // Active match: slightly darker pink or different style
                                        builder.addHighlight(matchStartInLine, matchEndInLine - matchStartInLine, Color.HOTPINK);
                                    } else {
                                        // Inactive match: light pink
                                        builder.addHighlight(matchStartInLine, matchEndInLine - matchStartInLine, Color.LIGHTPINK);
                                    }
                                }
                            }
                        }
                    }

                    // Check for errors on this line
                    for (SyntaxError error : viewModel.getErrors()) {
                        if (error.line() == antlrLine) {
                            int start = error.charPositionInLine();
                            int end = start + error.length();
                            if (start >= 0 && end <= text.length() && start < end) {
                                builder.addWavyUnderline(start, end, Color.RED);
                            } else if (start >= 0 && start <= text.length()) {
                                // Fallback if length is out of bounds or 0
                                int safeEnd = Math.min(start + 1, text.length());
                                if (start < safeEnd) {
                                    builder.addWavyUnderline(start, safeEnd, Color.RED);
                                }
                            }
                        }
                    }
                } else {
                    builder.addSegment(text);
                }

                return builder.build();
            }

            @Override
            public void handleChange(CodeTextModel model, TextPos start, TextPos end, int paragraphCount, int charCount, int textLength) {
                // Not needed for a full redraw on error list update
            }
        };
    }

    public EditorViewModel getViewModel() {
        return viewModel;
    }

    private void updateEditorStyle(int size) {
        editorCodeArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: " + size + "pt;");
    }

    /**
     * Synchronisiert die Stylesheets des Tooltip-Popups mit denen der Scene,
     * damit theme-aware CSS-Variablen auch im Popup-Window wirken.
     * Tooltip extends PopupControl – dessen Scene wird beim ersten Show() erstellt;
     * wir setzen die Stylesheets daher auf dem Tooltip selbst über setStyle-Klassen
     * und reichen die User-Agent-Stylesheets über den ownerNode weiter.
     * Der zuverlässigste Weg: direkt die Skin-Scene beschreiben sobald sie existiert.
     */
    private void syncTooltipStylesheets(javafx.scene.Scene scene) {
        // Tooltip.getScene() ist erst nach dem ersten show() verfügbar.
        // Wir merken uns die Stylesheets und setzen sie beim nächsten showingProperty-Wechsel.
        errorTooltip.showingProperty().addListener((_, _, showing) -> {
            if (showing && errorTooltip.getScene() != null) {
                errorTooltip.getScene().getStylesheets().setAll(scene.getStylesheets());
            }
        });
        // Falls der Tooltip bereits sichtbar war und die Scene sich ändert (Theme-Toggle):
        if (errorTooltip.getScene() != null) {
            errorTooltip.getScene().getStylesheets().setAll(scene.getStylesheets());
        }
    }

    /**
     * Converts a TextPos (paragraph + intra-paragraph offset) to an absolute
     * character offset in the full text, counting each paragraph separator as one '\n'.
     */
    /**
     * Converts an absolute character offset to a TextPos (paragraph + intra-paragraph offset).
     */
    private TextPos computeTextPosFromOffset(int offset) {
        if (editorCodeArea == null || editorCodeArea.getModel() == null) return null;
        int currentOffset = 0;
        for (int i = 0; i < editorCodeArea.getModel().size(); i++) {
            int len = editorCodeArea.getModel().getPlainText(i).length();
            if (offset <= currentOffset + len) {
                return TextPos.ofLeading(i, offset - currentOffset);
            }
            currentOffset += len + 1; // +1 for newline separator
        }
        // If offset is past the end, return the very end position
        int lastIdx = editorCodeArea.getModel().size() - 1;
        if (lastIdx < 0) return null;
        return TextPos.ofLeading(lastIdx, editorCodeArea.getModel().getPlainText(lastIdx).length());
    }

    private int computeAbsoluteOffset(TextPos pos) {
        if (pos == null) return 0;
        int absolute = 0;
        int paragraphIndex = pos.index();
        for (int i = 0; i < paragraphIndex; i++) {
            absolute += editorCodeArea.getModel().getPlainText(i).length();
            absolute += 1; // newline separator between paragraphs
        }
        absolute += pos.offset();
        return absolute;
    }
}
