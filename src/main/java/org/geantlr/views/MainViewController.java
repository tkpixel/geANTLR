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
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.collections.ListChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.geantlr.FxmlControllerFactory;
import org.geantlr.viewmodels.EditorViewModel;
import org.geantlr.viewmodels.MainViewModel;
import org.geantlr.services.IGrammarLoaderService;
import org.geantlr.services.TokenHighlightMappingService;
import org.geantlr.services.DynamicGrammar;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceDialog;
import java.nio.file.Files;
import java.util.Optional;
import javafx.application.Platform;

@Singleton
public class MainViewController {

    @FXML
    private HeaderBar headerBar;

    @FXML
    private SplitPane editorSplitPane;

    @FXML
    private Button loadGrammarButton;

    @FXML
    private MenuItem viewGrammarMenuItem;

    @FXML
    private Button loadDomainModelButton;


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
        if (loadGrammarButton != null) {
            javafx.scene.Node originalGraphic = loadGrammarButton.getGraphic();
            viewModel.isLoadingGrammarProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
                    spinner.setPrefSize(16, 16);
                    loadGrammarButton.setGraphic(spinner);
                } else {
                    loadGrammarButton.setGraphic(originalGraphic);
                }
            });
            loadGrammarButton.disableProperty().bind(viewModel.isLoadingGrammarProperty());
        }

        if (mainProgressBar != null) {
            updateMainProgressBarBinding();
            viewModel.getActiveEditors().addListener((ListChangeListener<EditorViewModel>) change -> {
                updateMainProgressBarBinding();
            });
        }

        if (viewGrammarMenuItem != null) {
            viewGrammarMenuItem.disableProperty().bind(viewModel.dynamicGrammarProperty().isNull());
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

    private static final String LIGHT_CSS_RESOURCE = "/org/geantlr/theme-light.css";

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2m-moon-waning-crescent");
            // Light-Override-CSS entfernen
            if (editorSplitPane.getScene() != null) {
                String lightCss = getClass().getResource(LIGHT_CSS_RESOURCE) != null
                    ? getClass().getResource(LIGHT_CSS_RESOURCE).toExternalForm() : null;
                if (lightCss != null) {
                    editorSplitPane.getScene().getStylesheets().remove(lightCss);
                }
            }
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            themeIcon.setIconLiteral("mdi2w-white-balance-sunny");
            // Light-Override-CSS hinzufügen
            if (editorSplitPane.getScene() != null) {
                java.net.URL lightCssUrl = getClass().getResource(LIGHT_CSS_RESOURCE);
                if (lightCssUrl != null) {
                    String lightCss = lightCssUrl.toExternalForm();
                    if (!editorSplitPane.getScene().getStylesheets().contains(lightCss)) {
                        editorSplitPane.getScene().getStylesheets().add(lightCss);
                    }
                }
            }
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
    private void downloadContent() {
        int numEditors = viewModel.getActiveEditors().size();
        if (numEditors == 0) {
            return;
        }

        if (numEditors == 1) {
            saveEditorContent(viewModel.getActiveEditors().get(0));
        } else {
            Map<String, EditorViewModel> choices = new LinkedHashMap<>();
            choices.put("Left Editor", viewModel.getActiveEditors().get(0));
            choices.put("Right Editor", viewModel.getActiveEditors().get(1));

            ChoiceDialog<String> stringDialog = new ChoiceDialog<>(
                    "Left Editor", choices.keySet());
            stringDialog.setTitle("Select Editor");
            stringDialog.setHeaderText("Multiple editors are open.");
            stringDialog.setContentText("Choose which editor's content to download:");

            Optional<String> result = stringDialog.showAndWait();
            if (result.isPresent()) {
                saveEditorContent(choices.get(result.get()));
            }
        }
    }

    private void saveEditorContent(EditorViewModel editor) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Content");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Java Files", "*.java"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showSaveDialog(editorSplitPane.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), editor.getTextContent());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File Saved");
                alert.setHeaderText(null);
                alert.setContentText("Content saved to " + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Saving File");
                alert.setHeaderText("Failed to save content to file");
                alert.setContentText(e.getMessage());
                e.printStackTrace();
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void loadDomainModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Domain Model (.puml)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PlantUML Files", "*.puml", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(loadDomainModelButton.getScene().getWindow());
        if (selectedFile != null) {
            boolean dialogAttached = false;
            for (EditorViewModel editor : viewModel.getActiveEditors()) {
                Task<Void> parseTask = editor.loadDomainModel(selectedFile);
                if (parseTask != null && !dialogAttached) {
                    // Attach to only the first task to avoid multiple dialogs for the same file load
                    parseTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                        Platform.runLater(() -> showMessageDialog("Domain Model Loaded", "Success", "PlantUML domain model loaded successfully."));
                    });
                    parseTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED, e -> {
                        Throwable ex = parseTask.getException();
                        Platform.runLater(() -> showMessageDialog("Error Loading Domain Model", "Failed to load PlantUML file", ex != null ? ex.getMessage() : "Unknown error"));
                    });
                    dialogAttached = true;
                }
            }
        }
    }

    private void updateMainProgressBarBinding() {
        if (mainProgressBar == null) return;

        mainProgressBar.visibleProperty().unbind();
        mainProgressBar.managedProperty().unbind();

        // anyParsing: true wenn irgendein Editor gerade das Domain Model parst
        javafx.beans.binding.BooleanBinding anyParsing = new javafx.beans.binding.BooleanBinding() {
            {
                super.bind(viewModel.isLoadingGrammarProperty());
                for (EditorViewModel editor : viewModel.getActiveEditors()) {
                    super.bind(editor.isParsingDomainModelProperty());
                }
            }

            @Override
            protected boolean computeValue() {
                if (viewModel.isLoadingGrammarProperty().get()) return true;
                for (EditorViewModel editor : viewModel.getActiveEditors()) {
                    if (editor.isParsingDomainModelProperty().get()) return true;
                }
                return false;
            }
        };

        mainProgressBar.visibleProperty().bind(anyParsing);
        mainProgressBar.managedProperty().bind(anyParsing);
    }

    private void showMessageDialog(String title, String header, String content) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> showMessageDialog(title, header, content));
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/geantlr/views/MessageDialog.fxml"));
            loader.setControllerFactory(context.getBean(FxmlControllerFactory.class));
            javafx.scene.Parent root = loader.load();
            MessageDialogController controller = loader.getController();

            controller.setDialogInfo(title, header, content);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initStyle(javafx.stage.StageStyle.DECORATED);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (editorSplitPane != null && editorSplitPane.getScene() != null) {
                stage.initOwner(editorSplitPane.getScene().getWindow());
            }

            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root, 400, 250);

            // Apply current stylesheets
            javafx.scene.Scene mainScene = editorSplitPane.getScene();
            if (mainScene != null) {
                dialogScene.getStylesheets().setAll(mainScene.getStylesheets());
                mainScene.getStylesheets().addListener(
                    (javafx.collections.ListChangeListener<String>) _ ->
                        dialogScene.getStylesheets().setAll(mainScene.getStylesheets())
                );
            }

            stage.setScene(dialogScene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback to standard alert if custom dialog fails
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
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
            stage.initStyle(javafx.stage.StageStyle.DECORATED);
            javafx.scene.Scene grammarScene = new javafx.scene.Scene(root, 600, 800);

            // Stylesheets der Haupt-Scene übernehmen (inkl. theme-light.css wenn aktiv)
            javafx.scene.Scene mainScene = editorSplitPane.getScene();
            if (mainScene != null) {
                grammarScene.getStylesheets().setAll(mainScene.getStylesheets());
                // Bei Theme-Wechsel synchron halten
                mainScene.getStylesheets().addListener(
                    (javafx.collections.ListChangeListener<String>) _ ->
                        grammarScene.getStylesheets().setAll(mainScene.getStylesheets())
                );
            } else {
                // Fallback: nur theme.css
                String customCss = getClass().getResource("/org/geantlr/theme.css").toExternalForm();
                grammarScene.getStylesheets().add(customCss);
            }

            stage.setScene(grammarScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showMessageDialog("Error Displaying Grammar", "Failed to load grammar view", e.getMessage());
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
            dialogStage.initStyle(javafx.stage.StageStyle.EXTENDED);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(editorSplitPane.getScene().getWindow());
            javafx.scene.Scene dialogScene = new javafx.scene.Scene(root);
            // Stylesheets der Haupt-Scene übernehmen (inkl. theme-light.css wenn aktiv)
            javafx.scene.Scene mainScene = editorSplitPane.getScene();
            if (mainScene != null) {
                dialogScene.getStylesheets().setAll(mainScene.getStylesheets());
                mainScene.getStylesheets().addListener(
                    (javafx.collections.ListChangeListener<String>) _ ->
                        dialogScene.getStylesheets().setAll(mainScene.getStylesheets())
                );
            }
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();

            if (controller.isLoadConfirmed()) {
                File importDir = controller.getImportDir();
                File parserFile = controller.getParserFile();

                Task<DynamicGrammar> loadTask = viewModel.loadGrammarAsync(importDir, parserFile);

                loadTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
                    DynamicGrammar dynamicGrammar = loadTask.getValue();
                    if (dynamicGrammar != null) {
                        tokenHighlightMappingService.buildVocabularyMapping(dynamicGrammar.getVocabulary());
                    }
                    String rulesMsg = dynamicGrammar.getParserGrammar() != null
                        ? "Parser rules: " + dynamicGrammar.getParserGrammar().rules.size()
                        : "Lexer rules only";
                    String content = "Grammar '" + parserFile.getName() + "' loaded and compiled successfully.\n" + rulesMsg;
                    Platform.runLater(() -> showMessageDialog("Grammar Loaded", "Success", content));
                });

                loadTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED, e -> {
                    Throwable ex = loadTask.getException();
                    if (ex != null) ex.printStackTrace();
                    Platform.runLater(() -> showMessageDialog("Error Loading Grammar", "Failed to load or compile grammar", ex != null ? ex.getMessage() : "Unknown error"));
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showMessageDialog("Error Loading Grammar", "Failed to load or compile grammar", e.getMessage()));
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
