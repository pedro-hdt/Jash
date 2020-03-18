package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "GlobbingTest");
    }

    @AfterAll
    static void reset() throws IOException {

        // Reset after moving file to dir
        Files.createFile(IOUtils.resolveFilePath(MV_FILE_TXT));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + "dire" + StringUtils.fileSeparator() + MV_FILE_TXT));

        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        shell = new ShellImpl();
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

        assertTrue(stdout.toString().contains("match1.txt" + StringUtils.STRING_NEWLINE + "test.txt"));
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
    public void testGlobbingIntegrationWithMv() throws AbstractApplicationException, ShellException {

        assertFalse(Files.exists(IOUtils.resolveFilePath("dire" + StringUtils.fileSeparator() + MV_FILE_TXT)));

        shell.parseAndEvaluate("mv mvFil* dire/", stdout);

        assertTrue(Files.exists(IOUtils.resolveFilePath("dire" + StringUtils.fileSeparator() + MV_FILE_TXT)));
    }

    @Test
    void testResolveOneArgument_singleAsterisk_nonExistentFolder() throws AbstractApplicationException, ShellException {
        ArgumentResolver argumentResolver = new ArgumentResolver();
        String input = Environment.getCurrentDirectory() + StringUtils.fileSeparator() + "nonExistent" + StringUtils.fileSeparator() + "*";
        List<String> expected = Arrays.asList(input);
        List<String> actual = argumentResolver.resolveOneArgument(input);
        assertEquals(expected, actual);
    }
}
