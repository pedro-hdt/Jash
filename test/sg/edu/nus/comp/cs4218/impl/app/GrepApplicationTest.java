package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;


public class GrepApplicationTest {

    static final String originalDir = Environment.getCurrentDirectory();
    private static GrepApplication grepApplication;
    private static OutputStream stdout;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(originalDir + File.separator + "dummyTestFolder" + File.separator +
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

    @Test
    public void testGrepWithNoInputOrFileStream() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"ff"}
        , null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    @Test
    public void testGrepWithNullPattern() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i"}
            , System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    @Test
    public void testGrepWithNoPattern() throws AbstractApplicationException {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i",
            "", "Test-folder-1\\textfile.txt"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(EMPTY_PATTERN));
    }

    @Test
    public void testGrepsWithIncorrectOption() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-z"}
            , System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    @Test
    public void testGrepWithDirectory() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1"}, null, stdout);
        assertTrue(stdout.toString().contains(IS_DIRECTORY));
    }

    @Test
    public void testGrepWithNoFileName() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-i",
            "pattern"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    @Test
    public void testGrepWithNonExistentFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1\\text.txt"}, null, stdout);
        assertTrue(stdout.toString().contains(ERR_FILE_NOT_FOUND));

    }

    @Test
    public void testGrepWithOneMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("pattern\n", stdout.toString());
    }

    @Test
    public void testGrepWithNoMatchingFile() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("", stdout.toString());
    }

    @Test
    public void testGrepWithMultipleFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {"patterns", "Test-folder-1\\textfile.txt", "Test-folder-2\\textfile.txt"},
            null, stdout);
        assertEquals("Test-folder-2\\textfile.txt: patterns\n", stdout.toString());
    }

    @Test
    public void testGrepWithMultipleMatchingFiles() throws AbstractApplicationException {
        grepApplication.run(new String[] {"pattern", "Test-folder-1\\textfile.txt", "Test-folder-3\\textfile.txt"},
            null, stdout);
        assertEquals("Test-folder-1\\textfile.txt: pattern\n" + "Test-folder-3\\textfile.txt: pattern\n",
            stdout.toString());
    }

    @Test
    public void testGrepWithSensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"Pattern", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("", stdout.toString());
    }

    @Test
    public void testGrepWithInsensitivePattern() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", "Pattern", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("pattern\n", stdout.toString());
    }

    @Test
    public void testGrepWithCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-c", "pattern", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("1\n", stdout.toString());
    }

    @Test
    public void testGrepWithInsensitivePathAndCountLines() throws AbstractApplicationException {
        grepApplication.run(new String[] {"-i", "-c", "Pattern", "Test-folder-1\\textfile.txt"}, null, stdout);
        assertEquals("1\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithNoMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"patience"}, inputstream, stdout);
        assertEquals("", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithCaseSensitiveMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"Patience"}, inputstream, stdout);
        assertEquals("Patience is the key to success\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithCaseInsensitiveMatchingPattern() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "patience"}, inputstream, stdout);
        assertEquals("Patience is the key to success\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithCountLines() throws AbstractApplicationException {
        String content = "Patience is the key to success";
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithMultipleLines() throws AbstractApplicationException {
        String content1 = "Patience is the key to success\n";
        String content2 = "No success without hard work\n";
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "Patience"}, inputstream, stdout);
        assertEquals("1\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithMultipleMatchingLines() throws AbstractApplicationException {
        String content1 = "Patience is the key to success\n";
        String content2 = "No success without hard work\n";
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-c", "success"}, inputstream, stdout);
        assertEquals("2\n", stdout.toString());
    }

    @Test
    public void testGrepFromStdinWithAllOptions() throws AbstractApplicationException {
        String content1 = "Patience is the key to Success\n";
        String content2 = "No success without hard work\n";
        String content = content1 + content2;
        InputStream inputstream = new ByteArrayInputStream(content.getBytes());
        String out = "";
        grepApplication.run(new String[] {"-i", "-c", "success"}, inputstream, stdout);
        assertEquals("2\n", stdout.toString());
    }
}
