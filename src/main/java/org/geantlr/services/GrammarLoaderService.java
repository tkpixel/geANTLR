package org.geantlr.services;

import jakarta.inject.Singleton;
import org.antlr.v4.Tool;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.LexerGrammar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(GrammarLoaderService.class);

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
            LOG.error("Failed to read directory while finding grammar files: {}", directory, e);
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
        return loadDynamicGrammar(grammarFile.getParentFile(), grammarFile);
    }

    @Override
    public DynamicGrammar loadDynamicGrammar(File importDir, File parserFile) throws Exception {
        if (parserFile == null || !parserFile.exists() || !parserFile.isFile()) {
            throw new IllegalArgumentException("Invalid parser grammar file provided.");
        }

        // Custom tool to support multiple import directories for dependencies
        Tool tool = new Tool() {
            @Override
            public File getImportedGrammarFile(Grammar g, String fileName) {
                for (File dir : importDirectories) {
                    File candidate = new File(dir, fileName);
                    if (candidate.exists() && candidate.isFile()) {
                        return candidate;
                    }
                }
                return super.getImportedGrammarFile(g, fileName);
            }
        };

        if (importDir != null && importDir.exists()) {
            tool.libDirectory = importDir.getAbsolutePath();
            tool.outputDirectory = importDir.getAbsolutePath();
            addImportDirectory(importDir);
        } else {
            tool.libDirectory = parserFile.getParentFile().getAbsolutePath();
            tool.outputDirectory = parserFile.getParentFile().getAbsolutePath();
        }

        LexerGrammar lexerGrammar = null;
        Grammar parserGrammar = null;
        String rawGrammarText = loadGrammarContent(parserFile);

        // NATIVE 2-STEP LOAD PROCESS
        // Implicit resolution logic for when only parser is provided
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("tokenVocab\\s*=\\s*([a-zA-Z0-9_]+)").matcher(rawGrammarText);
        if (m.find()) {
                String lexerName = m.group(1);
                File inferredLexer = new File(parserFile.getParentFile(), lexerName + ".g4");

                if (!inferredLexer.exists() && importDir != null) {
                    inferredLexer = new File(importDir, lexerName + ".g4");
                }

                if (!inferredLexer.exists()) {
                    for (File dir : importDirectories) {
                        File candidate = new File(dir, lexerName + ".g4");
                        if (candidate.exists()) {
                            inferredLexer = candidate;
                            break;
                        }
                    }
                }

                if (inferredLexer.exists()) {
                     // STEP 1: Process inferred Lexer
                    Tool lexerTool = new Tool() {
                        @Override
                        public File getImportedGrammarFile(Grammar g, String fileName) {
                            return tool.getImportedGrammarFile(g, fileName);
                        }
                    };
                    lexerTool.libDirectory = tool.libDirectory;
                    lexerTool.outputDirectory = tool.outputDirectory;

                    Grammar rootLexer = lexerTool.loadGrammar(inferredLexer.getAbsolutePath());
                    if (rootLexer instanceof LexerGrammar) {
                        lexerGrammar = (LexerGrammar) rootLexer;
                        lexerTool.process(lexerGrammar, true);
                    }

                    // STEP 2: Load Parser
                    parserGrammar = tool.loadGrammar(parserFile.getAbsolutePath());
                } else {
                    // Fallback to strict load if lexer not found
                    parserGrammar = tool.loadGrammar(parserFile.getAbsolutePath());
                    if (parserGrammar != null && parserGrammar.isCombined()) {
                        lexerGrammar = parserGrammar.implicitLexer;
                    }
                }
        } else {
            // Not split, load standard
            parserGrammar = tool.loadGrammar(parserFile.getAbsolutePath());
            if (parserGrammar != null && parserGrammar.isCombined()) {
                lexerGrammar = parserGrammar.implicitLexer;
            } else if (parserGrammar instanceof LexerGrammar) {
                lexerGrammar = (LexerGrammar) parserGrammar;
                parserGrammar = null;
            }
        }

        if (parserGrammar == null && lexerGrammar == null) {
            throw new IllegalStateException("Failed to load parser grammar from " + parserFile.getName());
        }

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
        dynamicGrammar.setRawGrammarText(rawGrammarText);

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
