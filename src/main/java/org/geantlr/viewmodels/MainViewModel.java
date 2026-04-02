package org.geantlr.viewmodels;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jakarta.inject.Singleton;
import jakarta.inject.Inject;
import org.geantlr.services.DynamicGrammar;
import org.geantlr.services.IGrammarLoaderService;
import org.geantlr.services.TokenHighlightMappingService;
import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;

@Singleton
public class MainViewModel {

    private final IGrammarLoaderService grammarLoaderService;

    // Property to keep track of the active editors
    private final ObservableList<EditorViewModel> activeEditors = FXCollections.observableArrayList();

    // Property to hold the currently loaded dynamic grammar
    private final ObjectProperty<DynamicGrammar> dynamicGrammar = new SimpleObjectProperty<>(null);

    // Property to toggle experimental mode
    private final javafx.beans.property.BooleanProperty experimentalMode = new javafx.beans.property.SimpleBooleanProperty(false);

    private final BooleanProperty isLoadingGrammar = new SimpleBooleanProperty(false);

    @Inject
    public MainViewModel(IGrammarLoaderService grammarLoaderService) {
        this.grammarLoaderService = grammarLoaderService;
    }

    public ObservableList<EditorViewModel> getActiveEditors() {
        return activeEditors;
    }

    public void addEditor(EditorViewModel editor) {
        if (activeEditors.size() < 2) {
            activeEditors.add(editor);
        }
    }

    public void removeEditor(EditorViewModel editor) {
        activeEditors.remove(editor);
    }

    public DynamicGrammar getDynamicGrammar() {
        return dynamicGrammar.get();
    }

    public void setDynamicGrammar(DynamicGrammar grammar) {
        this.dynamicGrammar.set(grammar);
    }

    public ObjectProperty<DynamicGrammar> dynamicGrammarProperty() {
        return dynamicGrammar;
    }

    public javafx.beans.property.BooleanProperty experimentalModeProperty() {
        return experimentalMode;
    }

    public boolean isExperimentalMode() {
        return experimentalMode.get();
    }

    public void setExperimentalMode(boolean isExperimentalMode) {
        this.experimentalMode.set(isExperimentalMode);
    }

    // Archie: Added these delegate methods to improve MVVM separation.
    // The View should never talk directly to a Micronaut Domain Service (IGrammarLoaderService).
    // All business logic requests must flow through the ViewModel.
    public void addImportDirectory(File directory) {
        grammarLoaderService.addImportDirectory(directory);
    }

    public void clearImportDirectories() {
        grammarLoaderService.clearImportDirectories();
    }

    public int getImportDirectoriesCount() {
        return grammarLoaderService.getImportDirectories().size();
    }

    public DynamicGrammar loadDynamicGrammar(File grammarFile) throws Exception {
        return grammarLoaderService.loadDynamicGrammar(grammarFile);
    }

    public BooleanProperty isLoadingGrammarProperty() {
        return isLoadingGrammar;
    }

    public boolean isLoadingGrammar() {
        return isLoadingGrammar.get();
    }

    public Task<DynamicGrammar> loadGrammarAsync(File importDir, File parserFile) {
        Task<DynamicGrammar> task = new Task<>() {
            @Override
            protected DynamicGrammar call() throws Exception {
                if (importDir != null) {
                    grammarLoaderService.addImportDirectory(importDir);
                }
                return grammarLoaderService.loadDynamicGrammar(importDir, parserFile);
            }
        };

        task.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_RUNNING, e -> isLoadingGrammar.set(true));
        task.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> {
            isLoadingGrammar.set(false);
            setDynamicGrammar(task.getValue());
        });
        task.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED, e -> isLoadingGrammar.set(false));
        task.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_CANCELLED, e -> isLoadingGrammar.set(false));

        Thread.ofVirtual().start(task);

        return task;
    }
}
