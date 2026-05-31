package org.geantlr.viewmodels;

import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.geantlr.services.AntlrGrammarService;
import org.geantlr.services.CodeCompletionService;
import org.geantlr.services.CodeFormattingService;
import org.geantlr.services.SyntaxError;
import org.antlr.v4.runtime.Token;
import javafx.application.Platform;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Prototype
public class EditorViewModel {

    private static final Logger LOG = Logger.getLogger(EditorViewModel.class.getName());

    private final StringProperty textContent = new SimpleStringProperty("");
    private final IntegerProperty fontSize = new SimpleIntegerProperty(13);
    private final ObservableList<SyntaxError> errors = FXCollections.observableArrayList();
    private final ObservableList<Token> tokens = FXCollections.observableArrayList();
    private final ObservableList<String> suggestedTokens = FXCollections.observableArrayList();

    public record TokenStyle(int startInLine, int endInLine, String symbolicName, int tokenType, String text) {}
    private java.util.Map<Integer, java.util.List<TokenStyle>> tokenStylesByLine = new java.util.HashMap<>();

    public record InsertTextCommand(String text) {}

    private final ObjectProperty<InsertTextCommand> insertTextCommand = new SimpleObjectProperty<>();

    private final BooleanProperty isGenerating = new SimpleBooleanProperty(false);
    private final BooleanProperty isParsingDomainModel = new SimpleBooleanProperty(false);

    private final StringProperty referenceTemplate = new SimpleStringProperty("");
    private final StringProperty referenceTemplateName = new SimpleStringProperty("");

    private final StringProperty searchText = new SimpleStringProperty("");
    private final BooleanProperty searchMatchCase = new SimpleBooleanProperty(false);
    private final BooleanProperty searchWholeWord = new SimpleBooleanProperty(false);
    private final BooleanProperty searchRegex = new SimpleBooleanProperty(false);
    private final StringProperty searchCountText = new SimpleStringProperty("No results");
    private final IntegerProperty currentMatchIndex = new SimpleIntegerProperty(-1);

    public record MatchedBracketsRecord(int openIndex, int closeIndex, char bracketChar) {}
    private final ObjectProperty<MatchedBracketsRecord> matchedBrackets = new SimpleObjectProperty<>(null);

    public record SearchMatch(int start, int end) {}
    private final ObservableList<SearchMatch> searchMatches = FXCollections.observableArrayList();

    private final ObservableList<String> availableOllamaModels = FXCollections.observableArrayList();
    private final ObjectProperty<String> selectedOllamaModel = new SimpleObjectProperty<>();

    private final BooleanProperty isTyping = new SimpleBooleanProperty(false);

    private boolean isUpdating = false;
    private int currentCaretPosition = 0;

    private final AntlrGrammarService antlrGrammarService;
    private final CodeCompletionService codeCompletionService;
    private final CodeFormattingService codeFormattingService;
    private final MainViewModel mainViewModel;
    private final org.geantlr.services.RuleGenerationService ruleGenerationService;
    private final org.geantlr.services.PlantUmlParsingService plantUmlParsingService;

    public BooleanProperty experimentalModeProperty() {
        return mainViewModel != null ? mainViewModel.experimentalModeProperty() : null;
    }


    public record ReplaceTextCommand(String text) {}
    private final ObjectProperty<ReplaceTextCommand> replaceTextCommand = new SimpleObjectProperty<>();

    @Inject
    public EditorViewModel(AntlrGrammarService antlrGrammarService, CodeCompletionService codeCompletionService, CodeFormattingService codeFormattingService, MainViewModel mainViewModel, org.geantlr.services.RuleGenerationService ruleGenerationService, org.geantlr.services.PlantUmlParsingService plantUmlParsingService) {
        this.antlrGrammarService = antlrGrammarService;
        this.codeCompletionService = codeCompletionService;
        this.codeFormattingService = codeFormattingService;
        this.mainViewModel = mainViewModel;
        this.ruleGenerationService = ruleGenerationService;
        this.plantUmlParsingService = plantUmlParsingService;





        loadAvailableOllamaModels();
    }

