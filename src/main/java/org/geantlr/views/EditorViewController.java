package org.geantlr.views;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.geantlr.viewmodels.EditorViewModel;

public class EditorViewController {

    @FXML
    private TextArea editorTextArea;

    private EditorViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new EditorViewModel();

        // Bind the text area text property to the view model's text content property bidirectionally
        if (editorTextArea != null) {
            editorTextArea.textProperty().bindBidirectional(viewModel.textContentProperty());
        }
    }

    public EditorViewModel getViewModel() {
        return viewModel;
    }
}
