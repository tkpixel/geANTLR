package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import jfx.incubator.scene.control.richtext.CodeArea;
import org.geantlr.viewmodels.EditorViewModel;

@Prototype
public class EditorViewController {

    @FXML
    private CodeArea editorCodeArea;

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
        }
    }

    public EditorViewModel getViewModel() {
        return viewModel;
    }
}
