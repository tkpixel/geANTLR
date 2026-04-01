package org.geantlr.services;

import com.google.adk.tools.Annotations.Schema;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.geantlr.viewmodels.MainViewModel;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class AntlrValidationTool {

    private static final Logger LOG = LoggerFactory.getLogger(AntlrValidationTool.class);

    private final MainViewModel mainViewModel;

    @Inject
    public AntlrValidationTool(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Schema(name = "validateCode", description = "Validates the generated DSL code using the ANTLR grammar")
    public java.util.Map<String, Object> validateCode(
            @Schema(name = "dslCode", description = "The DSL code to validate") String dslCode) {

        DynamicGrammar grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            return java.util.Map.of("result", "SUCCESS"); // Nothing to validate against
        }

        LlmFeedbackErrorListener errorListener = new LlmFeedbackErrorListener();
        CharStream input = CharStreams.fromString(dslCode);

        LexerInterpreter lexerInterpreter = grammar.createLexerInterpreter(input);
        lexerInterpreter.removeErrorListeners();
        // Fallback standard error listener for lexer if needed, but we attach ours just in case
        lexerInterpreter.addErrorListener(errorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexerInterpreter);

        if (grammar.getParserGrammar() != null) {
            ParserInterpreter parserInterpreter = grammar.createParserInterpreter(tokenStream);
            parserInterpreter.removeErrorListeners();
            parserInterpreter.addErrorListener(errorListener);

            org.antlr.v4.tool.Rule startRule = grammar.getParserGrammar().rules.values().iterator().next();

            try {
                parserInterpreter.parse(startRule.index);
            } catch (Exception e) {
                // Ignore parsing exception, errorListener will catch syntax errors
                LOG.trace("Validation parsing exception ignored, relying on error listener", e);
            }
        } else {
            tokenStream.fill();
        }

        if (errorListener.getErrors().isEmpty()) {
            return java.util.Map.of("result", "SUCCESS");
        }

        StringBuilder errorMessage = new StringBuilder();
        for (String error : errorListener.getErrors()) {
            errorMessage.append(error).append("\n");
        }

        return java.util.Map.of("result", errorMessage.toString().trim());
    }

    private static class LlmFeedbackErrorListener extends BaseErrorListener {
        private final List<String> errors = new ArrayList<>();

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            String offendingText = "unknown";
            if (offendingSymbol instanceof Token) {
                Token token = (Token) offendingSymbol;
                offendingText = token.getText();
            }

            String expectedTokensList = "";
            if (e != null && recognizer != null) {
                Vocabulary vocabulary = recognizer.getVocabulary();
                IntervalSet expectedTokens = e.getExpectedTokens();
                if (expectedTokens != null && vocabulary != null) {
                    List<String> tokenNames = new ArrayList<>();
                    for (int id : expectedTokens.toList()) {
                        String name = vocabulary.getDisplayName(id);
                        if (name == null || name.isEmpty() || name.equals("<INVALID>")) {
                            name = vocabulary.getSymbolicName(id);
                        }
                        if (name != null && !name.isEmpty() && !name.equals("<EOF>")) {
                            tokenNames.add(name);
                        }
                    }
                    expectedTokensList = String.join(", ", tokenNames);
                }
            }

            // Fallback message if we can't extract expected tokens (e.g. Lexer errors)
            if (expectedTokensList.isEmpty()) {
                expectedTokensList = "a valid token for this context";
            }

            String llmPrompt = String.format("Syntax Error in line %d at character %d. You wrote '%s'. This is invalid according to the grammar. You MUST use one of the following valid elements here: [%s]. Please analyze this feedback, correct only this specific part, and output the full corrected code again.",
                    line, charPositionInLine, offendingText, expectedTokensList);

            errors.add(llmPrompt);
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
