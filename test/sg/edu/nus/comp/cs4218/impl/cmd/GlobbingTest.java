package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Tests globbing by integrating with commands
 */
public class GlobbingTest {

    public static final String MV_FILE_TXT = "mvFile.txt";
    public ShellImpl shell;
    private ArgumentResolver argumentResolver;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "GlobbingTest");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        shell = new ShellImpl();
        argumentResolver = new ArgumentResolver();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Globbing when no matches for ls
     */
    @Test
    public void testGlobbingWithNoMatches() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls *.md", stdout);

        assertTrue(stdout.toString().contains(ERR_FILE_NOT_FOUND));
    }

    /**
     * Globbing which returns 1 file
     */
    @Test
    public void testGlobbingWithMatch() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls match*", stdout);

        assertTrue(stdout.toString().contains("match1.txt"));
    }

    /**
     * Globbing which returns multiple files
     */
    @Test
    public void testGlobbingWithMultipleMatches() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls *.txt", stdout);

        assertTrue(stdout.toString().contains("f1.txt" + StringUtils.STRING_NEWLINE + "match1.txt"));
    }

    /**
     * Globbing which returns match with dir
     * NOTE: test will fail until EF1 fix for globbing is made, hence ignored
     */
    @Test
    public void testGlobbingWithLsIntegration() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls dire/*", stdout);

        assertTrue(stdout.toString().contains("fileInsideDir.txt"));
    }


    /**
     * Move file with globbing to another directory
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    @Disabled("TEMP. as its modifying the folder structure. Put this test under integration")
    public void testGlobbingIntegrationWithMv() throws AbstractApplicationException, ShellException, IOException {

        assertFalse(Files.exists(IOUtils.resolveFilePath("dire" + StringUtils.fileSeparator() + MV_FILE_TXT)));

        shell.parseAndEvaluate("mv mvFil* dire/", stdout);

        assertTrue(Files.exists(IOUtils.resolveFilePath("dire" + StringUtils.fileSeparator() + MV_FILE_TXT)));

        // Reset after moving file to dir
        Files.createFile(IOUtils.resolveFilePath(MV_FILE_TXT));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + "dire" + StringUtils.fileSeparator() + MV_FILE_TXT));
    }

    @Test
    void testResolveOneArgument_singleAsterisk_nonExistentFolder() throws AbstractApplicationException, ShellException {
        ArgumentResolver argumentResolver = new ArgumentResolver();
        String input = Environment.getCurrentDirectory() + StringUtils.fileSeparator() + "nonExistent" + StringUtils.fileSeparator() + "*";
        List<String> expected = Arrays.asList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgument_multipleAsterisk_folderWithFile_fileExtension() throws AbstractApplicationException, ShellException {
        String input = "*.t*";
        List<String> expected = Arrays.asList("abc.tat", "f1.tad", "f1.txt", "match1.txt", "mvFile.txt", "test.txt");
        Collections.sort(expected);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        input =  "*.t*t";
        expected = Arrays.asList("abc.tat", "f1.txt", "match1.txt", "mvFile.txt", "test.txt");
        Collections.sort(expected);
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        input =  "*.*a*";
        expected = Arrays.asList("abc.tat", "f1.tad");
        Collections.sort(expected);
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgument_multipleAsterisksInARow_folderWithFile_workTheSameAsSingleAsterisk() throws AbstractApplicationException, ShellException {
        // Double asterisks
        String input = "**";
        List<String> expected = Arrays.asList("abc.tat", "dire", "f1.tad", "f1.txt", "match1.txt", "mvFile.txt", "test.txt");
        Collections.sort(expected);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        // Multiple asterisks
        input = "*******";
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgument_singleAsterisk_folderWithFile_fileExtension_nonExistent() throws AbstractApplicationException, ShellException {
        String input = "dire" + StringUtils.fileSeparator() + "*.t";
        List<String> expected = Collections.singletonList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgumentInsideFolder() throws AbstractApplicationException, ShellException {
        String input = "dire" + StringUtils.fileSeparator() + "*";
        List<String> expected = Collections.singletonList("dire" + StringUtils.fileSeparator() + "fileInsideDir.txt");
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgument_singleAsterisk_emptyFolder() throws AbstractApplicationException, ShellException, IOException {

        Path path = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "emptyFolder"));
        File file = path.toFile();

        String input = file.getName() + StringUtils.fileSeparator() + "*";
        // Not sure about the expected output
        List<String> expected = Collections.singletonList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        file.delete();
    }


}
