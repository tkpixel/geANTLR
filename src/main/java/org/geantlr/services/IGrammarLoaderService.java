package org.geantlr.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service contract for dynamically loading and discovering ANTLR grammar (.g4) files.
 */
public interface IGrammarLoaderService {

    /**
     * Finds all grammar files (.g4) in a specific directory.
     *
     * @param directory The directory to search in.
     * @return A list of paths to grammar files found in the directory.
     */
    List<Path> findGrammarFiles(File directory);

    /**
     * Reads the content of a grammar file.
     *
     * @param grammarFile The grammar file to read.
     * @return The text content of the grammar file.
     * @throws IOException If an error occurs reading the file.
     */
    String loadGrammarContent(File grammarFile) throws IOException;

    /**
     * Loads and compiles a grammar file into a dynamic grammar model capable of parsing text.
     *
     * @param grammarFile The grammar file to parse.
     * @return A DynamicGrammar instance representing the parsed grammar and its internal ATN models.
     * @throws Exception If compilation fails or the grammar is invalid.
     */
    DynamicGrammar loadDynamicGrammar(File grammarFile) throws Exception;
}
