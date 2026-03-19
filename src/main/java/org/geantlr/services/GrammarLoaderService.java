package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.Tool;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of IGrammarLoaderService for finding and loading .g4 files.
 */
@Singleton
public class GrammarLoaderService implements IGrammarLoaderService {

    @Override
    public List<Path> findGrammarFiles(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".g4"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public String loadGrammarContent(File grammarFile) throws IOException {
        if (grammarFile == null || !grammarFile.exists() || !grammarFile.isFile()) {
            throw new IllegalArgumentException("Invalid grammar file provided.");
        }
        return Files.readString(grammarFile.toPath());
    }

    @Override
    public DynamicGrammar loadDynamicGrammar(File grammarFile) throws Exception {
        if (grammarFile == null || !grammarFile.exists() || !grammarFile.isFile()) {
            throw new IllegalArgumentException("Invalid grammar file provided.");
        }

        // Initialize ANTLR Tool
        Tool tool = new Tool();

        // Ensure the directory of the file is in the library path so it can resolve imports if needed
        tool.libDirectory = grammarFile.getParentFile().getAbsolutePath();

        // 2. Instantiate Grammar object.
        // It reads and parses the .g4 file to construct AST and rules.
        Grammar parserGrammar = tool.loadGrammar(grammarFile.getAbsolutePath());

        // We only support valid combined or parser grammars for this primary entry point
        if (parserGrammar == null) {
            throw new IllegalStateException("Failed to load parser grammar from " + grammarFile.getName());
        }

        // 3. Extract the implicit Lexer grammar
        LexerGrammar lexerGrammar = null;
        if (parserGrammar.isCombined()) {
            lexerGrammar = parserGrammar.implicitLexer;
            if (lexerGrammar == null) {
                // Sometime the Tool's loading doesn't expose it directly based on timing, but typically it sets `implicitLexer`
                throw new IllegalStateException("Combined grammar loaded, but implicit lexer is null.");
            }
        } else if (parserGrammar instanceof LexerGrammar) {
            lexerGrammar = (LexerGrammar) parserGrammar;
            parserGrammar = null; // No parser grammar
        }

        // Explicitly extract and store the ATN and Vocabulary
        org.antlr.v4.runtime.atn.ATN atn = null;
        org.antlr.v4.runtime.Vocabulary vocabulary = null;

        if (parserGrammar != null) {
            atn = parserGrammar.getATN();
            vocabulary = parserGrammar.getVocabulary();
        } else if (lexerGrammar != null) {
            atn = lexerGrammar.getATN();
            vocabulary = lexerGrammar.getVocabulary();
        }

        DynamicGrammar dynamicGrammar = new DynamicGrammar(parserGrammar, lexerGrammar);
        dynamicGrammar.setAtn(atn);
        dynamicGrammar.setVocabulary(vocabulary);

        // Programmatically initialize interpreters with empty streams
        if (lexerGrammar != null) {
            org.antlr.v4.runtime.CharStream emptyInput = org.antlr.v4.runtime.CharStreams.fromString("");
            org.antlr.v4.runtime.LexerInterpreter lexerInterpreter = lexerGrammar.createLexerInterpreter(emptyInput);
            dynamicGrammar.setLexerInterpreter(lexerInterpreter);

            if (parserGrammar != null) {
                org.antlr.v4.runtime.CommonTokenStream tokenStream = new org.antlr.v4.runtime.CommonTokenStream(lexerInterpreter);
                org.antlr.v4.runtime.ParserInterpreter parserInterpreter = parserGrammar.createParserInterpreter(tokenStream);
                dynamicGrammar.setParserInterpreter(parserInterpreter);
            }
        }

        return dynamicGrammar;
    }
}
