package org.geantlr.services;

import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHighlightMappingServiceTest {

    private TokenHighlightMappingService service;

    @BeforeEach
    void setUp() {
        service = new TokenHighlightMappingService();
    }

    @ParameterizedTest
    @ValueSource(strings = {"'if'", "'while'", "'class'", "'return'", "'try'"})
    @DisplayName("Should assign 'keyword' class to literal names matching word patterns")
    void shouldAssignKeywordClassToLiteralWords(String literalName) {
        // Given
        String[] literalNames = new String[]{null, literalName};
        String[] symbolicNames = new String[]{null, null};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("keyword");
    }

    @ParameterizedTest
    @ValueSource(strings = {"'+'", "'-'", "'*'", "'/'", "'=='", "'<='", "'=>'", "'&&'"})
    @DisplayName("Should assign 'operator' class to literal names matching symbol patterns")
    void shouldAssignOperatorClassToLiteralSymbols(String literalName) {
        // Given
        String[] literalNames = new String[]{null, literalName};
        String[] symbolicNames = new String[]{null, null};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("operator");
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOME_KW", "ANOTHER_KEYWORD"})
    @DisplayName("Should assign 'keyword' class to symbolic names ending with _KW or _KEYWORD")
    void shouldAssignKeywordClassToSymbolicKeywords(String symbolicName) {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, symbolicName};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("keyword");
    }

    @Test
    @DisplayName("Should return null for symbolic name without keyword mapping")
    void shouldReturnNullForSymbolicWord() {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, "JUST_A_WORD"};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"LINE_COMMENT", "BLOCK_COMMENT", "COMMENT_TOKEN"})
    @DisplayName("Should assign 'comment' class to symbolic names containing COMMENT")
    void shouldAssignCommentClassToSymbolicComments(String symbolicName) {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, symbolicName};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("comment");
    }

    @ParameterizedTest
    @ValueSource(strings = {"STRING_LITERAL", "SOME_STRING"})
    @DisplayName("Should assign 'string' class to symbolic names containing STRING or LITERAL")
    void shouldAssignStringClassToSymbolicStrings(String symbolicName) {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, symbolicName};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("string");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INT_LITERAL", "FLOAT_VAL", "NUMERIC", "DIGIT_TOKEN"})
    @DisplayName("Should assign 'number' class to symbolic names containing INT, FLOAT, NUM, or DIGIT")
    void shouldAssignNumberClassToSymbolicNumbers(String symbolicName) {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, symbolicName};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("number");
    }

    @ParameterizedTest
    @ValueSource(strings = {"INT_LITERAL", "NUMERIC_STRING"})
    @DisplayName("Should prioritize 'number' over 'string' if both match")
    void shouldPrioritizeNumberOverString(String symbolicName) {
        // Given
        String[] literalNames = new String[]{null, null};
        String[] symbolicNames = new String[]{null, symbolicName};
        Vocabulary vocabulary = new VocabularyImpl(literalNames, symbolicNames);

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isEqualTo("number");
    }

    @Test
    @DisplayName("Should return null for unknown token types")
    void shouldReturnNullForUnknownTokenTypes() {
        // Given
        Vocabulary vocabulary = VocabularyImpl.EMPTY_VOCABULARY;

        // When
        service.buildVocabularyMapping(vocabulary);

        // Then
        assertThat(service.getCssClass(1)).isNull();
    }

    @Test
    @DisplayName("Should handle null vocabulary gracefully")
    void shouldHandleNullVocabularyGracefully() {
        // When
        service.buildVocabularyMapping(null);

        // Then
        assertThat(service.getCssClass(1)).isNull();
    }
}
