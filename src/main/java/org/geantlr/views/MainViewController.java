package org.geantlr.views;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.collections.ListChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.geantlr.FxmlControllerFactory;
import org.geantlr.viewmodels.EditorViewModel;
import org.geantlr.viewmodels.MainViewModel;
import org.geantlr.services.IGrammarLoaderService;
import org.geantlr.services.DynamicGrammar;
import org.geantlr.services.TokenHighlightMappingService;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

@Singleton
public class MainViewController {

    @FXML
    private HeaderBar headerBar;

    @FXML
    private SplitPane editorSplitPane;

    @FXML
    private Button toggleEditorsBtn;

    @FXML
    private Button viewGrammarBtn;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private FontIcon themeIcon;

    @FXML
    private ProgressBar mainProgressBar;

    private final MainViewModel viewModel;
    private final IGrammarLoaderService grammarLoaderService;
    private final TokenHighlightMappingService tokenHighlightMappingService;
    private final ApplicationContext context;

    private boolean isDarkMode = true;

    // Track instances
    private final Map<EditorViewModel, Region> editorRegions = new HashMap<>();

    @Inject
    public MainViewController(MainViewModel viewModel, IGrammarLoaderService grammarLoaderService, TokenHighlightMappingService tokenHighlightMappingService, ApplicationContext context) {
        this.viewModel = viewModel;
        this.grammarLoaderService = grammarLoaderService;
        this.tokenHighlightMappingService = tokenHighlightMappingService;
        this.context = context;
    }

    @FXML
    public void initialize() {
        if (mainProgressBar != null) {
            mainProgressBar.visibleProperty().bind(viewModel.isLoadingGrammarProperty());
            mainProgressBar.managedProperty().bind(viewModel.isLoadingGrammarProperty());
        }

        // Use an accent button style if provided by AtlantaFX
        themeToggleBtn.getStyleClass().addAll("accent", atlantafx.base.theme.Styles.BUTTON_ICON);
        themeToggleBtn.setAccessibleText("Toggle Theme");
        themeToggleBtn.setAccessibleHelp("Switches between light and dark themes.");
        themeToggleBtn.setTooltip(new javafx.scene.control.Tooltip("Toggle Theme"));

        if (toggleEditorsBtn != null) {
            toggleEditorsBtn.getStyleClass().addAll(atlantafx.base.theme.Styles.BUTTON_ICON);
            toggleEditorsBtn.setAccessibleText("Toggle Editors");
            toggleEditorsBtn.setAccessibleHelp("Switches between single and split editor views.");
            toggleEditorsBtn.setTooltip(new javafx.scene.control.Tooltip("Toggle Editors"));
        }

        if (viewGrammarBtn != null) {
            viewGrammarBtn.setAccessibleText("View Grammar");
            viewGrammarBtn.setAccessibleHelp("Opens the currently loaded grammar in a new window.");
            viewGrammarBtn.setTooltip(new javafx.scene.control.Tooltip("View Grammar"));
            viewGrammarBtn.disableProperty().bind(viewModel.dynamicGrammarProperty().isNull());
        }

        // Listen to active editors list
        viewModel.getActiveEditors().addListener((ListChangeListener<EditorViewModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (EditorViewModel addedViewModel : change.getAddedSubList()) {
                        Region region = loadEditorView(addedViewModel);
                        editorRegions.put(addedViewModel, region);
                        editorSplitPane.getItems().add(region);
                    }
                }
                if (change.wasRemoved()) {
                    for (EditorViewModel removedViewModel : change.getRemoved()) {
                        Region region = editorRegions.remove(removedViewModel);
                        if (region != null) {
                            editorSplitPane.getItems().remove(region);
                        }
                    }
                }
            }
            if (viewModel.getActiveEditors().size() == 2) {
                Platform.runLater(() -> editorSplitPane.setDividerPositions(0.5));
            }
        });

        // Initialize with 1 editor
        viewModel.addEditor(context.getBean(EditorViewModel.class));
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2m-moon-waning-crescent");
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2w-white-balance-sunny");
        }
    }

    @FXML
    private void toggleExperimentalMode() {
        viewModel.setExperimentalMode(!viewModel.isExperimentalMode());
    }

    @FXML
    private void toggleEditors() {
        if (viewModel.getActiveEditors().size() == 1) {
            viewModel.addEditor(context.getBean(EditorViewModel.class));
        } else if (viewModel.getActiveEditors().size() == 2) {
            viewModel.removeEditor(viewModel.getActiveEditors().get(1));
        }
    }

    @FXML
    private void viewGrammar() {
        if (viewModel.getDynamicGrammar() == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/geantlr/views/GrammarView.fxml"));
            loader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
            javafx.scene.Parent root = loader.load();
            GrammarViewController controller = loader.getController();

            controller.setGrammarText(viewModel.getDynamicGrammar().getRawGrammarText());

            Stage stage = new Stage();
            stage.setTitle("Current Grammar");
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 800);

            // Load custom theme overrides
            String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
            scene.getStylesheets().add(customCss);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Displaying Grammar");
            alert.setHeaderText("Failed to load grammar view");
            alert.setContentText(e.getMessage());
            e.printStackTrace();
            alert.showAndWait();
        }
    }

    @FXML
    private void loadGrammar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/geantlr/views/LoadGrammarDialog.fxml"));
            loader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
            javafx.scene.Parent root = loader.load();
            LoadGrammarDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Load Split Grammars");
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(editorSplitPane.getScene().getWindow());
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (controller.isLoadConfirmed()) {
                File importDir = controller.getImportDir();
                File parserFile = controller.getParserFile();

                Task<DynamicGrammar> loadTask = viewModel.loadGrammarAsync(importDir, parserFile, tokenHighlightMappingService);

                loadTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                    DynamicGrammar dynamicGrammar = loadTask.getValue();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Grammar Loaded");
                    alert.setHeaderText("Success");
                    String rulesMsg = dynamicGrammar.getParserGrammar() != null
                        ? "Parser rules: " + dynamicGrammar.getParserGrammar().rules.size()
                        : "Lexer rules only";

                    alert.setContentText("Grammar '" + parserFile.getName() + "' loaded and compiled successfully.\n" +
                                         rulesMsg);
                    alert.showAndWait();
                });

                loadTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED, e -> {
                    Throwable ex = loadTask.getException();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Loading Grammar");
                    alert.setHeaderText("Failed to load or compile grammar");
                    alert.setContentText(ex != null ? ex.getMessage() : "Unknown error");
                    if (ex != null) ex.printStackTrace();
                    alert.showAndWait();
                });
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Loading Grammar");
            alert.setHeaderText("Failed to load or compile grammar");
            alert.setContentText(e.getMessage());
            e.printStackTrace();
            alert.showAndWait();
        }
    }

    private Region loadEditorView(EditorViewModel editorViewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/geantlr/views/EditorView.fxml"));
            loader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
            Region region = loader.load();
            EditorViewController controller = loader.getController();
            controller.setViewModel(editorViewModel);
            return region;
        } catch (IOException e) {
            e.printStackTrace();
            StackPane pane = new StackPane(new Label("Error loading editor"));
            pane.setStyle("-fx-border-color: red; -fx-background-color: -color-bg-default;");
            return pane;
        }
    }
}
