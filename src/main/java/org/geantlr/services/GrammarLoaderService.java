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

    private String extractAndRemove(StringBuilder sb, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(sb.toString());
        if (m.find()) {
            String found = m.group();
            sb.delete(m.start(), m.end());
            return found;
        }
        return "";
    }

    private String extractTokensContent(StringBuilder sb) {
        StringBuilder content = new StringBuilder();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?s)tokens\\s*\\{([^}]*)\\}").matcher(sb.toString());
        while (m.find()) {
            content.append(m.group(1)).append(", ");
            sb.delete(m.start(), m.end());
            m = java.util.regex.Pattern.compile("(?s)tokens\\s*\\{([^}]*)\\}").matcher(sb.toString());
        }
        return content.toString();
    }

    private String extractOptionsContent(StringBuilder sb) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?s)options\\s*\\{([^}]*)\\}").matcher(sb.toString());
        if (m.find()) {
            String content = m.group(1);
            sb.delete(m.start(), m.end());
            // Remove tokenVocab
            content = content.replaceAll("(?i)tokenVocab\\s*=\\s*[a-zA-Z0-9_]+\\s*;", "");
            if (content.trim().isEmpty()) {
                return "";
            }
            return "options {" + content + "}\n";
        }
        return "";
    }

    private String mergeGrammars(String parserText, String lexerText, String combinedName) {
        StringBuilder pText = new StringBuilder(parserText);
        StringBuilder lText = new StringBuilder(lexerText);

        // Remove headers
        extractAndRemove(pText, "(?i)parser\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;");
        extractAndRemove(lText, "(?i)lexer\\s+grammar\\s+[a-zA-Z0-9_]+\\s*;");

        // Options
        String pOptions = extractOptionsContent(pText);
        extractOptionsContent(lText); // Lexer options are discarded to prevent conflicts

        // Imports
        String pImports = extractAndRemove(pText, "(?i)import\\s+[a-zA-Z0-9_]+\\s*;");
        String lImports = extractAndRemove(lText, "(?i)import\\s+[a-zA-Z0-9_]+\\s*;");

        // Tokens
        String combinedTokensContent = extractTokensContent(pText) + extractTokensContent(lText);
        String tokensBlock = combinedTokensContent.trim().isEmpty() ? "" : "tokens { " + combinedTokensContent + " }\n";

        // Channels
        String channelsBlock = extractAndRemove(lText, "(?s)channels\\s*\\{[^}]*\\}");

        // Build strictly ordered grammar
        return "grammar " + combinedName + ";\n"
                + pOptions
                + pImports + "\n"
                + lImports + "\n"
                + tokensBlock
                + channelsBlock + "\n"
                + pText.toString() + "\n"
                + lText.toString();
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

            String combinedName = "CombinedGrammar" + System.currentTimeMillis();
            String combinedText = mergeGrammars(parserText, lexerText, combinedName);

            // Write combined text to a temporary file to let ANTLR Tool process it robustly with full context
            File tempCombinedFile = new File(parserFile.getParentFile(), combinedName + ".g4");
            tempCombinedFile.deleteOnExit();
            Files.writeString(tempCombinedFile.toPath(), combinedText);

            parserGrammar = tool.loadGrammar(tempCombinedFile.getAbsolutePath());

            if (parserGrammar != null && parserGrammar.isCombined()) {
                lexerGrammar = parserGrammar.implicitLexer;
            }

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

                    String combinedName = "CombinedGrammar" + System.currentTimeMillis();
                    String combinedText = mergeGrammars(parserText, lexerText, combinedName);

                    File tempCombinedFile = new File(parserFile.getParentFile(), combinedName + ".g4");
                    tempCombinedFile.deleteOnExit();
                    Files.writeString(tempCombinedFile.toPath(), combinedText);

                    parserGrammar = tool.loadGrammar(tempCombinedFile.getAbsolutePath());

                    if (parserGrammar != null && parserGrammar.isCombined()) {
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
