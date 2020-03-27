package sg.edu.nus.comp.cs4218.impl.cmd;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

/**
 * Tests globbing by integrating with commands
 */
public class GlobbingTest {
    
    public static final String MV_FILE_TXT = "mvFile.txt";
    public static final String MATCH1_TXT = "match1.txt";
    public static final String F1_TXT = "f1.txt";
    public static final String DIRE = "dire";
    public static final String ABC_TAT_FILE = "abc.tat";
    public static final String F1_TAD_FILE = "f1.tad";
    public ShellImpl shell;
    private ArgumentResolver argumentResolver;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.currentDirectory;

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

        assertTrue(stdout.toString().contains(MATCH1_TXT));
    }

    /**
     * Globbing which returns multiple files
     */
    @Test
    public void testGlobbingWithMultipleMatches() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls *.txt", stdout);

        assertTrue(stdout.toString().contains(F1_TXT + StringUtils.STRING_NEWLINE + MATCH1_TXT));
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
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    @Disabled("TEMP. as its modifying the folder structure. Put this test under integration")
    public void testGlobbingIntegrationWithMv() throws AbstractApplicationException, ShellException, IOException {

        assertFalse(Files.exists(IOUtils.resolveFilePath(DIRE + StringUtils.fileSeparator() + MV_FILE_TXT)));

        shell.parseAndEvaluate("mv mvFil* dire/", stdout);

        assertTrue(Files.exists(IOUtils.resolveFilePath(DIRE + StringUtils.fileSeparator() + MV_FILE_TXT)));

        // Reset after moving file to dir
        Files.createFile(IOUtils.resolveFilePath(MV_FILE_TXT));
        Files.deleteIfExists(Paths.get(Environment.currentDirectory
          + StringUtils.fileSeparator() + DIRE + StringUtils.fileSeparator() + MV_FILE_TXT));
    }

    @Test
    void testSingleAsteriskInvalidFile() throws AbstractApplicationException, ShellException {
        ArgumentResolver argumentResolver = new ArgumentResolver();
        String input = Environment.currentDirectory + StringUtils.fileSeparator() + "nonExistent" + StringUtils.fileSeparator() + "*";
        List<String> expected = Arrays.asList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testMultipleAsterisks() throws AbstractApplicationException, ShellException {
        String input = "*.t*";
        List<String> expected = Arrays.asList(ABC_TAT_FILE, F1_TAD_FILE, F1_TXT, MATCH1_TXT, MV_FILE_TXT, "test.txt");
        Collections.sort(expected);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    
        input = "*.t*t";
        expected = Arrays.asList(ABC_TAT_FILE, F1_TXT, MATCH1_TXT, MV_FILE_TXT, "test.txt");
        Collections.sort(expected);
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    
        input = "*.*a*";
        expected = Arrays.asList(ABC_TAT_FILE, F1_TAD_FILE);
        Collections.sort(expected);
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testMultipleAsterisksInARow() throws AbstractApplicationException, ShellException {
        // Double asterisks
        String input = "**";
        List<String> expected = Arrays.asList(ABC_TAT_FILE, DIRE, F1_TAD_FILE, F1_TXT, MATCH1_TXT, "mvFile.txt", "test.txt");
        Collections.sort(expected);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        // Multiple asterisks
        input = "*******";
        actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testSingleAsteriskNonExistentFile() throws AbstractApplicationException, ShellException {
        String input = DIRE + StringUtils.fileSeparator() + "*.t";
        List<String> expected = Collections.singletonList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testResolveOneArgumentInsideFolder() throws AbstractApplicationException, ShellException {
        String input = DIRE + StringUtils.fileSeparator() + "*";
        List<String> expected = Collections.singletonList(DIRE + StringUtils.fileSeparator() + "fileInsideDir.txt");
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }

    @Test
    void testEmptyFolderGlobal() throws AbstractApplicationException, ShellException, IOException {

        Path path = Files.createDirectory(Paths.get(Environment.currentDirectory, "emptyFolder"));
        File file = path.toFile();

        String input = file.getName() + StringUtils.fileSeparator() + "*";
        // Not sure about the expected output
        List<String> expected = Collections.singletonList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);

        file.delete();
    }


}
