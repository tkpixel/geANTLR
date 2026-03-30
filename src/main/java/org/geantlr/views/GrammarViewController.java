package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import javafx.fxml.FXML;
import jfx.incubator.scene.control.richtext.CodeArea;

@Prototype
public class GrammarViewController {

    @FXML
    private CodeArea grammarCodeArea;

    @FXML
    public void initialize() {
        if (grammarCodeArea != null) {
            grammarCodeArea.setLineNumbersEnabled(true);
            grammarCodeArea.setEditable(false);
            // Apply the same style class as the main editor for consistent dark theme
            grammarCodeArea.getStyleClass().add("editor-code-area");
        }
    }

    public void setGrammarText(String text) {
        if (grammarCodeArea != null && text != null) {
            grammarCodeArea.setText(text);
        }
    }
}
