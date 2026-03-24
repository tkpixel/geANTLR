package org.geantlr.services;

import com.vmware.antlr4c3.CodeCompletionCore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.CommonTokenStream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class CodeCompletionService {

    private final SymbolTableService symbolTableService;

    private static Field ignoredTokensField;
    private static Field preferredRulesField;

    static {
        try {
            ignoredTokensField = CodeCompletionCore.class.getDeclaredField("ignoredTokens");
            ignoredTokensField.setAccessible(true);
            preferredRulesField = CodeCompletionCore.class.getDeclaredField("preferredRules");
            preferredRulesField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            System.err.println("Could not initialize CodeCompletionCore reflection fields: " + e.getMessage());
        }
    }

    @Inject
    public CodeCompletionService(SymbolTableService symbolTableService) {
        this.symbolTableService = symbolTableService;
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

        // Task 2: Fine-tune the antlr4-c3 Engine
        Vocabulary vocabulary = grammar.getVocabulary();
        if (vocabulary == null && grammar.getParserGrammar() != null) {
            vocabulary = grammar.getParserGrammar().getVocabulary();
        }

        Set<Integer> ignoredTokens = new HashSet<>();
        if (vocabulary != null) {
            for (int i = 0; i <= vocabulary.getMaxTokenType(); i++) {
                String name = vocabulary.getSymbolicName(i);
                if (name != null && (name.equals("ID") || name.equals("IDENTIFIER") || name.equals("WS") || name.equals("EOF"))) {
                    ignoredTokens.add(i);
                }
                String literalName = vocabulary.getLiteralName(i);
                if (literalName != null && (literalName.equals("'+'") || literalName.equals("'-'") || literalName.equals("';'"))) {
                    ignoredTokens.add(i);
                }
            }
            ignoredTokens.add(Token.EOF);
        }

        Set<Integer> preferredRules = new HashSet<>();
        if (grammar.getParserGrammar() != null) {
            for (org.antlr.v4.tool.Rule rule : grammar.getParserGrammar().rules.values()) {
                 if (rule.name.toLowerCase().contains("variableref") || rule.name.toLowerCase().contains("functionref") || rule.name.toLowerCase().contains("expr")) {
                     preferredRules.add(rule.index);
                 }
            }
        }

        try {
            if (ignoredTokensField != null) {
                ignoredTokensField.set(core, ignoredTokens);
            }
            if (preferredRulesField != null) {
                preferredRulesField.set(core, preferredRules);
            }
        } catch (IllegalAccessException e) {
            System.err.println("Could not access fields: " + e.getMessage());
        }

        CodeCompletionCore.CandidatesCollection candidates = core.collectCandidates(tokenIndex, null);

        List<String> suggestedTokens = new ArrayList<>();

        if (vocabulary != null) {
            for (Integer id : candidates.tokens.keySet()) {
                String displayName = vocabulary.getDisplayName(id);
                if (displayName != null) {
                    suggestedTokens.add(displayName);
                }
            }
        }

        // Task 3: Semantic Mapping of Candidates
        if (!candidates.rules.isEmpty()) {
            boolean hasPreferredRuleMatch = false;
            for (Map.Entry<Integer, List<Integer>> entry : candidates.rules.entrySet()) {
                 if (preferredRules.contains(entry.getKey())) {
                     hasPreferredRuleMatch = true;
                     break;
                 }
            }
            if (hasPreferredRuleMatch) {
                List<String> variables = symbolTableService.getVariables();
                suggestedTokens.addAll(variables);
            }
        }

        return suggestedTokens;
    }
}
