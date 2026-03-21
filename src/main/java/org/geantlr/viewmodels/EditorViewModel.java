package org.geantlr.viewmodels;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.geantlr.services.AntlrGrammarService;
import org.geantlr.services.CodeCompletionService;
import org.geantlr.services.CodeFormattingService;
import org.geantlr.services.ParseResult;
import org.geantlr.services.SyntaxError;
import org.antlr.v4.runtime.Token;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Prototype
public class EditorViewModel {

    private final StringProperty textContent = new SimpleStringProperty("");
    private final javafx.beans.property.IntegerProperty fontSize = new javafx.beans.property.SimpleIntegerProperty(13);
    private final ObservableList<SyntaxError> errors = FXCollections.observableArrayList();
    private final ObservableList<Token> tokens = FXCollections.observableArrayList();
    private final ObservableList<String> suggestedTokens = FXCollections.observableArrayList();

    public record TokenStyle(int startInLine, int endInLine, String symbolicName) {}
    private java.util.Map<Integer, java.util.List<TokenStyle>> tokenStylesByLine = new java.util.HashMap<>();

    public record InsertTextCommand(String text) {}

    private final ObjectProperty<InsertTextCommand> insertTextCommand = new SimpleObjectProperty<>();

    private boolean isUpdating = false;
    private int currentCaretPosition = 0;

    private final AntlrGrammarService antlrGrammarService;
    private final CodeCompletionService codeCompletionService;
    private final CodeFormattingService codeFormattingService;
    private final MainViewModel mainViewModel;

    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    public record ReplaceTextCommand(String text) {}
    private final ObjectProperty<ReplaceTextCommand> replaceTextCommand = new SimpleObjectProperty<>();

    @Inject
    public EditorViewModel(AntlrGrammarService antlrGrammarService, CodeCompletionService codeCompletionService, CodeFormattingService codeFormattingService, MainViewModel mainViewModel) {
        this.antlrGrammarService = antlrGrammarService;
        this.codeCompletionService = codeCompletionService;
        this.codeFormattingService = codeFormattingService;
        this.mainViewModel = mainViewModel;

        debounce.setOnFinished(event -> parseText(textContent.get()));

        textContent.addListener((obs, oldVal, newVal) -> {
            debounce.playFromStart();
            updateSuggestions();
        });
    }

    private void parseText(String text) {
        var grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            errors.clear();
            tokens.clear();
            return;
        }

        CompletableFuture.supplyAsync(() -> antlrGrammarService.parseText(grammar, text))
            .thenAccept(parseResult -> {
                // Compute the token styles on the background thread
                var newStyles = computeTokenStyles(parseResult.tokens(), grammar.getVocabulary());

                Platform.runLater(() -> {
                    errors.setAll(parseResult.errors());
                    tokenStylesByLine = newStyles;
                    if (parseResult.tokens() != null) {
                        tokens.setAll(parseResult.tokens());
                    } else {
                        tokens.clear();
                    }
                });
            });
    }

    private java.util.Map<Integer, java.util.List<TokenStyle>> computeTokenStyles(List<Token> tokenList, org.antlr.v4.runtime.Vocabulary vocab) {
        java.util.Map<Integer, java.util.List<TokenStyle>> styles = new java.util.HashMap<>();
        if (tokenList == null || vocab == null) return styles;

        for (Token token : tokenList) {
            String symbolicName = vocab.getSymbolicName(token.getType());
            if (symbolicName == null) continue;

            String text = token.getText();
            if (text == null) continue;

            int startLine = token.getLine();
            int startCharPos = token.getCharPositionInLine();

            String[] lines = text.split("\r?\n", -1);

            for (int i = 0; i < lines.length; i++) {
                int currentLine = startLine + i;
                int startInLine = (i == 0) ? startCharPos : 0;
                int endInLine = startInLine + lines[i].length();

                if (startInLine < endInLine) {
                    styles.computeIfAbsent(currentLine, k -> new java.util.ArrayList<>())
                          .add(new TokenStyle(startInLine, endInLine, symbolicName));
                }
            }
        }
        return styles;
    }

    public ObservableList<SyntaxError> getErrors() {
        return errors;
    }

    public ObservableList<Token> getTokens() {
        return tokens;
    }

    public org.antlr.v4.runtime.Vocabulary getVocabulary() {
        var grammar = mainViewModel.getDynamicGrammar();
        return grammar != null ? grammar.getVocabulary() : null;
    }

    public java.util.List<TokenStyle> getTokenStylesForLine(int line) {
        return tokenStylesByLine.getOrDefault(line, java.util.Collections.emptyList());
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

    public javafx.beans.property.IntegerProperty fontSizeProperty() {
        return fontSize;
    }

    public int getFontSize() {
        return fontSize.get();
    }

    public void setFontSize(int size) {
        this.fontSize.set(size);
    }

    public ObservableList<String> getSuggestedTokens() {
        return suggestedTokens;
    }

    public ObjectProperty<InsertTextCommand> insertTextCommandProperty() {
        return insertTextCommand;
    }

    public ObjectProperty<ReplaceTextCommand> replaceTextCommandProperty() {
        return replaceTextCommand;
    }

    public void clearReplaceTextCommand() {
        replaceTextCommand.set(null);
    }

    public void formatCode() {
        var grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) return;

        String currentText = textContent.get();
        CompletableFuture.supplyAsync(() -> codeFormattingService.formatCode(grammar, currentText))
            .thenAccept(formattedText -> {
                Platform.runLater(() -> {
                    if (formattedText != null && !formattedText.equals(currentText)) {
                        setUpdating(true);
                        replaceTextCommand.set(new ReplaceTextCommand(formattedText));
                    }
                });
            });
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setUpdating(boolean updating) {
        isUpdating = updating;
    }

    public void insertBaustein(String token) {
        String cleanToken = token.startsWith("'") && token.endsWith("'")
                ? token.substring(1, token.length() - 1)
                : token;
        setUpdating(true);
        insertTextCommand.set(new InsertTextCommand(cleanToken));
    }

    public void clearInsertTextCommand() {
        insertTextCommand.set(null);
    }

    public void onCaretPositionChanged(int caretPosition) {
        this.currentCaretPosition = caretPosition;
        updateSuggestions();
    }

    private void updateSuggestions() {
        var grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            Platform.runLater(suggestedTokens::clear);
            return;
        }
        String text = textContent.get();
        int caretPosition = this.currentCaretPosition;
        CompletableFuture.supplyAsync(() -> codeCompletionService.getSuggestedTokens(grammar, text, caretPosition))
            .thenAccept(suggestions -> Platform.runLater(() -> suggestedTokens.setAll(suggestions)));
    }
}