    public void executeSearch() {
        String query = searchText.get();
        String text = textContent.get();

        searchMatches.clear();
        currentMatchIndex.set(-1);

        if (query == null || query.isEmpty() || text == null || text.isEmpty()) {
            updateSearchCount();
            return;
        }

        try {
            String patternString = query;
            if (!searchRegex.get()) {
                patternString = java.util.regex.Pattern.quote(query);
            }
            if (searchWholeWord.get()) {
                patternString = "\\b" + patternString + "\\b";
            }

            int flags = 0;
            if (!searchMatchCase.get()) {
                flags |= java.util.regex.Pattern.CASE_INSENSITIVE;
            }

            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString, flags);
            java.util.regex.Matcher matcher = pattern.matcher(text);

            java.util.List<SearchMatch> matches = new java.util.ArrayList<>();
            while (matcher.find()) {
                matches.add(new SearchMatch(matcher.start(), matcher.end()));
            }
            searchMatches.setAll(matches);

            if (!searchMatches.isEmpty()) {
                currentMatchIndex.set(0);
            }
            updateSearchCount();
        } catch (java.util.regex.PatternSyntaxException e) {
            searchCountText.set("Invalid Regex");
        }
    }

    private void updateSearchCount() {
        if (searchMatches.isEmpty()) {
            searchCountText.set("No results");
        } else {
            searchCountText.set((currentMatchIndex.get() + 1) + " of " + searchMatches.size());
        }
    }

    public void searchNext() {
        if (searchMatches.isEmpty()) return;
        int nextIndex = (currentMatchIndex.get() + 1) % searchMatches.size();
        currentMatchIndex.set(nextIndex);
        updateSearchCount();
    }

    public void searchPrevious() {
        if (searchMatches.isEmpty()) return;
        int prevIndex = currentMatchIndex.get() - 1;
        if (prevIndex < 0) {
            prevIndex = searchMatches.size() - 1;
        }
        currentMatchIndex.set(prevIndex);
        updateSearchCount();
    }

    public StringProperty searchTextProperty() { return searchText; }
    public String getSearchText() { return searchText.get(); }
    public void setSearchText(String text) { this.searchText.set(text); }

    public BooleanProperty searchMatchCaseProperty() { return searchMatchCase; }
    public BooleanProperty searchWholeWordProperty() { return searchWholeWord; }
    public BooleanProperty searchRegexProperty() { return searchRegex; }

    public StringProperty searchCountTextProperty() { return searchCountText; }

    public ObservableList<SearchMatch> getSearchMatches() { return searchMatches; }
    public IntegerProperty currentMatchIndexProperty() { return currentMatchIndex; }

    private void loadAvailableOllamaModels() {
        CompletableFuture.runAsync(() -> {
            java.util.List<String> models = new java.util.ArrayList<>();
            String userHome = System.getProperty("user.home");
            java.io.File ollamaModelsDir = new java.io.File(userHome, ".ollama/models/manifests/registry.ollama.ai/library");

            if (ollamaModelsDir.exists() && ollamaModelsDir.isDirectory()) {
                java.io.File[] directories = ollamaModelsDir.listFiles(java.io.File::isDirectory);
                if (directories != null) {
                    for (java.io.File modelDir : directories) {
                        String modelName = modelDir.getName();
                        java.io.File[] tags = modelDir.listFiles(java.io.File::isFile);
                        if (tags != null) {
                            for (java.io.File tag : tags) {
                                models.add(modelName + ":" + tag.getName());
                            }
                        } else {
                            models.add(modelName);
                        }
                    }
                }
            }

            // Fallback
            if (models.isEmpty()) {
                models.add("qwen2.5-coder:7b");
                models.add("llama3");
            }

            Platform.runLater(() -> {
                availableOllamaModels.setAll(models);
            });
        });
    }


    public void triggerParse() {
        parseText(textContent.get());
        isTyping.set(false);
        updateSuggestions();
    }

    private void parseText(String text) {
        if (mainViewModel == null) return;
        var grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            errors.clear();
            tokens.clear();
            return;
        }

        CompletableFuture.supplyAsync(() -> antlrGrammarService.parseText(grammar, text))
            .thenAccept(parseResult -> {
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
            String text = token.getText();
            if (text == null) continue;

            String symbolicName = vocab.getSymbolicName(token.getType());
            int startLine = token.getLine();
            int startCharPos = token.getCharPositionInLine();

            // Performance Optimization: Fast-path for single-line tokens to avoid regex compilation and array allocation
            if (text.indexOf('\n') == -1) {
                int startInLine = startCharPos;
                int endInLine = startInLine + text.length();
                if (startInLine < endInLine) {
                    styles.computeIfAbsent(startLine, k -> new java.util.ArrayList<>())
                          .add(new TokenStyle(startInLine, endInLine, symbolicName, token.getType(), text));
                }
            } else {
                // Fallback for multi-line tokens (e.g. block comments)
                String[] lines = text.split("\r?\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    int currentLine = startLine + i;
                    int startInLine = (i == 0) ? startCharPos : 0;
                    int endInLine = startInLine + lines[i].length();
                    if (startInLine < endInLine) {
                        styles.computeIfAbsent(currentLine, k -> new java.util.ArrayList<>())
                              .add(new TokenStyle(startInLine, endInLine, symbolicName, token.getType(), token.getText()));
                    }
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
        if (mainViewModel == null) return null;
        var grammar = mainViewModel.getDynamicGrammar();
        return grammar != null ? grammar.getVocabulary() : null;
    }

    public java.util.List<TokenStyle> getTokenStylesForLine(int line) {
        return tokenStylesByLine.getOrDefault(line, java.util.Collections.emptyList());
    }

    public SyntaxError getErrorAt(int line, int charPositionInLine) {
        for (SyntaxError error : errors) {
            if (error.line() == line) {
                int start = error.charPositionInLine();
                int end = start + Math.max(1, error.length());
                if (charPositionInLine >= start && charPositionInLine < end) {
                    return error;
                }
            }
        }
        return null;
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

    public IntegerProperty fontSizeProperty() {
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

    public BooleanProperty isGeneratingProperty() {
        return isGenerating;
    }

    public BooleanProperty isTypingProperty() {
        return isTyping;
    }

    public boolean isGenerating() {
        return isGenerating.get();
    }

    public void clearReplaceTextCommand() {
        replaceTextCommand.set(null);
    }

    public void formatCode() {
        if (mainViewModel == null) return;
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
        // Suggestions will be refreshed via the text change → debounce → updateSuggestions chain
    }

    public void clearInsertTextCommand() {
        insertTextCommand.set(null);
    }

    public void onCaretPositionChanged(int caretPosition) {
        this.currentCaretPosition = caretPosition;
        updateSuggestions();
        updateMatchedBrackets(caretPosition);
    }

    public ObjectProperty<MatchedBracketsRecord> matchedBracketsProperty() {
        return matchedBrackets;
    }

    private void updateMatchedBrackets(int caretPosition) {
        String text = textContent.get();
        if (text == null || text.isEmpty() || caretPosition < 0 || caretPosition > text.length()) {
            matchedBrackets.set(null);
            return;
        }

        // 1. Check if caret is exactly at a bracket, or right after one (highest priority)
        int charIdx = -1;
        char bracketChar = '\0';

        // Prefer char to the right
        if (caretPosition < text.length()) {
            char c = text.charAt(caretPosition);
            if (isBracket(c)) {
                charIdx = caretPosition;
                bracketChar = c;
            }
        }

        // Fallback to char to the left
        if (charIdx == -1 && caretPosition > 0) {
            char c = text.charAt(caretPosition - 1);
            if (isBracket(c)) {
                charIdx = caretPosition - 1;
                bracketChar = c;
            }
        }

        if (charIdx != -1) {
            int matchIdx = findMatchingBracket(text, charIdx, bracketChar);
            if (matchIdx != -1) {
                int openIdx = Math.min(charIdx, matchIdx);
                int closeIdx = Math.max(charIdx, matchIdx);
                char actualBracketChar = text.charAt(openIdx);
                matchedBrackets.set(new MatchedBracketsRecord(openIdx, closeIdx, actualBracketChar));
                return;
            }
        }

        // 2. Caret is not on a bracket – find the innermost enclosing curly-brace block
        //    so that the IntelliJ-style guide line is always visible inside a block.
        int enclosingOpen = findEnclosingCurlyBrace(text, caretPosition);
        if (enclosingOpen != -1) {
            int enclosingClose = findMatchingBracket(text, enclosingOpen, '{');
            if (enclosingClose != -1) {
                matchedBrackets.set(new MatchedBracketsRecord(enclosingOpen, enclosingClose, '{'));
                return;
            }
        }

        matchedBrackets.set(null);
    }

    /**
     * Walks backwards from {@code caretPosition} to find the nearest unmatched {@code '{'}.
     * Returns its index in {@code text}, or -1 if none found.
     */
    private int findEnclosingCurlyBrace(String text, int caretPosition) {
        int depth = 0;
        for (int i = caretPosition - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '}') {
                depth++;
            } else if (c == '{') {
                if (depth == 0) {
                    return i;
                }
                depth--;
            }
        }
        return -1;
    }

    private boolean isBracket(char c) {
        return c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']' || c == '`';
    }

    private int findMatchingBracket(String text, int startIdx, char bracketChar) {
        char openChar, closeChar;
        boolean forward;

        switch (bracketChar) {
            case '(': openChar = '('; closeChar = ')'; forward = true; break;
            case ')': openChar = ')'; closeChar = '('; forward = false; break;
            case '{': openChar = '{'; closeChar = '}'; forward = true; break;
            case '}': openChar = '}'; closeChar = '{'; forward = false; break;
            case '[': openChar = '['; closeChar = ']'; forward = true; break;
            case ']': openChar = ']'; closeChar = '['; forward = false; break;
            case '`':
                // Backticks are symmetric. We try searching forward first.
                // If we don't find it, we search backward.
                int nextMatch = text.indexOf('`', startIdx + 1);
                if (nextMatch != -1) return nextMatch;
                return text.lastIndexOf('`', startIdx - 1);
            default: return -1;
        }

        int count = 1;
        int step = forward ? 1 : -1;
        int idx = startIdx + step;

        while (idx >= 0 && idx < text.length()) {
            char c = text.charAt(idx);
            if (c == closeChar) {
                count--;
                if (count == 0) return idx;
            } else if (c == openChar) {
                count++;
            }
            idx += step;
        }

        return -1;
    }

    private void updateSuggestions() {
        if (mainViewModel == null) return;
        var grammar = mainViewModel.getDynamicGrammar();
        String text = textContent.get();
        int caretPosition = this.currentCaretPosition;
        CompletableFuture.supplyAsync(() -> codeCompletionService.getSuggestedTokens(grammar, text, caretPosition))
            .thenAccept(suggestions -> Platform.runLater(() -> suggestedTokens.setAll(suggestions)));
    }


    public StringProperty referenceTemplateNameProperty() {
        return referenceTemplateName;
    }

    public ObservableList<String> getAvailableOllamaModels() {
        return availableOllamaModels;
    }

    public ObjectProperty<String> selectedOllamaModelProperty() {
        return selectedOllamaModel;
    }

    public void generateRuleFromText(String naturalLanguagePrompt) {
        if (isGenerating.get() || naturalLanguagePrompt == null || naturalLanguagePrompt.isBlank() || selectedOllamaModel.get() == null) {
            return;
        }

        isGenerating.set(true);

        final String templateContent = referenceTemplate.get();
        final String modelName = selectedOllamaModel.get();

        javafx.concurrent.Task<String> generationTask = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                return ruleGenerationService.generateRule(naturalLanguagePrompt, templateContent, modelName);
            }
        };

        generationTask.setOnSucceeded(_ -> {
            isGenerating.set(false);
            String result = generationTask.getValue();
            if (result != null && !result.isEmpty()) {
                setUpdating(true);
                insertTextCommand.set(new InsertTextCommand(result));
            }
        });

        generationTask.setOnFailed(_ -> {
            isGenerating.set(false);
            Throwable e = generationTask.getException();
            if (e != null) {
                LOG.severe("Rule generation failed: " + e.getMessage());
            }
        });

        Thread.ofVirtual().start(generationTask);
    }

    public javafx.concurrent.Task<Void> loadDomainModel(java.io.File file) {
        if (file == null || !file.exists()) return null;

        javafx.concurrent.Task<Void> parseTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                String content = java.nio.file.Files.readString(file.toPath());
                plantUmlParsingService.parseDomainModel(content);
                return null;
            }
        };

        parseTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_RUNNING, _ -> isParsingDomainModel.set(true));
        parseTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_SUCCEEDED, _ -> {
            isParsingDomainModel.set(false);
            updateSuggestions();
        });
        parseTask.addEventHandler(javafx.concurrent.WorkerStateEvent.WORKER_STATE_FAILED, _ -> {
            isParsingDomainModel.set(false);
            Throwable ex = parseTask.getException();
            LOG.severe("Failed to load domain model: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        Thread.ofVirtual().start(parseTask);
        return parseTask;
    }

    public BooleanProperty isParsingDomainModelProperty() {
        return isParsingDomainModel;
    }


    public void loadTemplateAsync(java.io.File file) {
        if (file == null || !file.exists()) return;

        javafx.concurrent.Task<String> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                return java.nio.file.Files.readString(file.toPath());
            }
        };

        loadTask.setOnSucceeded(_ -> {
            String content = loadTask.getValue();
            referenceTemplate.set(content);
            referenceTemplateName.set("Template: " + file.getName());
        });

        loadTask.setOnFailed(_ -> {
            Throwable ex = loadTask.getException();
            LOG.severe("Failed to read template file: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        Thread.ofVirtual().start(loadTask);
    }
}
