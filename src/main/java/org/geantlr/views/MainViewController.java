package org.geantlr.views;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import org.geantlr.viewmodels.MainViewModel;
import org.kordamp.ikonli.javafx.FontIcon;

public class MainViewController {

    @FXML
    private HBox titleBar;

    @FXML
    private Button minimizeBtn;

    @FXML
    private Button maximizeBtn;

    @FXML
    private FontIcon maximizeIcon;

    @FXML
    private Button closeBtn;

    @FXML
    private SplitPane editorSplitPane;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private FontIcon themeIcon;

    private MainViewModel viewModel;
    private boolean isDarkMode = true;

    // Regions for editors
    private Region primaryEditor;
    private Region secondaryEditor;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        viewModel = new MainViewModel();

        // Use an accent button style if provided by AtlantaFX
        themeToggleBtn.getStyleClass().addAll("accent");

        setupTitleBar();

        // Create editors from FXML
        primaryEditor = loadEditorView();
        secondaryEditor = loadEditorView();

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

    private void setupTitleBar() {
        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                // Ignore top 5 pixels to allow vertical resizing without moving the window
                if (event.getY() > 5) {
                    xOffset = event.getSceneX();
                    yOffset = event.getSceneY();
                } else {
                    xOffset = -1; // Indicate invalid drag start
                    yOffset = -1;
                }
            }
        });

        titleBar.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && yOffset > 5) {
                Stage stage = (Stage) titleBar.getScene().getWindow();
                // Prevent dragging if maximized
                if (!stage.isMaximized()) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            }
        });

        titleBar.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                maximizeWindow();
            }
        });
    }

    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            maximizeIcon.setIconLiteral("mdi2w-window-maximize");
        } else {
            stage.setMaximized(true);
            maximizeIcon.setIconLiteral("mdi2w-window-restore");
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.fireEvent(new javafx.stage.WindowEvent(stage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void toggleEditors() {
        if (viewModel.getActiveEditorsCount() == 1) {
            viewModel.setActiveEditorsCount(2);
        } else {
            viewModel.setActiveEditorsCount(1);
        }
    }

    private Region loadEditorView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/geantlr/views/EditorView.fxml"));
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to placeholder if FXML fails to load
            StackPane pane = new StackPane(new Label("Error loading editor"));
            pane.setStyle("-fx-border-color: red; -fx-background-color: -color-bg-default;");
            return pane;
        }
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
