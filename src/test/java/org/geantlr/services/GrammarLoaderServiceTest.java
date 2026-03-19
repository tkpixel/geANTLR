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
        grammarFile1 = Files.createFile(tempDir.resolve("test1.g4"));
        Files.writeString(grammarFile1, "grammar Test1;");

        grammarFile2 = Files.createFile(tempDir.resolve("test2.g4"));
        Files.writeString(grammarFile2, "grammar Test2;");

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
        assertTrue(files.contains(grammarFile1), "Should contain test1.g4");
        assertTrue(files.contains(grammarFile2), "Should contain test2.g4");
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
        assertEquals("grammar Test1;", content, "Content should match what was written");
    }

    @Test
    void loadGrammarContent_InvalidFile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.loadGrammarContent(new File("nonexistent_file.g4"));
        }, "Should throw exception for nonexistent file");
    }
}
