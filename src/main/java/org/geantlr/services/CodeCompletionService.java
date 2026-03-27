package org.geantlr.services;

import com.vmware.antlr4c3.CodeCompletionCore;
import jakarta.inject.Singleton;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.CommonTokenStream;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class CodeCompletionService {

    private final PlantUmlParsingService plantUmlParsingService;

    // Pattern to find variable declarations like "ClassName varName" or "ClassName varName,"
    private static final Pattern VAR_DECL_PATTERN = Pattern.compile(
        "([A-Z][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)(?:\\s*[,;=)\\n\\r]|$)"
    );

    @Inject
    public CodeCompletionService(PlantUmlParsingService plantUmlParsingService) {
        this.plantUmlParsingService = plantUmlParsingService;
    }

    public List<String> getSuggestedTokens(DynamicGrammar grammar, String text, int caretPosition) {
        if (text == null) return Collections.emptyList();

        List<String> suggestedTokens = new ArrayList<>();

        // --- Domain model dot-accessor suggestions (text-based, grammar-independent) ---
        List<String> domainSuggestions = getDomainMemberSuggestions(text, caretPosition);
        if (!domainSuggestions.isEmpty()) {
            // When we are after a dot, only show domain-model members (no grammar tokens)
            return domainSuggestions;
        }

        // --- Grammar-based token suggestions ---
        if (grammar == null || grammar.getParserGrammar() == null) {
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

        return suggestedTokens;
    }

    /**
     * Determines if the cursor is positioned after a dot accessor and returns the
     * members of the resolved type. Supports chained access like ctx.datenpunkt.
     *
     * Examples:
     *   "ctx."            → resolves ctx → DatenpunktKontext → fields of DatenpunktKontext
     *   "ctx.datenpunkt." → resolves ctx → DatenpunktKontext, then datenpunkt → Datenpunkt → fields of Datenpunkt
     *   "ctx.datenpunkt.bal" → same as above (partial match after last dot is ignored for type resolution)
     */
    private List<String> getDomainMemberSuggestions(String text, int caretPosition) {
        Map<String, DomainClass> cache = plantUmlParsingService.getDomainModelCache();
        if (cache == null || cache.isEmpty()) {
            return Collections.emptyList();
        }

        int safeEnd = Math.min(caretPosition, text.length());
        String textBeforeCaret = text.substring(0, safeEnd);

        // Extract the dot-chain immediately before the caret.
        // We walk backwards from the caret, collecting identifier.identifier. segments.
        // Stop at any character that cannot be part of an identifier or dot (whitespace, operators, etc.)
        int end = textBeforeCaret.length() - 1;
        // Skip trailing partial identifier after last dot (e.g. "ctx.datenpunkt.bal" → ignore "bal")
        while (end >= 0 && (Character.isLetterOrDigit(textBeforeCaret.charAt(end)) || textBeforeCaret.charAt(end) == '_')) {
            end--;
        }
        // There must be a dot right here
        if (end < 0 || textBeforeCaret.charAt(end) != '.') {
            return Collections.emptyList();
        }
        // Now collect the full chain before the dot
        int chainEnd = end; // exclusive – points at the dot
        int chainStart = chainEnd - 1;
        while (chainStart >= 0) {
            char c = textBeforeCaret.charAt(chainStart);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '.') {
                chainStart--;
            } else {
                break;
            }
        }
        chainStart++; // first char of the chain

        if (chainStart >= chainEnd) {
            return Collections.emptyList();
        }

        String chain = textBeforeCaret.substring(chainStart, chainEnd); // e.g. "ctx" or "ctx.datenpunkt"
        String[] segments = chain.split("\\.");

        // Resolve first segment: it may be a variable name or a class name directly
        String currentType = resolveVariableType(segments[0], text, cache);
        if (currentType == null) {
            return Collections.emptyList();
        }

        // Walk remaining segments using fieldTypes
        for (int i = 1; i < segments.length; i++) {
            DomainClass currentClass = cache.get(currentType);
            if (currentClass == null) return Collections.emptyList();

            String fieldName = segments[i];
            String nextType = currentClass.fieldTypes().get(fieldName);
            if (nextType == null) {
                // Field exists but type unknown – cannot go deeper
                return Collections.emptyList();
            }
            currentType = nextType;
        }

        DomainClass target = cache.get(currentType);
        if (target == null || target.fields() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(target.fields());
    }

    /**
     * Resolves a variable/identifier name to a domain class name.
     * Checks (in order):
     *  1. Is the name itself a known class? → return it
     *  2. Scan text for "ClassName varName" declarations
     */
    private String resolveVariableType(String variableName, String fullText, Map<String, DomainClass> cache) {
        if (cache.containsKey(variableName)) {
            return variableName;
        }
        Matcher varDecl = VAR_DECL_PATTERN.matcher(fullText);
        while (varDecl.find()) {
            String typeName = varDecl.group(1);
            String varName  = varDecl.group(2);
            if (varName.equals(variableName) && cache.containsKey(typeName)) {
                return typeName;
            }
        }
        return null;
    }
}
