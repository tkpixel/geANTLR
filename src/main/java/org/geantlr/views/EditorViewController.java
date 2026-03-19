package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Button;
import jfx.incubator.scene.control.richtext.CodeArea;
import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import jfx.incubator.scene.control.richtext.TextPos;
import org.geantlr.services.SyntaxError;
import org.geantlr.viewmodels.EditorViewModel;

@Prototype
public class EditorViewController {

    @FXML
    private CodeArea editorCodeArea;

    @FXML
    private FlowPane suggestionsPane;

    private EditorViewModel viewModel;

    @Inject
    public EditorViewController() {
    }

    @FXML
    public void initialize() {
        if (editorCodeArea != null) {
            editorCodeArea.setLineNumbersEnabled(true);
        }
    }

    public void setViewModel(EditorViewModel viewModel) {
        this.viewModel = viewModel;
        if (editorCodeArea != null) {
            // Unbind previous listeners if necessary (simplified for demo)

            // Update view model when CodeArea text changes
            editorCodeArea.getModel().addListener(change -> {
                this.viewModel.setTextContent(editorCodeArea.getText());
            });

            // Set initial text
            editorCodeArea.setText(this.viewModel.getTextContent());

            // Listen to view model changes
            this.viewModel.textContentProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.equals(editorCodeArea.getText())) {
                    editorCodeArea.setText(newVal);
                }
            });

            // Listen to error changes
            this.viewModel.getErrors().addListener((ListChangeListener<SyntaxError>) c -> {
                // Trigger a full redraw to apply syntax decorations
                editorCodeArea.setSyntaxDecorator(null);
                editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());
            });

            editorCodeArea.setSyntaxDecorator(createSyntaxDecorator());

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
                    // On click, append text (simple demo action)
                    btn.setOnAction(e -> {
                        String cleanToken = token.startsWith("'") && token.endsWith("'")
                                ? token.substring(1, token.length() - 1)
                                : token;
                        // Insert at caret position logic can be improved later
                        editorCodeArea.insertText(editorCodeArea.getCaretPosition(), cleanToken + " ", null);
                        editorCodeArea.requestFocus();
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

                // Check for errors on this line
                // Note: ANTLR lines are 1-based, paragraphIndex is 0-based
                if (viewModel != null) {
                    for (SyntaxError error : viewModel.getErrors()) {
                        if (error.line() == paragraphIndex + 1) {
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
}
