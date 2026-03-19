package org.geantlr.viewmodels;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.geantlr.services.AntlrGrammarService;
import org.geantlr.services.SyntaxError;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Prototype
public class EditorViewModel {

    private final StringProperty textContent = new SimpleStringProperty("");
    private final ObservableList<SyntaxError> errors = FXCollections.observableArrayList();

    private final AntlrGrammarService antlrGrammarService;
    private final MainViewModel mainViewModel;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    @Inject
    public EditorViewModel(AntlrGrammarService antlrGrammarService, MainViewModel mainViewModel) {
        this.antlrGrammarService = antlrGrammarService;
        this.mainViewModel = mainViewModel;

        debounce.setOnFinished(event -> parseText(textContent.get()));

        textContent.addListener((obs, oldVal, newVal) -> {
            debounce.playFromStart();
        });
    }

    private void parseText(String text) {
        var grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            errors.clear();
            return;
        }

        CompletableFuture.supplyAsync(() -> antlrGrammarService.parseText(grammar, text))
            .thenAccept(syntaxErrors -> {
                Platform.runLater(() -> {
                    errors.setAll(syntaxErrors);
                });
            });
    }

    public ObservableList<SyntaxError> getErrors() {
        return errors;
    }

    public StringProperty textContentProperty() {
        return textContent;
    }

    public String getTextContent() {
        return textContent.get();
    }

    public void setTextContent(String text) {
        this.textContent.set(text);
    }
}
