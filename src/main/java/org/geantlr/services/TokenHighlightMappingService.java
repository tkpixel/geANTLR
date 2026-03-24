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

        // Custom German Keywords
        addMapping("REGEL_KW", "keyword");
        addMapping("WENN_KW", "keyword");
        addMapping("REGEL", "keyword");
        addMapping("WENN", "keyword");
        addMapping("SONST", "keyword");
        addMapping("PRUEFUNG", "keyword");
        addMapping("ERGEBNIS", "keyword");
        addMapping("ERFOLG", "keyword");
        addMapping("FEHLER", "keyword");
        addMapping("WAHR", "keyword");
        addMapping("FALSCH", "keyword");

        // Operators
        addMapping("OPERATOR", "operator");
    }

    public void addMapping(String symbolicName, String cssClass) {
        if (symbolicName != null && cssClass != null) {
            tokenToCssClassMap.put(symbolicName.toUpperCase(), cssClass);
        }
    }

    public String getCssClass(String symbolicName) {
        if (symbolicName == null) {
            return null;
        }
        if (symbolicName.startsWith("@")) {
            return "annotation";
        }
        return switch (symbolicName.toUpperCase()) {
            case "GRAMMAR", "PARSER", "LEXER", "RETURNS", "LOCALS", "IMPORT", "FRAGMENT", "OPTIONS", "MODE", "CATCH", "FINALLY", "THROWS", "CHANNELS" -> "keyword";
            default -> tokenToCssClassMap.get(symbolicName.toUpperCase());
        };
    }
}
