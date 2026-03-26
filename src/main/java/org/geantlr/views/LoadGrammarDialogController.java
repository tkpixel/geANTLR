package org.geantlr.views;

import io.micronaut.context.annotation.Prototype;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

@Prototype
public class LoadGrammarDialogController {

    @FXML private TextField importDirField;
    @FXML private TextField parserFileField;
    @FXML private Button loadBtn;

    private File importDir;
    private File parserFile;

    private boolean loadConfirmed = false;

    @FXML
    public void initialize() {
        parserFileField.textProperty().addListener((obs, oldV, newV) -> {
            loadBtn.setDisable(newV == null || newV.isEmpty());
        });
    }

    @FXML
    private void selectImportDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Import Directory");

        // Auto-select parent of parser if available
        if (parserFile != null && parserFile.getParentFile() != null) {
            dirChooser.setInitialDirectory(parserFile.getParentFile());
        }

        File selectedDir = dirChooser.showDialog(getStage());
        if (selectedDir != null) {
            importDir = selectedDir;
            importDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void selectParserFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Parser or Combined Grammar");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ANTLR Grammar (*.g4)", "*.g4"));

        if (importDir != null) fileChooser.setInitialDirectory(importDir);

        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            parserFile = selectedFile;
            parserFileField.setText(selectedFile.getAbsolutePath());

            // Auto-populate import dir if not set
            if (importDir == null && parserFile.getParentFile() != null) {
                importDir = parserFile.getParentFile();
                importDirField.setText(importDir.getAbsolutePath());
            }
        }
    }

    @FXML
    private void cancel() {
        loadConfirmed = false;
        closeStage();
    }

    @FXML
    private void load() {
        loadConfirmed = true;
        closeStage();
    }

    private Stage getStage() {
        return (Stage) importDirField.getScene().getWindow();
    }

    private void closeStage() {
        getStage().close();
    }

    public File getImportDir() { return importDir; }
    public File getParserFile() { return parserFile; }
    public boolean isLoadConfirmed() { return loadConfirmed; }
}
