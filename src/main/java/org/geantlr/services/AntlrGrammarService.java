package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;

import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@Singleton
public class AntlrGrammarService {

    private static final Logger LOG = LoggerFactory.getLogger(AntlrGrammarService.class);

    public ParseResult parseText(DynamicGrammar dynamicGrammar, String text) {
        if (dynamicGrammar == null || text == null || text.isEmpty()) {
            return new ParseResult(Collections.emptyList(), Collections.emptyList());
        }

        CustomErrorListener errorListener = new CustomErrorListener();

        CharStream input = CharStreams.fromString(text);

        LexerInterpreter lexerInterpreter = dynamicGrammar.createLexerInterpreter(input);
        lexerInterpreter.removeErrorListeners();
        lexerInterpreter.addErrorListener(errorListener);

        CommonTokenStream tokenStream = new CommonTokenStream(lexerInterpreter);

        // If a parser grammar exists, parse it
        if (dynamicGrammar.getParserGrammar() != null) {
            ParserInterpreter parserInterpreter = dynamicGrammar.createParserInterpreter(tokenStream);
            parserInterpreter.removeErrorListeners();
            parserInterpreter.addErrorListener(errorListener);

            // Need to get the start rule. Assuming the first rule defined in the grammar is the start rule.
            org.antlr.v4.tool.Rule startRule = dynamicGrammar.getParserGrammar().rules.values().iterator().next();

            try {
                parserInterpreter.parse(startRule.index);
            } catch (Exception e) {
                // Ignore general exceptions during compilation/parsing as errors are handled by listener
                LOG.trace("Parsing exception ignored as it is handled by error listener", e);
            }
        } else {
            // If it's a lexer grammar only, consume all tokens to trigger lexer errors
            tokenStream.fill();
        }

        tokenStream.fill(); // Ensure we have all tokens
        List<Token> tokens = tokenStream.getTokens();

        return new ParseResult(tokens, errorListener.getErrors());
    }
}
