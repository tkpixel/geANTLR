package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.Tool;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of IGrammarLoaderService for finding and loading .g4 files.
 */
@Singleton
public class GrammarLoaderService implements IGrammarLoaderService {

    private final List<File> importDirectories = new ArrayList<>();

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

        // Initialize ANTLR Tool with a custom import resolution strategy
        Tool tool = new Tool() {
            @Override
            public File getImportedGrammarFile(Grammar g, String fileName) {
                // Try explicitly added import directories first
                for (File dir : importDirectories) {
                    File candidate = new File(dir, fileName);
                    if (candidate.exists() && candidate.isFile()) {
                        return candidate;
                    }
                }
                // Fallback to the original logic (e.g. checking libDirectory)
                return super.getImportedGrammarFile(g, fileName);
            }
        };

        // Ensure the directory of the file is in the library path so it can resolve imports if needed locally
        tool.libDirectory = grammarFile.getParentFile().getAbsolutePath();
        tool.outputDirectory = grammarFile.getParentFile().getAbsolutePath();

        LexerGrammar lexerGrammar = null;

        // Check if this grammar explicitly depends on an external tokenVocab (i.e. separate lexer and parser)
        String content = loadGrammarContent(grammarFile);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("tokenVocab\\s*=\\s*([a-zA-Z0-9_]+)").matcher(content);
        if (m.find()) {
            String lexerName = m.group(1);
            File lexerFile = new File(grammarFile.getParentFile(), lexerName + ".g4");

            // Search in import directories if not in the same folder
            if (!lexerFile.exists()) {
                for (File dir : importDirectories) {
                    File candidate = new File(dir, lexerName + ".g4");
                    if (candidate.exists()) {
                        lexerFile = candidate;
                        break;
                    }
                }
            }

            if (lexerFile.exists()) {
                Tool lexerTool = new Tool();
                lexerTool.libDirectory = lexerFile.getParentFile().getAbsolutePath();
                lexerTool.outputDirectory = lexerFile.getParentFile().getAbsolutePath();
                Grammar lexerG = lexerTool.loadGrammar(lexerFile.getAbsolutePath());
                if (lexerG instanceof LexerGrammar) {
                    lexerTool.process(lexerG, false); // Generates the .tokens file
                    lexerGrammar = (LexerGrammar) lexerG;
                }
            }
        }

        // 2. Instantiate Grammar object.
        // It reads and parses the .g4 file to construct AST and rules.
        Grammar parserGrammar = tool.loadGrammar(grammarFile.getAbsolutePath());

        // We only support valid combined or parser grammars for this primary entry point
        if (parserGrammar == null) {
            throw new IllegalStateException("Failed to load parser grammar from " + grammarFile.getName());
        }

        // 3. Extract the implicit Lexer grammar if combined
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
        dynamicGrammar.setRawGrammarText(loadGrammarContent(grammarFile));

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

    @Override
    public void addImportDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            if (!importDirectories.contains(directory)) {
                importDirectories.add(directory);
            }
        } else {
            throw new IllegalArgumentException("Invalid directory provided for grammar imports.");
        }
    }

    @Override
    public List<File> getImportDirectories() {
        return Collections.unmodifiableList(importDirectories);
    }

    @Override
    public void clearImportDirectories() {
        importDirectories.clear();
    }
}
