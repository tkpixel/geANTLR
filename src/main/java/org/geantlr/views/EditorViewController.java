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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
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

            editorCodeArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
                        () -> {
                            if (this.viewModel != null) {
                                this.viewModel.formatCode();
                            }
                        }
                    );
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

                // Pre-select an item if available once the list is populated
                this.viewModel.getAvailableOllamaModels().addListener((javafx.collections.ListChangeListener<String>) c -> {
                    if (!this.viewModel.getAvailableOllamaModels().isEmpty() && modelComboBox.getSelectionModel().isEmpty()) {
                        javafx.application.Platform.runLater(() -> modelComboBox.getSelectionModel().selectFirst());
                    }
                });
            }

            generateButton.setOnAction(e -> {
                String prompt = promptTextArea.getText();
                if (prompt != null && !prompt.isBlank()) {
                    this.viewModel.generateRuleFromText(prompt);
                }
            });

            selectTemplateButton.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Template Rule File");
                File selectedFile = fileChooser.showOpenDialog(selectTemplateButton.getScene().getWindow());
                if (selectedFile != null) {
                    try {
                        String content = Files.readString(selectedFile.toPath());
                        this.viewModel.referenceTemplateProperty().set(content);
                        this.viewModel.referenceTemplateNameProperty().set("Template: " + selectedFile.getName());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        if (editorCodeArea != null) {
            // Unbind previous listeners if necessary (simplified for demo)

            // Update view model when CodeArea text changes
            editorCodeArea.getModel().addListener(change -> {
                if (!this.viewModel.isUpdating()) {
                    this.viewModel.setTextContent(editorCodeArea.getText());
                }
            });

            // Set initial text
            editorCodeArea.setText(this.viewModel.getTextContent());

            // Listen to view model changes
            this.viewModel.textContentProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.equals(editorCodeArea.getText())) {
                    editorCodeArea.setText(newVal);
                }
            });

            // Listen to replace text command for formatting
            this.viewModel.replaceTextCommandProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        // replaceText applies a single action to the undo stack
                        // Using 'false' for 'preserveStyle' parameter
                        int lastParagraph = editorCodeArea.getModel().size() - 1;
                        editorCodeArea.replaceText(TextPos.ofLeading(0, 0), TextPos.ofLeading(lastParagraph, editorCodeArea.getModel().getPlainText(lastParagraph).length()), newVal.text(), false);
                    } finally {
                        this.viewModel.clearReplaceTextCommand();
                        this.viewModel.setUpdating(false);
                        this.viewModel.setTextContent(editorCodeArea.getText());
                    }
                }
            });

            // Listen to insert text command
            this.viewModel.insertTextCommandProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    try {
                        TextPos currentPos = editorCodeArea.getCaretPosition();
                        if (currentPos == null) {
                            int lastParagraph = Math.max(0, editorCodeArea.getModel().size() - 1);
                            int textLen = editorCodeArea.getModel().getPlainText(lastParagraph).length();
                            currentPos = TextPos.ofLeading(lastParagraph, textLen);
                        }

                        editorCodeArea.insertText(currentPos, newVal.text() + " ", null);

                        int newOffset = currentPos.offset() + newVal.text().length() + 1;
                        editorCodeArea.select(TextPos.ofLeading(newOffset, newOffset));

                        editorCodeArea.requestFocus();
                    } finally {
                        // Clear the command in ViewModel to allow repeated commands
                        this.viewModel.clearInsertTextCommand();
                        this.viewModel.setUpdating(false);
                        this.viewModel.setTextContent(editorCodeArea.getText());
                    }
                }
            });

            // Listen to error changes
            this.viewModel.getErrors().addListener((ListChangeListener<SyntaxError>) c -> {
                // Trigger a full redraw to apply syntax decorations
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            // Listen to token changes for highlighting
            this.viewModel.getTokens().addListener((ListChangeListener<Token>) c -> {
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());

            // Initial font size
            updateEditorStyle(this.viewModel.getFontSize());

            // Listen to font size changes
            this.viewModel.fontSizeProperty().addListener((obs, oldVal, newVal) -> {
                updateEditorStyle(newVal.intValue());
            });

            // Zoom with Ctrl+Scroll
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

            // Listen to caret position
            editorCodeArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    int caretIndex = newVal.offset();
                    viewModel.onCaretPositionChanged(caretIndex);
                }
            });

            // Listen to token suggestions
            this.viewModel.getSuggestedTokens().addListener((ListChangeListener<String>) c -> {
                suggestionsPane.getChildren().clear();
                for (String token : this.viewModel.getSuggestedTokens()) {
                    Button btn = new Button(token);
                    btn.getStyleClass().addAll("pill-button");
                    // On click, append text via the view model mediator
                    btn.setOnAction(e -> {
                        viewModel.insertBaustein(token);
                    });
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
                RichParagraph.Builder builder = RichParagraph.builder().addSegment(text);

                // Note: ANTLR lines are 1-based, paragraphIndex is 0-based
                int antlrLine = paragraphIndex + 1;

                if (viewModel != null) {
                    var vocab = viewModel.getVocabulary();
                    // We will query the view model for the processed token styles for this specific line
                    var tokenStyles = viewModel.getTokenStylesForLine(antlrLine);

                    if (vocab != null && tokenStyles != null && !tokenStyles.isEmpty()) {
                        for (EditorViewModel.TokenStyle style : tokenStyles) {
                            int start = style.startInLine();
                            int end = style.endInLine();

                            // Ensure valid bounds
                            if (start >= 0 && end <= text.length() && start < end) {
                                String cssClass = tokenHighlightMappingService.getCssClass(style.symbolicName());
                                if (cssClass != null) {
                                    // Workaround for the reviewer's missing method:
                                    // We'll map the css class string directly to a Color using a simple lookup
                                    // because addHighlight only takes a Color, despite the prompt's instruction.
                                    Color highlightColor = getHighlightColor(cssClass);
                                    if (highlightColor != null) {
                                        builder.addHighlight(start, end, highlightColor);
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

    private Color getHighlightColor(String cssClass) {
        // Fallback mapping since addHighlight requires Color instead of CSS class strings
        return switch (cssClass) {
            case "keyword" -> Color.web("#000080"); // Standard Java keyword
            case "string" -> Color.web("#008000");  // Standard Java string
            case "number" -> Color.web("#bf8700");  // -color-warning-fg
            case "comment" -> Color.web("#8c959f"); // -color-fg-muted
            default -> null;
        };
    }
}
