package org.geantlr.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CodeCompletionServiceTest {

    private PlantUmlParsingService plantUmlParsingService;

    private CodeCompletionService codeCompletionService;

    @BeforeEach
    void setUp() {
        // Use a real or simple stub instance instead of Mockito to avoid ByteBuddy Java 25 issues
        plantUmlParsingService = new PlantUmlParsingService() {
            private Map<String, DomainClass> cache = new HashMap<>();

            @Override
            public Map<String, DomainClass> getDomainModelCache() {
                return cache;
            }

            public void setCache(Map<String, DomainClass> cache) {
                this.cache = cache;
            }
        };
        codeCompletionService = new CodeCompletionService(plantUmlParsingService);
    }

    private void setMockCache(Map<String, DomainClass> cache) {
        try {
            java.lang.reflect.Method method = plantUmlParsingService.getClass().getMethod("setCache", Map.class);
            method.invoke(plantUmlParsingService, cache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnEmptyListWhenTextIsNull() {
        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, null, 0);

        // Then
        assertThat(suggestions).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenCacheIsEmpty() {
        // Given
        setMockCache(Collections.emptyMap());

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, "ctx.", 4);

        // Then
        assertThat(suggestions).isEmpty();
    }

    @Test
    void shouldReturnDomainSuggestionsForDirectClassMatch() {
        // Given
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt", "kmSprung"), Map.of());
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, "DatenpunktKontext.", 18);

        // Then
        assertThat(suggestions).containsExactlyInAnyOrder("datenpunkt", "bezugspunkt", "kmSprung");
    }

    @Test
    void shouldReturnDomainSuggestionsForVariableDeclaration() {
        // Given
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt"), Map.of());
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        String text = "DatenpunktKontext ctx;\nctx.";

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, text, 27);

        // Then
        assertThat(suggestions).containsExactlyInAnyOrder("datenpunkt", "bezugspunkt");
    }

    @Test
    void shouldReturnDomainSuggestionsForChainedAccess() {
        // Given
        DomainClass dpClass = new DomainClass("Datenpunkt", List.of("id"), Map.of());
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt"), Map.of("datenpunkt", "Datenpunkt"));
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("Datenpunkt", dpClass);
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        String text = "DatenpunktKontext ctx;\nctx.datenpunkt.";

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, text, 38);

        // Then
        assertThat(suggestions).containsExactly("id");
    }

    @Test
    void shouldReturnEmptyListWhenChainedAccessTypeIsUnknown() {
        // Given
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt"), Map.of()); // No type info for datenpunkt
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        String text = "DatenpunktKontext ctx;\nctx.datenpunkt.";

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, text, 38);

        // Then
        assertThat(suggestions).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenVariableCannotBeResolved() {
        // Given
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt"), Map.of());
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        String text = "unknownVar.";

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, text, 11);

        // Then
        assertThat(suggestions).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenDotIsMissing() {
        // Given
        DomainClass ctxClass = new DomainClass("DatenpunktKontext", List.of("datenpunkt", "bezugspunkt"), Map.of());
        Map<String, DomainClass> cache = new HashMap<>();
        cache.put("DatenpunktKontext", ctxClass);
        setMockCache(cache);

        String text = "DatenpunktKontext ctx;\nctx";

        // When
        List<String> suggestions = codeCompletionService.getSuggestedTokens(null, text, 26);

        // Then
        assertThat(suggestions).isEmpty();
    }
}
