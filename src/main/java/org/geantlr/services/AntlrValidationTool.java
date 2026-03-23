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

@Singleton
public class AntlrValidationTool {

    private final MainViewModel mainViewModel;

    @Inject
    public AntlrValidationTool(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    @Schema(name = "validateCode", description = "Validates the generated DSL code using the ANTLR grammar")
    public String validateCode(
            @Schema(name = "dslCode", description = "The DSL code to validate") String dslCode) {

        DynamicGrammar grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            return "SUCCESS"; // Nothing to validate against
        }

        CustomErrorListener errorListener = new CustomErrorListener();
        CharStream input = CharStreams.fromString(dslCode);

        LexerInterpreter lexerInterpreter = grammar.createLexerInterpreter(input);
        lexerInterpreter.removeErrorListeners();
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
            }
        } else {
            tokenStream.fill();
        }

        if (errorListener.getErrors().isEmpty()) {
            return "SUCCESS";
        }

        StringBuilder errorMessage = new StringBuilder("Syntax Errors found:\n");
        for (SyntaxError error : errorListener.getErrors()) {
            errorMessage.append(String.format("- Line %d, Position %d (Expected/Offending length %d): %s\n",
                    error.line(), error.charPositionInLine(), error.length(), error.message()));
        }

        return errorMessage.toString();
    }
}
