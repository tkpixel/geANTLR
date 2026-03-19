package org.geantlr.views;

import javafx.fxml.FXML;
import jfx.incubator.scene.control.richtext.CodeArea;
import org.geantlr.viewmodels.EditorViewModel;

public class EditorViewController {

    @FXML
    private CodeArea editorCodeArea;

    private EditorViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new EditorViewModel();

        // Update view model when CodeArea text changes
        if (editorCodeArea != null) {
            editorCodeArea.getModel().addListener(change -> {
                viewModel.setTextContent(editorCodeArea.getText());
            });

            // Set initial text
            editorCodeArea.setText(viewModel.getTextContent());

            // Listen to view model changes
            viewModel.textContentProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.equals(editorCodeArea.getText())) {
                    editorCodeArea.setText(newVal);
                }
            });
        }
    }

    public EditorViewModel getViewModel() {
        return viewModel;
    }
}
