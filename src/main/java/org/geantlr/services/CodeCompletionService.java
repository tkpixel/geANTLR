package org.geantlr.services;

import com.vmware.antlr4c3.CodeCompletionCore;
import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.CommonTokenStream;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Singleton
public class CodeCompletionService {

    private final PlantUmlParsingService plantUmlParsingService;

    @Inject
    public CodeCompletionService(PlantUmlParsingService plantUmlParsingService) {
        this.plantUmlParsingService = plantUmlParsingService;
    }

    public List<String> getSuggestedTokens(DynamicGrammar grammar, String text, int caretPosition) {
        if (grammar == null || text == null || grammar.getParserGrammar() == null) {
            return Collections.emptyList();
        }

        // Lex the text
        CharStream input = CharStreams.fromString(text);
        LexerInterpreter lexerInterpreter = grammar.createLexerInterpreter(input);
        lexerInterpreter.removeErrorListeners();

        CommonTokenStream tokenStream = new CommonTokenStream(lexerInterpreter);
        tokenStream.fill();

        List<Token> tokens = tokenStream.getTokens();
        int tokenIndex = 0;

        if (tokens != null) {
            for (int i = 0; i < tokens.size(); i++) {
                Token t = tokens.get(i);
                if (t.getStartIndex() <= caretPosition && t.getStopIndex() >= caretPosition) {
                    tokenIndex = i;
                    break;
                } else if (t.getStartIndex() > caretPosition) {
                    tokenIndex = i;
                    break;
                }
            }
            if (tokenIndex == 0 && !tokens.isEmpty() && tokens.get(tokens.size() - 1).getStopIndex() < caretPosition) {
                tokenIndex = tokens.size();
            }
        }

        CodeCompletionCore core = new CodeCompletionCore(grammar.getParserGrammar().createParserInterpreter(tokenStream), null, null);
        CodeCompletionCore.CandidatesCollection candidates = core.collectCandidates(tokenIndex, null);

        List<String> suggestedTokens = new ArrayList<>();
        Vocabulary vocabulary = grammar.getVocabulary();

        if (vocabulary == null && grammar.getParserGrammar() != null) {
            vocabulary = grammar.getParserGrammar().getVocabulary();
        }

        if (vocabulary != null) {
            for (Integer id : candidates.tokens.keySet()) {
                String displayName = vocabulary.getDisplayName(id);
                if (displayName != null) {
                    suggestedTokens.add(displayName);
                }
            }
        }

        // Check context for dot accessor
        if (tokenIndex > 1 && tokens.size() >= tokenIndex) {
            Token previousToken = tokens.get(tokenIndex - 1);
            if (".".equals(previousToken.getText())) {
                Token objectToken = tokens.get(tokenIndex - 2);
                String objectName = objectToken.getText();
                Map<String, DomainClass> domainModelCache = plantUmlParsingService.getDomainModelCache();
                if (domainModelCache.containsKey(objectName)) {
                    DomainClass domainClass = domainModelCache.get(objectName);
                    if (domainClass != null && domainClass.fields() != null) {
                        for (String field : domainClass.fields()) {
                            if (!suggestedTokens.contains(field)) {
                                suggestedTokens.add(field);
                            }
                        }
                    }
                }
            }
        }

        return suggestedTokens;
    }
}
