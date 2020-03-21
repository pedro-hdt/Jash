package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.NULL_POINTER;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;


public class GrepApplicationTest {

    public static final String FOLDER1 = "Test-folder-1";
    public static final String FOLDER2 = "Test-folder-2";
    public static final String TEXTFILE = "textfile.txt";
    public static final String NO_READ_FILE = "file_noread_permission.txt";
    public static final String PATTERN = "pattern";
    public static final String CONTENT1 = "Patience is the key to success";
    public static final String UNREADABLE_FILE = "unreadableFile.txt";

    static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private static GrepApplication grepApplication;
    private static OutputStream stdout;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() +
            "GrepTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }


    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Try grep command with no file name
     */
    @Test
    public void testGrepWithNoInputOrFileStream() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"ff"}
            , null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    /**
     * Try grep command with null pattern and file name
     */
    @Test
    public void testGrepWithNullPatternAndFileName() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.grepFromFiles(null,
            false, false));
        assertTrue(expectedException.getMessage().contains(NULL_POINTER));
    }

    /**
     * Try grep with null pattern only
     */
    @Test
    public void testGrepWithNullPatternOnly() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i"}
            , System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    /**
     * Try grep with no pattern but proper file name
     */
    @Test
    public void testGrepWithNoPattern() throws AbstractApplicationException {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i",
            "", FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null, stdout));
        assertTrue(expectedException.getMessage().contains(EMPTY_PATTERN));
    }

    /**
     * Try grep with options other than -i and -c
     */
    @Test
    public void testGrepsWithIncorrectOption() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-z",
                PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}
            , System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    /**
     * Try grep with a directory instead of a file
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithDirectory() throws AbstractApplicationException {
        grepApplication.run(new String[] {PATTERN, FOLDER1}, null, stdout);
        assertTrue(stdout.toString().contains(IS_DIRECTORY));
    }

    /**
     * Try grep with correct option and pattern but no file name
     */
    @Test
    public void testGrepWithNoFileName() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i",
            PATTERN}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    /**
     * Try grep with invalid  regex
     */
    @Test
    public void testGrepWithInvalidRegex() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.grepFromFiles("[", true, true,
                FOLDER1 + StringUtils.fileSeparator() + "textfile.txt"));
        assertTrue(expectedException.getMessage().contains(ERR_INVALID_REGEX));
    }

    /**
     * Try grep with invalid  regex
     */
    @Test
    public void testGrepWithInvalidRegexStdIn() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin("[", true, true,
                System.in));
        assertTrue(expectedException.getMessage().contains(ERR_INVALID_REGEX));
    }

    /**
     * Try Grep With NullStream
     */
    @Test
    public void testGrepWithNullStream() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.grepFromStdin("[", true, true,
                null));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

    /**
     * Try grep with a not existing file
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithNonExistentFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {PATTERN, FOLDER1 + StringUtils.fileSeparator() + "text.txt"}, null, stdout);
        assertTrue(stdout.toString().contains(ERR_FILE_NOT_FOUND));

    }

    /**
     * Try grep with one file matching pattern correctly
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithOneMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null, stdout);
        assertEquals(PATTERN + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with no file containing matching pattern
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithNoMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with multiple files but only one file matching pattern correctly
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithMultipleFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", FOLDER1 + StringUtils.fileSeparator() + TEXTFILE,
                FOLDER2 + StringUtils.fileSeparator() + TEXTFILE},
            null, stdout);
        assertEquals(FOLDER2 + StringUtils.fileSeparator() + "textfile.txt: patterns" + StringUtils.STRING_NEWLINE,
            stdout.toString());
    }

    @Test
    public void testGrepWithNotReadableFile() throws AbstractApplicationException, IOException {

        Path path = Files.createFile(Paths.get(Environment.getCurrentDirectory(),FOLDER1, UNREADABLE_FILE));
        File file = path.toFile();
        file.setReadable(false);

        grepApplication.run(new String[] {PATTERN, FOLDER1 + StringUtils.fileSeparator() + UNREADABLE_FILE} , System.in, stdout);
        assertEquals(FOLDER1 + StringUtils.fileSeparator() + UNREADABLE_FILE + ": " + ERR_NO_PERM + StringUtils.STRING_NEWLINE, stdout.toString());

        file.setReadable(true);
        file.delete();
    }

    /**
     * Try grep with multiple files matching pattern correctly
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithMultipleMatchingFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE,
                "Test-folder-3" + StringUtils.fileSeparator() + TEXTFILE},
            null, stdout);
        assertEquals(FOLDER1 + StringUtils.fileSeparator() + "textfile.txt: pattern" + StringUtils.STRING_NEWLINE +
                "Test-folder-3" + StringUtils.fileSeparator() + "textfile.txt: pattern" + StringUtils.STRING_NEWLINE,
            stdout.toString());
    }

    /**
     * Try grep with pattern not matching due to sensitive case (by default)
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithSensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"Pattern", FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with option -i to check insensitive pattern matching
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithInsensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null,
            stdout);
        assertEquals(PATTERN + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with option -c to count number of lines matching
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-c", PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE}, null,
            stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with option -i and -c together
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithInsensitivePathAndCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", "-c", PATTERN, FOLDER1 + StringUtils.fileSeparator() + TEXTFILE},
            null, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with no matching pattern
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithNoMatchingPattern() throws AbstractApplicationException {
        String content = CONTENT1;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"patience"}, inputstream, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with input from stdin with matching pattern
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMatchingPattern() throws AbstractApplicationException {
        String content = CONTENT1;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"Patience"}, inputstream, stdout);
        assertEquals(CONTENT1 + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with case insensitive matching pattern
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithCaseInsensitiveMatchingPattern() throws AbstractApplicationException {
        String content = CONTENT1;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "patience"}, inputstream, stdout);
        assertEquals(CONTENT1 + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with option -c
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithCountLines() throws AbstractApplicationException {
        String content = CONTENT1;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with multi-line input from stdin with one matching pattern
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMultipleLines() throws AbstractApplicationException {
        String content1 = CONTENT1 + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with multi-line input from stdin with multiple matching patterns
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMultipleMatchingLines() throws AbstractApplicationException {
        String content1 = CONTENT1 + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "success"}, inputstream, stdout);
        assertEquals("2" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep input from stdin with-all options
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithAllOptions() throws AbstractApplicationException {
        String content1 = CONTENT1 + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "-c", "success"}, inputstream, stdout);
        assertEquals("2" + StringUtils.STRING_NEWLINE, stdout.toString());
    }
}
