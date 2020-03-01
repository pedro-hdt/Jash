package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;


public class GrepApplicationTest {

    static final String originalDir = Environment.getCurrentDirectory();
    private static GrepApplication grepApplication;
    private static OutputStream stdout;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(originalDir + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() +
            "GrepTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(originalDir);
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
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.grepFromFiles(null, false, false));
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
            "", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(EMPTY_PATTERN));
    }

    /**
     * Try grep with options other than -i and -c
     */
    @Test
    public void testGrepsWithIncorrectOption() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-z", "pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}
            , System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    /**
     * Try grep with a directory instead of a file
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithDirectory() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1"}, null, stdout);
        assertTrue(stdout.toString().contains(IS_DIRECTORY));
    }

    /**
     * Try grep with correct option and pattern but no file name
     */
    @Test
    public void testGrepWithNoFileName() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i",
            "pattern"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    /**
     * Try grep with a not existing file
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithNonExistentFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1" + StringUtils.fileSeparator() + "text.txt"}, null, stdout);
        assertTrue(stdout.toString().contains(ERR_FILE_NOT_FOUND));

    }

    /**
     * Try grep with one file matching pattern correctly
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithOneMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("pattern" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with no file containing matching pattern
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithNoMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with multiple files but only one file matching pattern correctly
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithMultipleFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt", "Test-folder-2" + StringUtils.fileSeparator() + "textfile.txt"},
            null, stdout);
        assertEquals("Test-folder-2" + StringUtils.fileSeparator() + "textfile.txt: patterns" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with multiple files matching pattern correctly
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithMultipleMatchingFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt", "Test-folder-3" + StringUtils.fileSeparator() + "textfile.txt"},
            null, stdout);
        assertEquals("Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt: pattern" + StringUtils.STRING_NEWLINE + "Test-folder-3" + StringUtils.fileSeparator() + "textfile.txt: pattern" + StringUtils.STRING_NEWLINE,
            stdout.toString());
    }

    /**
     * Try grep with pattern not matching due to sensitive case (by default)
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithSensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"Pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with option -i to check insensitive pattern matching
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithInsensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", "Pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("pattern" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with option -c to count number of lines matching
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-c", "pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with option -i and -c together
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepWithInsensitivePathAndCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", "-c", "Pattern", "Test-folder-1" + StringUtils.fileSeparator() + "textfile.txt"}, null, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with no matching pattern
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithNoMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"patience"}, inputstream, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try grep with input from stdin with matching pattern
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"Patience"}, inputstream, stdout);
        assertEquals("Patience is the key to success" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with case insensitive matching pattern
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithCaseInsensitiveMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "patience"}, inputstream, stdout);
        assertEquals("Patience is the key to success" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with input from stdin with option -c
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithCountLines() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with multi-line input from stdin with one matching pattern
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMultipleLines() throws AbstractApplicationException {
        String content1 = "Patience is the key to success" + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep with multi-line input from stdin with multiple matching patterns
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithMultipleMatchingLines() throws AbstractApplicationException {
        String content1 = "Patience is the key to success" + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "success"}, inputstream, stdout);
        assertEquals("2" + StringUtils.STRING_NEWLINE, stdout.toString());
    }

    /**
     * Try grep input from stdin withall options
     * @throws AbstractApplicationException
     */
    @Test
    public void testGrepFromStdinWithAllOptions() throws AbstractApplicationException {
        String content1 = "Patience is the key to Success" + StringUtils.STRING_NEWLINE;
        String content2 = "No success without hard work" + StringUtils.STRING_NEWLINE;
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "-c", "success"}, inputstream, stdout);
        assertEquals("2" + StringUtils.STRING_NEWLINE, stdout.toString());
    }
}
