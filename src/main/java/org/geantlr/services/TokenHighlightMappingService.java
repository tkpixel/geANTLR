package org.geantlr.services;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class TokenHighlightMappingService {

    private final Map<String, String> tokenToCssClassMap = new HashMap<>();

    private final Map<Integer, String> cachedTokenClassMapping = new HashMap<>();
    private org.antlr.v4.runtime.Vocabulary activeVocabulary;

    public TokenHighlightMappingService() {
    }

    public void buildVocabularyMapping(org.antlr.v4.runtime.Vocabulary vocabulary) {
        this.activeVocabulary = vocabulary;
        cachedTokenClassMapping.clear();

        if (vocabulary == null) {
            return;
        }

        int maxTokenType = vocabulary.getMaxTokenType();
        for (int id = 1; id <= maxTokenType; id++) {
            String cssClass = null;

            // Schritt A: Keyword & Operator Erkennung (Literal Name)
            String literalName = vocabulary.getLiteralName(id);
            if (literalName != null) {
                // Bereinigungslogik
                String cleanLiteral = literalName.replaceAll("^'|'$", "");

                // Regex für Keywords: Nur Buchstaben, Ziffern, Unterstriche, beginnt mit Buchstabe
                if (cleanLiteral.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                    cssClass = "keyword";
                }
                // Wenn nicht keyword, aber Symbole vorhanden sind (Operator)
                else if (cleanLiteral.matches("^[^a-zA-Z0-9_\\s]+$")) {
                    cssClass = "operator";
                }
            }

            // Schritt B: Erkennung komplexer Tokens (Symbolic Name)
            if (cssClass == null) {
                String symbolicName = vocabulary.getSymbolicName(id);
                if (symbolicName != null) {
                    String name = symbolicName.toUpperCase();
                    if (name.endsWith("_KW") || name.endsWith("_KEYWORD")) {
                        cssClass = "keyword";
                    } else if (name.contains("COMMENT")) {
                        cssClass = "comment";
                    } else if (name.contains("STRING") || name.contains("LITERAL")) {
                        // Da String/Literal oft auch für Numbers missbraucht wird, erst Number checken
                        if (name.contains("INT") || name.contains("FLOAT") || name.contains("NUM") || name.contains("DIGIT")) {
                            cssClass = "number";
                        } else {
                            cssClass = "string";
                        }
                    } else if (name.contains("INT") || name.contains("FLOAT") || name.contains("NUM") || name.contains("DIGIT")) {
                        cssClass = "number";
                    }
                }
            }

            // Schritt C: Fallback null in the Map
            cachedTokenClassMapping.put(id, cssClass);
        }
    }

    public String getCssClass(int tokenType) {
        if (cachedTokenClassMapping.containsKey(tokenType)) {
            return cachedTokenClassMapping.get(tokenType);
        }
        return null; // Standard fallback (no style assigned)
    }
}
