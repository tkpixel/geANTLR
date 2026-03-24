package org.geantlr.services;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class TokenHighlightMappingService {

    private final Map<String, String> tokenToCssClassMap = new HashMap<>();

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
        }

        // Identify keywords dynamically from vocabulary literal names (e.g., 'REGEL', 'WENN')
        // Allows Unicode characters like German umlauts and hyphens.
        if (vocabulary != null) {
            String literalName = vocabulary.getLiteralName(tokenType);
            if (literalName != null && literalName.matches("'[A-Za-z_\\u00C0-\\u024F][A-Za-z0-9_\\u00C0-\\u024F-]*'")) {
                return "keyword";
            }
        }

        if (symbolicName == null) {
            return null;
        }

        // Dynamically identify keywords based on common ANTLR grammar naming conventions
        String upperName = symbolicName.toUpperCase();

        if (upperName.contains("COMMENT")) {
            return "comment";
        }
        if (upperName.contains("STRING")) {
            return "string";
        }
        if (upperName.contains("NUMBER") || upperName.contains("INT") || upperName.contains("FLOAT") || upperName.contains("LITERAL") && (upperName.contains("NUM") || upperName.contains("DEC") || upperName.contains("HEX"))) {
            return "number";
        }
        if (upperName.endsWith("_KW") || upperName.contains("KEYWORD")) {
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
