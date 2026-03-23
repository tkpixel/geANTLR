package org.geantlr.services;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.LexerInterpreter;
import org.antlr.v4.runtime.ParserInterpreter;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;

/**
 * Encapsulates the runtime interpreter models of an ANTLR 4 grammar loaded dynamically.
 */
public class DynamicGrammar {

    private final Grammar parserGrammar;
    private final LexerGrammar lexerGrammar;

    private LexerInterpreter lexerInterpreter;
    private ParserInterpreter parserInterpreter;

    private org.antlr.v4.runtime.atn.ATN atn;
    private org.antlr.v4.runtime.Vocabulary vocabulary;
    private String rawGrammarText;

    public DynamicGrammar(Grammar parserGrammar, LexerGrammar lexerGrammar) {
        this.parserGrammar = parserGrammar;
        this.lexerGrammar = lexerGrammar;
    }

    public void setRawGrammarText(String rawGrammarText) {
        this.rawGrammarText = rawGrammarText;
    }

    public String getRawGrammarText() {
        return rawGrammarText;
    }

    /**
     * Creates a new instance of a LexerInterpreter based on the implicitly generated Lexer grammar.
     *
     * @param input The character stream to tokenize.
     * @return A LexerInterpreter configured for this grammar.
     */
    public LexerInterpreter createLexerInterpreter(CharStream input) {
        return lexerGrammar.createLexerInterpreter(input);
    }

    /**
     * Creates a new instance of a ParserInterpreter based on the loaded parser grammar.
     *
     * @param input The token stream to parse.
     * @return A ParserInterpreter configured for this grammar.
     */
    public ParserInterpreter createParserInterpreter(TokenStream input) {
        return parserGrammar.createParserInterpreter(input);
    }

    public Grammar getParserGrammar() {
        return parserGrammar;
    }

    public LexerGrammar getLexerGrammar() {
        return lexerGrammar;
    }

    public LexerInterpreter getLexerInterpreter() {
        return lexerInterpreter;
    }

    public void setLexerInterpreter(LexerInterpreter lexerInterpreter) {
        this.lexerInterpreter = lexerInterpreter;
    }

    public ParserInterpreter getParserInterpreter() {
        return parserInterpreter;
    }

    public void setParserInterpreter(ParserInterpreter parserInterpreter) {
        this.parserInterpreter = parserInterpreter;
    }

    public org.antlr.v4.runtime.atn.ATN getAtn() {
        return atn;
    }

    public void setAtn(org.antlr.v4.runtime.atn.ATN atn) {
        this.atn = atn;
    }

    public org.antlr.v4.runtime.Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(org.antlr.v4.runtime.Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }
}
