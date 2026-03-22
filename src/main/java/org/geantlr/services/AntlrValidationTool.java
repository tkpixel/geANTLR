package org.geantlr.services;

import com.google.adk.tools.Annotations.Schema;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.geantlr.viewmodels.MainViewModel;

@Singleton
public class AntlrValidationTool {

    private final AntlrGrammarService antlrGrammarService;
    private final MainViewModel mainViewModel;

    @Inject
    public AntlrValidationTool(AntlrGrammarService antlrGrammarService, MainViewModel mainViewModel) {
        this.antlrGrammarService = antlrGrammarService;
        this.mainViewModel = mainViewModel;
    }

    public String validateCode(String dslCode) {
        DynamicGrammar grammar = mainViewModel.getDynamicGrammar();
        if (grammar == null) {
            return "SUCCESS"; // Nothing to validate against
        }

        ParseResult result = antlrGrammarService.parseText(grammar, dslCode);

        if (result.errors() == null || result.errors().isEmpty()) {
            return "SUCCESS";
        }

        StringBuilder errorMessage = new StringBuilder("Syntax Errors found:\n");
        for (SyntaxError error : result.errors()) {
            errorMessage.append(String.format("- Line %d, Position %d: %s\n",
                    error.line(), error.charPositionInLine(), error.message()));
        }

        return errorMessage.toString();
    }
}
