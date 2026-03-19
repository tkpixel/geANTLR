package org.geantlr.views;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import org.geantlr.viewmodels.MainViewModel;
import org.kordamp.ikonli.javafx.FontIcon;

public class MainViewController {

    @FXML
    private SplitPane editorSplitPane;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private FontIcon themeIcon;

    private MainViewModel viewModel;
    private boolean isDarkMode = true;

    // Placeholder regions for editors
    private Region primaryEditor;
    private Region secondaryEditor;

    @FXML
    public void initialize() {
        viewModel = new MainViewModel();

        // Use an accent button style if provided by AtlantaFX
        themeToggleBtn.getStyleClass().addAll("accent");

        // Create placeholder editors for now
        primaryEditor = createPlaceholderEditor("Editor 1");
        secondaryEditor = createPlaceholderEditor("Editor 2");

        // Set initial state
        editorSplitPane.getItems().add(primaryEditor);

        // Listen to changes in the active editors count
        viewModel.activeEditorsCountProperty().addListener((obs, oldVal, newVal) -> {
            updateEditorsView(newVal.intValue());
        });

        // We will initialize with 1 editor, but if it needs 2, we update
        updateEditorsView(viewModel.getActiveEditorsCount());
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2w-white-balance-sunny");
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2m-moon-waning-crescent");
        }
    }

    @FXML
    private void toggleEditors() {
        if (viewModel.getActiveEditorsCount() == 1) {
            viewModel.setActiveEditorsCount(2);
        } else {
            viewModel.setActiveEditorsCount(1);
        }
    }

    private Region createPlaceholderEditor(String text) {
        StackPane pane = new StackPane(new Label(text));
        pane.setStyle("-fx-border-color: gray; -fx-background-color: -color-bg-default;");
        return pane;
    }

    private void updateEditorsView(int count) {
        if (count == 1) {
            if (editorSplitPane.getItems().contains(secondaryEditor)) {
                editorSplitPane.getItems().remove(secondaryEditor);
            }
            if (!editorSplitPane.getItems().contains(primaryEditor)) {
                editorSplitPane.getItems().add(0, primaryEditor);
            }
        } else if (count == 2) {
            if (!editorSplitPane.getItems().contains(primaryEditor)) {
                editorSplitPane.getItems().add(0, primaryEditor);
            }
            if (!editorSplitPane.getItems().contains(secondaryEditor)) {
                editorSplitPane.getItems().add(secondaryEditor);
                // After adding the second editor, set the divider position
                javafx.application.Platform.runLater(() -> {
                    editorSplitPane.setDividerPositions(0.5);
                });
            }
        }
    }
}
