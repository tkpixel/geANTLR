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
        return loadDynamicGrammar(grammarFile.getParentFile(), null, grammarFile);
    }

    @Override
    public DynamicGrammar loadDynamicGrammar(File importDir, File lexerFile, File parserFile) throws Exception {
        if (parserFile == null || !parserFile.exists() || !parserFile.isFile()) {
            throw new IllegalArgumentException("Invalid parser grammar file provided.");
        }

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

        // Process explicit split grammars via In-Memory Merge
        if (lexerFile != null && lexerFile.exists()) {
            String lexerText = loadGrammarContent(lexerFile);
            String parserText = rawGrammarText;

            // Remove header declarations
            lexerText = lexerText.replaceAll("(?i)lexer\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;", "");
            parserText = parserText.replaceAll("(?i)parser\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;", "");

            // Remove options blocks entirely to prevent conflicts and ATN initialization crashes
            parserText = parserText.replaceAll("(?s)options\\s*\\{.*?\\}", "");
            lexerText = lexerText.replaceAll("(?s)options\\s*\\{.*?\\}", "");

            // Remove import directives that could cause resolution loops
            parserText = parserText.replaceAll("(?i)import\\s+[a-zA-Z0-9_]+\\s*;", "");
            lexerText = lexerText.replaceAll("(?i)import\\s+[a-zA-Z0-9_]+\\s*;", "");

            String combinedText = "grammar CombinedGrammar;\n" + parserText + "\n" + lexerText;

            // Create in-memory combined grammar
            parserGrammar = new Grammar(combinedText);
            tool.process(parserGrammar, false);

            if (parserGrammar.isCombined()) {
                lexerGrammar = parserGrammar.implicitLexer;
            }

            // The raw text displayed in the editor should ideally be the combined text
            // so line numbers match up for errors.
            rawGrammarText = combinedText;
        } else {
            // Attempt to resolve implicitly like before if only parser was provided
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
                    String lexerText = loadGrammarContent(inferredLexer);
                    String parserText = rawGrammarText;

                    lexerText = lexerText.replaceAll("(?i)lexer\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;", "");
                    parserText = parserText.replaceAll("(?i)parser\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;", "");

                    // Remove options and imports entirely to prevent conflicts
                    parserText = parserText.replaceAll("(?s)options\\s*\\{.*?\\}", "");
                    lexerText = lexerText.replaceAll("(?s)options\\s*\\{.*?\\}", "");
                    parserText = parserText.replaceAll("(?i)import\\s+[a-zA-Z0-9_]+\\s*;", "");
                    lexerText = lexerText.replaceAll("(?i)import\\s+[a-zA-Z0-9_]+\\s*;", "");

                    String combinedText = "grammar CombinedGrammar;\n" + parserText + "\n" + lexerText;

                    parserGrammar = new Grammar(combinedText);
                    tool.process(parserGrammar, false);

                    if (parserGrammar.isCombined()) {
                        lexerGrammar = parserGrammar.implicitLexer;
                    }
                    rawGrammarText = combinedText;
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
