package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
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
import atlantafx.base.theme.Styles;
import javafx.scene.control.Tooltip;
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
    private final PauseTransition hoverPause = new PauseTransition(Duration.millis(800));
    private SyntaxError lastHoveredError = null;
    // Screen coordinates stored on mouse-move, used when the pause fires
    private double lastScreenX, lastScreenY;

    private EditorViewModel viewModel;
    private final TokenHighlightMappingService tokenHighlightMappingService;
    private javafx.scene.canvas.Canvas bracketCanvas;

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
                if (viewModel.isTypingProperty().get()) return;

                TextPos pos = editorCodeArea.getTextPosition(lastScreenX, lastScreenY);
                if (pos == null) {
                    LOG.fine("[Hover] getTextPosition returned null for screen=(" + lastScreenX + "," + lastScreenY + ")");
                    return;
                }

                int antlrLine = pos.index() + 1;
                int antlrChar = pos.offset();
                LOG.fine("[Hover] TextPos index=" + pos.index() + " offset=" + pos.offset()
                    + " → ANTLR line=" + antlrLine + " char=" + antlrChar
                    + "  errors=" + viewModel.getErrors().size());

                SyntaxError error = viewModel.getErrorAt(antlrLine, antlrChar);
                if (error != null) {
                    LOG.fine("[Hover] MATCH → showing tooltip: " + error.message());
                    errorTooltip.setText(error.message());
                    errorTooltip.show(editorCodeArea, lastScreenX + 2, lastScreenY + 18);
                } else {
                    LOG.fine("[Hover] no match at line=" + antlrLine + " char=" + antlrChar);
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
                    // Tooltip-CSS mit der Scene synchron halten
                    syncTooltipStylesheets(newScene);
                    newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) _ ->
                        syncTooltipStylesheets(newScene));
                }
            });

            // ── Keyboard shortcuts via EventFilter on the editor (per-editor, not scene-global) ──
            editorCodeArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                // Ctrl+Shift+F → Format code (only this editor)
                if (event.getCode() == KeyCode.F && event.isShortcutDown() && event.isShiftDown()) {
                    if (this.viewModel != null) {
                        this.viewModel.formatCode();
                    }
                    event.consume();
                    return;
                }
                // Ctrl+F → Open search bar (only this editor)
                if (event.getCode() == KeyCode.F && event.isShortcutDown() && !event.isShiftDown()) {
                    if (searchBar != null && searchTextField != null) {
                        searchBar.setVisible(true);
                        searchBar.setManaged(true);
                        searchTextField.requestFocus();
                        searchTextField.selectAll();
                    }
                    event.consume();
                    return;
                }
                // Tab key handling
                if (event.getCode() == KeyCode.TAB) {
                    handleTabKey(event);
                    return;
                }
            });
        }

        if (searchCloseButton != null) {
            searchCloseButton.setOnAction(_ -> closeSearch());
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

    private void handleTabKey(javafx.scene.input.KeyEvent event) {
        // Fallback no-op for tab handling if not fully implemented in the tasklist
        // Added to prevent compile errors since handleTabKey is called in the event filter
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

        if (generateButton != null && promptTextArea != null && experimentalBox != null) {
            javafx.scene.Node originalGraphic = generateButton.getGraphic();
            this.viewModel.isGeneratingProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    ProgressIndicator spinner = new ProgressIndicator();
                    spinner.setPrefSize(16, 16);
                    generateButton.setGraphic(spinner);
                } else {
                    generateButton.setGraphic(originalGraphic);
                }
            });
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
                viewModel.searchCountTextProperty().addListener((_, _, newVal) ->
                    searchCountLabel.setText(newVal != null ? newVal : "No results"));
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

            this.viewModel.matchedBracketsProperty().addListener((_, _, _) -> {
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
                // Use a Canvas overlay so we can draw arbitrary lines without JavaFX shape positioning issues.
                // The canvas is bound to the overlay pane's size so it always covers the full editor area.
                bracketCanvas = new javafx.scene.canvas.Canvas();
                bracketCanvas.setMouseTransparent(true);
                bracketCanvas.widthProperty().bind(bracketLineOverlay.widthProperty());
                bracketCanvas.heightProperty().bind(bracketLineOverlay.heightProperty());
                bracketLineOverlay.getChildren().add(bracketCanvas);


                // Redraw whenever layout changes or the bracket record changes
                bracketLineOverlay.widthProperty().addListener((_, _, _) -> updateBracketLine());
                bracketLineOverlay.heightProperty().addListener((_, _, _) -> updateBracketLine());
                editorCodeArea.needsLayoutProperty().addListener((_, _, _) -> updateBracketLine());
                editorCodeArea.addEventHandler(javafx.scene.input.ScrollEvent.ANY, _ -> updateBracketLine());
                this.viewModel.matchedBracketsProperty().addListener((_, _, _) -> updateBracketLine());
            }

            this.viewModel.getSuggestedTokens().addListener((ListChangeListener<String>) _ -> updateSuggestionsPane());
            updateSuggestionsPane();
        }
    }

    private void updateSuggestionsPane() {
        if (suggestionsPane == null || this.viewModel == null) return;

        suggestionsPane.getChildren().clear();
        if (this.viewModel.getSuggestedTokens().isEmpty()) {
            Label emptyLabel = new Label("No suggestions available");
            emptyLabel.getStyleClass().add(Styles.TEXT_MUTED);
            suggestionsPane.getChildren().add(emptyLabel);
        } else {
            for (String token : this.viewModel.getSuggestedTokens()) {
                Button btn = new Button(token);
                btn.getStyleClass().addAll("pill-button");
                btn.setAccessibleText("Insert " + token);
                btn.setAccessibleHelp("Inserts the suggested token at the current cursor position");
                btn.setTooltip(new Tooltip("Insert '" + token + "'"));
                btn.setOnAction(_ -> viewModel.insertBaustein(token));
                suggestionsPane.getChildren().add(btn);
            }
        }
    }

    private void updateBracketLine() {
        if (bracketCanvas == null || bracketLineOverlay == null || viewModel == null) {
            return;
        }

        EditorViewModel.MatchedBracketsRecord matchedBrackets = viewModel.matchedBracketsProperty().get();

        // Clear canvas regardless – always redraw from scratch
        javafx.scene.canvas.GraphicsContext gc = bracketCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, bracketCanvas.getWidth(), bracketCanvas.getHeight());

        if (matchedBrackets == null || matchedBrackets.bracketChar() != '{') {
            return;
        }

        TextPos openPos = computeTextPosFromOffset(matchedBrackets.openIndex());
        TextPos closePos = computeTextPosFromOffset(matchedBrackets.closeIndex());

        if (openPos == null || closePos == null || openPos.index() == closePos.index()) {
            return;
        }

        // Schedule the pixel-coordinate lookup after the layout pass is complete
        javafx.application.Platform.runLater(() -> drawBracketLine(gc, openPos, closePos));
    }

    private void drawBracketLine(javafx.scene.canvas.GraphicsContext gc, TextPos openPos, TextPos closePos) {
        try {
            // Find the first non-whitespace column on the opening-brace line
            String openLineText = editorCodeArea.getModel().getPlainText(openPos.index());
            int firstNonWsIdx = 0;
            while (firstNonWsIdx < openLineText.length() && Character.isWhitespace(openLineText.charAt(firstNonWsIdx))) {
                firstNonWsIdx++;
            }
            if (firstNonWsIdx >= openLineText.length()) {
                firstNonWsIdx = openPos.offset();
            }

            TextPos indentPos = TextPos.ofLeading(openPos.index(), firstNonWsIdx);

            javafx.geometry.Point2D indentPt  = getCharTopLeft(indentPos);
            javafx.geometry.Point2D openPt    = getCharTopLeft(openPos);
            javafx.geometry.Point2D closePt   = getCharTopLeft(closePos);

            LOG.fine("[BracketLine] indentPt=" + indentPt + " openPt=" + openPt + " closePt=" + closePt
                    + "  canvas=" + bracketCanvas.getWidth() + "x" + bracketCanvas.getHeight());

            double overlayHeight = bracketCanvas.getHeight();

            // Determine X: prefer indent column, then open-brace, finally close-brace
            // (the closing brace is always at the same indentation level as the block start)
            Double verticalX = null;
            if (indentPt != null) {
                verticalX = indentPt.getX();
            } else if (openPt != null) {
                verticalX = openPt.getX();
            } else if (closePt != null) {
                verticalX = closePt.getX();
            }

            if (verticalX == null) {
                LOG.fine("[BracketLine] verticalX could not be determined (all three positions outside viewport) – skipping draw");
                return;
            }

            // Y-start: bottom of the opening-brace row.
            // If the opening brace is scrolled above the viewport, clamp to the top edge.
            double lineHeight = estimateLineHeight();
            double startY = (openPt != null) ? openPt.getY() + lineHeight : 0.0;

            // Y-end: top of the closing-brace row.
            // If the closing brace is scrolled below the viewport, clamp to the bottom edge.
            double endY = (closePt != null) ? closePt.getY() : overlayHeight;

            if (endY <= startY) {
                LOG.fine("[BracketLine] endY(" + endY + ") <= startY(" + startY + ") – skipping draw");
                return;
            }

            // Draw the vertical guide line on the canvas
            gc.clearRect(0, 0, bracketCanvas.getWidth(), bracketCanvas.getHeight());
            gc.setStroke(javafx.scene.paint.Color.web("#4c5052"));
            gc.setLineWidth(1.0);
            // Snap to pixel boundary for crisp rendering
            double x = Math.floor(verticalX) + 0.5;
            gc.strokeLine(x, startY, x, endY);

            LOG.fine("[BracketLine] drew line x=" + x + " y=" + startY + "→" + endY);

        } catch (Exception e) {
            LOG.warning("[BracketLine] Exception during draw: " + e.getMessage());
        }
    }

    /** Returns the top-left pixel coordinate (in bracketLineOverlay space) of the character at {@code pos},
     *  or null if the paragraph is currently not rendered in the viewport. */
    private javafx.geometry.Point2D getCharTopLeft(TextPos pos) {
        if (pos == null) return null;

        int targetPara = pos.index();
        int charIdx    = pos.offset();

        // ── Step 1: Collect and sort all visible TextFlows by their on-screen Y position ──
        java.util.List<javafx.scene.text.TextFlow> flows = new java.util.ArrayList<>();
        for (javafx.scene.Node cell : editorCodeArea.lookupAll(".content > *")) {
            javafx.scene.text.TextFlow tf = findTextFlow(cell);
            if (tf != null) flows.add(tf);
        }

        if (flows.isEmpty()) {
            LOG.fine("[BracketLine] No TextFlows found via .content > *");
            return null;
        }

        flows.sort(java.util.Comparator.comparingDouble(
                f -> f.localToScene(f.getBoundsInLocal()).getMinY()));

        // ── Step 2: Find which TextFlow the caret is currently in ──
        int caretFlowIndex = -1;
        int caretParaIndex = -1;

        TextPos caretPos = editorCodeArea.getCaretPosition();
        if (caretPos != null) {
            caretParaIndex = caretPos.index();

            // Locate the caret node (a Path rendered inside the CodeArea skin)
            javafx.scene.Node caretNode = editorCodeArea.lookup(".caret");
            if (caretNode != null) {
                javafx.geometry.Bounds caretBounds = caretNode.localToScene(caretNode.getBoundsInLocal());
                double caretY = caretBounds.getCenterY();

                for (int i = 0; i < flows.size(); i++) {
                    javafx.geometry.Bounds fb = flows.get(i).localToScene(flows.get(i).getBoundsInLocal());
                    if (caretY >= fb.getMinY() && caretY <= fb.getMaxY()) {
                        caretFlowIndex = i;
                        break;
                    }
                }
            }
        }

        LOG.fine("[BracketLine] caretParaIndex=" + caretParaIndex
                + " caretFlowIndex=" + caretFlowIndex
                + " targetPara=" + targetPara
                + " flows=" + flows.size());

        // ── Step 3: Map targetPara → its TextFlow ──
        javafx.scene.text.TextFlow targetFlow = null;

        if (caretFlowIndex != -1 && caretParaIndex != -1) {
            // Robust: derive index from caret anchor
            int targetFlowIndex = caretFlowIndex - (caretParaIndex - targetPara);
            if (targetFlowIndex >= 0 && targetFlowIndex < flows.size()) {
                targetFlow = flows.get(targetFlowIndex);
            }
        }

        if (targetFlow == null) {
            // Fallback: try reflection-based getIndex() (works after a scene-graph lookup)
            for (javafx.scene.Node cell : editorCodeArea.lookupAll(".content > *")) {
                try {
                    java.lang.reflect.Method m = cell.getClass().getMethod("getIndex");
                    Object val = m.invoke(cell);
                    if (val instanceof Integer idx && idx == targetPara) {
                        targetFlow = findTextFlow(cell);
                        break;
                    }
                } catch (Exception ignored) {
                    LOG.finest("Reflection getIndex lookup failed: " + ignored.getMessage());
                }
            }
        }

        if (targetFlow == null) {
            LOG.fine("[BracketLine] para=" + targetPara + " not in visible viewport");
            return null;
        }

        // ── Step 4: Get pixel coordinate via LayoutInfo / CaretInfo ──
        javafx.scene.text.LayoutInfo li = targetFlow.getLayoutInfo();
        if (li == null) {
            LOG.fine("[BracketLine] LayoutInfo null for para=" + targetPara);
            return null;
        }

        try {
            javafx.scene.text.CaretInfo ci = li.caretInfoAt(charIdx, true);
            if (ci == null || ci.getSegmentCount() == 0) {
                LOG.fine("[BracketLine] CaretInfo empty para=" + targetPara + " charIdx=" + charIdx);
                return null;
            }

            javafx.geometry.Rectangle2D seg = ci.getSegmentAt(0);
            javafx.geometry.Bounds local = new javafx.geometry.BoundingBox(
                    seg.getMinX(), seg.getMinY(), seg.getWidth(), seg.getHeight());
            javafx.geometry.Bounds inOverlay =
                    bracketLineOverlay.sceneToLocal(targetFlow.localToScene(local));

            return new javafx.geometry.Point2D(inOverlay.getMinX(), inOverlay.getMinY());
        } catch (Exception ex) {
            LOG.warning("[BracketLine] caretInfoAt threw: " + ex.getMessage());
            return null;
        }
    }

    /** Recursively finds the first TextFlow in the child hierarchy of {@code node}. */
    private javafx.scene.text.TextFlow findTextFlow(javafx.scene.Node node) {
        if (node instanceof javafx.scene.text.TextFlow tf) return tf;
        if (node instanceof javafx.scene.Parent p) {
            for (javafx.scene.Node child : p.getChildrenUnmodifiable()) {
                javafx.scene.text.TextFlow found = findTextFlow(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** Estimates the line height by measuring the first visible TextFlow; falls back to a heuristic. */
    private double estimateLineHeight() {
        for (javafx.scene.Node cell : editorCodeArea.lookupAll(".content > *")) {
            javafx.scene.text.TextFlow tf = findTextFlow(cell);
            if (tf != null) {
                double h = tf.getHeight();
                if (h > 0) return h;
            }
        }
        // Fallback: derive from font size (14pt ≈ 19px at 96 dpi)
        return viewModel != null ? viewModel.getFontSize() * 1.4 : 19.0;
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
