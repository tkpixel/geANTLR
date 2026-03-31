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

    private final javafx.scene.control.Tooltip errorTooltip = new javafx.scene.control.Tooltip();
    private final PauseTransition hoverPause = new PauseTransition(Duration.millis(300));
    private SyntaxError lastHoveredError = null;
    // Screen coordinates stored on mouse-move, used when the pause fires
    private double lastScreenX, lastScreenY;

    private EditorViewModel viewModel;
    private final TokenHighlightMappingService tokenHighlightMappingService;

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
                    // Tooltip-CSS mit der Scene synchron halten
                    syncTooltipStylesheets(newScene);
                    newScene.getStylesheets().addListener((javafx.collections.ListChangeListener<String>) _ ->
                        syncTooltipStylesheets(newScene));
                }
            });
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

            this.viewModel.getTokens().addListener((ListChangeListener<Token>) _ -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
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
