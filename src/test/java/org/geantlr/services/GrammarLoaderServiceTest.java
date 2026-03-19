package org.geantlr.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrammarLoaderServiceTest {

    private GrammarLoaderService service;
    private Path tempDir;
    private Path grammarFile1;
    private Path grammarFile2;
    private Path textFile;

    @BeforeEach
    void setUp() throws IOException {
        service = new GrammarLoaderService();
        tempDir = Files.createTempDirectory("grammarTest");

        // Create some dummy .g4 files and a .txt file
        grammarFile1 = Files.createFile(tempDir.resolve("Test1.g4"));
        Files.writeString(grammarFile1, "grammar Test1; a : 'b' ;");

        grammarFile2 = Files.createFile(tempDir.resolve("Test2.g4"));
        Files.writeString(grammarFile2, "grammar Test2; a : 'b' ;");

        textFile = Files.createFile(tempDir.resolve("notGrammar.txt"));
        Files.writeString(textFile, "just some text");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(grammarFile1);
        Files.deleteIfExists(grammarFile2);
        Files.deleteIfExists(textFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void findGrammarFiles_ReturnsOnlyG4Files() {
        List<Path> files = service.findGrammarFiles(tempDir.toFile());

        assertEquals(2, files.size(), "Should find exactly 2 grammar files");
        assertTrue(files.contains(grammarFile1), "Should contain Test1.g4");
        assertTrue(files.contains(grammarFile2), "Should contain Test2.g4");
    }

    @Test
    void findGrammarFiles_EmptyDirectory_ReturnsEmptyList() throws IOException {
        Path emptyDir = Files.createTempDirectory("emptyTest");
        List<Path> files = service.findGrammarFiles(emptyDir.toFile());

        assertTrue(files.isEmpty(), "Should return empty list for empty directory");
        Files.deleteIfExists(emptyDir);
    }

    @Test
    void findGrammarFiles_InvalidDirectory_ReturnsEmptyList() {
        List<Path> files = service.findGrammarFiles(new File("nonexistent_directory"));

        assertTrue(files.isEmpty(), "Should return empty list for nonexistent directory");
    }

    @Test
    void loadGrammarContent_ValidFile_ReturnsContent() throws IOException {
        String content = service.loadGrammarContent(grammarFile1.toFile());
        assertEquals("grammar Test1; a : 'b' ;", content, "Content should match what was written");
    }

    @Test
    void loadGrammarContent_InvalidFile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.loadGrammarContent(new File("nonexistent_file.g4"));
        }, "Should throw exception for nonexistent file");
    }

    @Test
    void loadDynamicGrammar_ValidFile_ReturnsDynamicGrammar() throws Exception {
        DynamicGrammar grammar = service.loadDynamicGrammar(grammarFile1.toFile());

        assertNotNull(grammar, "Should return a DynamicGrammar instance");
        assertNotNull(grammar.getParserGrammar(), "Parser grammar should not be null");
        assertNotNull(grammar.getLexerGrammar(), "Implicit lexer grammar should not be null");
        assertEquals("Test1", grammar.getParserGrammar().name, "Grammar name should match");
        assertEquals("Test1Lexer", grammar.getLexerGrammar().name, "Lexer grammar name should match");
        assertTrue(grammar.getParserGrammar().rules.size() > 0, "Parser should have rules");
    }

    @Test
    void loadDynamicGrammar_InvalidFile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.loadDynamicGrammar(new File("nonexistent_file.g4"));
        }, "Should throw exception for nonexistent file");
    }

    @Test
    void testImportDirectoriesManagement() throws Exception {
        assertTrue(service.getImportDirectories().isEmpty());

        File newDir = tempDir.toFile();
        service.addImportDirectory(newDir);
        assertEquals(1, service.getImportDirectories().size());
        assertTrue(service.getImportDirectories().contains(newDir));

        service.clearImportDirectories();
        assertTrue(service.getImportDirectories().isEmpty());
    }

    @Test
    void testLoadDynamicGrammarWithImports() throws Exception {
        Path importDir1 = Files.createTempDirectory("importDir1");
        Path importDir2 = Files.createTempDirectory("importDir2");
        Path mainDir = Files.createTempDirectory("mainDir");

        try {
            // Create A.g4 in importDir1
            Path aFile = Files.createFile(importDir1.resolve("A.g4"));
            Files.writeString(aFile, "grammar A; a : 'a' ;");

            // Create B.g4 in importDir2, imports A
            Path bFile = Files.createFile(importDir2.resolve("B.g4"));
            Files.writeString(bFile, "grammar B; import A; b : a ;");

            // Create C.g4 in mainDir, imports B
            Path cFile = Files.createFile(mainDir.resolve("C.g4"));
            Files.writeString(cFile, "grammar C; import B; c : b ;");

            // We must add importDir1 and importDir2 so C can find B, and B can find A
            service.addImportDirectory(importDir1.toFile());
            service.addImportDirectory(importDir2.toFile());

            DynamicGrammar grammar = service.loadDynamicGrammar(cFile.toFile());

            assertNotNull(grammar);
            assertNotNull(grammar.getParserGrammar());
            assertEquals("C", grammar.getParserGrammar().name);
            // It should have resolved imports successfully
            assertTrue(grammar.getParserGrammar().rules.size() > 0);
        } finally {
            Files.deleteIfExists(importDir1.resolve("A.g4"));
            Files.deleteIfExists(importDir1);
            Files.deleteIfExists(importDir2.resolve("B.g4"));
            Files.deleteIfExists(importDir2);
            Files.deleteIfExists(mainDir.resolve("C.g4"));
            Files.deleteIfExists(mainDir);
        }
    }
}
