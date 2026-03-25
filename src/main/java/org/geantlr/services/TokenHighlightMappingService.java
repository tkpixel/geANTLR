package org.geantlr.services;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class TokenHighlightMappingService {

    private final Map<String, String> tokenToCssClassMap = new HashMap<>();

    private static final java.util.Set<String> UNIVERSAL_KEYWORDS = java.util.Set.of(
        "if", "else", "for", "while", "return", "function", "func", "class", "struct",
        "import", "package", "public", "private", "protected", "switch", "case", "default",
        "break", "continue", "const", "var", "let", "type", "interface", "enum",
        "wahr", "falsch", "true", "false", "null", "nichts", "funktionsaufruf"
    );

    public TokenHighlightMappingService() {
        // Default mappings
        // Strings
        addMapping("STRING", "string");
        addMapping("STRING_LITERAL", "string");

        // Numbers
        addMapping("INT", "number");
        addMapping("NUMBER", "number");
        addMapping("FLOAT", "number");
        addMapping("DOUBLE", "number");
        addMapping("DECIMAL_LITERAL", "number");
        addMapping("HEX_LITERAL", "number");
        addMapping("OCT_LITERAL", "number");
        addMapping("BINARY_LITERAL", "number");
        addMapping("DIGIT", "number");

        // Comments
        addMapping("COMMENT", "comment");
        addMapping("LINE_COMMENT", "comment");
        addMapping("BLOCK_COMMENT", "comment");

        // Identifiers
        addMapping("IDENTIFIER", "identifier");
        addMapping("ID", "identifier");

        // Keywords / Booleans
        addMapping("KEYWORD", "keyword");
        addMapping("BOOLEAN", "keyword");
        addMapping("TRUE", "keyword");
        addMapping("FALSE", "keyword");
        addMapping("NULL", "keyword");

        // Operators
        addMapping("OPERATOR", "operator");
    }

    public void addMapping(String symbolicName, String cssClass) {
        if (symbolicName != null && cssClass != null) {
            tokenToCssClassMap.put(symbolicName.toUpperCase(), cssClass);
        }
    }

    public String getCssClass(String symbolicName, int tokenType, org.antlr.v4.runtime.Vocabulary vocabulary, String text) {
        if (text != null && text.startsWith("@")) {
            return "annotation";
        }

        // Text-based heuristics
        if (text != null) {
            if (text.startsWith("//") || text.startsWith("/*") || text.startsWith("#")) {
                return "comment";
            }
            if (text.matches("([\"']).*\\1")) {
                return "string";
            }
            if (text.matches("-?\\d+(\\.\\d+)?")) {
                return "number";
            }
            if (UNIVERSAL_KEYWORDS.contains(text.toLowerCase())) {
                return "keyword";
            }
        }

        // Identify keywords dynamically from vocabulary literal names (e.g., 'REGEL', 'WENN')
        // Allows Unicode characters like German umlauts and hyphens.
        if (vocabulary != null) {
            String literalName = vocabulary.getLiteralName(tokenType);
            if (literalName != null) {
                String cleanLiteral = literalName.replaceAll("^'|'$", "");
                if (cleanLiteral.matches("^[A-Za-z_\\u00C0-\\u024F][A-Za-z0-9_\\u00C0-\\u024F-]*$")) {
                    return "keyword";
                }
            }
        }

        if (symbolicName == null) {
            return null;
        }

        // Dynamically identify keywords based on common ANTLR grammar naming conventions
        String upperName = symbolicName.toUpperCase();

        if (upperName.endsWith("_KW") || upperName.endsWith("_KEYWORD")) {
            return "keyword";
        }
        if (upperName.contains("COMMENT")) {
            return "comment";
        }
        if (upperName.contains("STRING") || upperName.contains("LITERAL")) {
            // Check specific types of literals if possible
            if (upperName.contains("NUMBER") || upperName.contains("INT") || upperName.contains("FLOAT") || upperName.contains("DIGIT") || upperName.contains("DEC") || upperName.contains("HEX")) {
                return "number";
            }
            if (text != null && text.matches("-?\\d+(\\.\\d+)?")) {
                return "number"; // Fallback for numeric literals that lack clear symbolic names
            }
            return "string"; // Defaults to string for other literals
        }
        if (upperName.contains("NUMBER") || upperName.contains("INT") || upperName.contains("FLOAT") || upperName.contains("DIGIT")) {
            return "number";
        }
        if (upperName.contains("KEYWORD")) {
            return "keyword";
        }

        String mapped = tokenToCssClassMap.get(upperName);
        if (mapped != null) {
            return mapped;
        }

        return switch (upperName) {
            case "GRAMMAR", "PARSER", "LEXER", "RETURNS", "LOCALS", "IMPORT", "FRAGMENT", "OPTIONS", "MODE", "CATCH", "FINALLY", "THROWS", "CHANNELS" -> "keyword";
            default -> null;
        };
    }
}
