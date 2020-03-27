package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Tests for cut command.
 * <p>
 * Negative test cases:
 * - Null output stream
 * - Null args
 * - Insufficient args
 * - File not found
 * - Is directory
 * - Empty stdin
 * - Null Stdin
 * - Error permission to read
 * - Illegal range
 * - Invalid flag
 * <p>
 * Positive test cases:
 * - "-c" flag used + single number with file/stdin
 * - "-c" flag used + comma separated numbers with file/stdin
 * - "-c" flag used + range of numbers with file/stdin
 * - "-c" flag used + range of numbers with multiple files
 * - "-b" flag used + single number with file/stdin
 * - "-b" flag used + comma separated numbers with file/stdin
 * - "-b" flag used + range of numbers with file/stdin
 * - "-b" flag used + range of numbers with multiple files
 */
public class CutApplicationTest {
    private static CutApplication cutApp;
    private static OutputStream stdout;
    
    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private static final String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
    private static final String CUT1_FILE = "cut1.txt";
    private static final String CUT2_FILE = "cut2.txt";
    
    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(CUT_TEST_DIR);
    }
    
    @BeforeEach
    void setUp() {
        cutApp = new CutApplication();
        stdout = new ByteArrayOutputStream();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }
    
    @Test
    public void testFailsWithNullOutputstream() {
        String[] args = new String[]{};
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }
    
    @Test
    public void testFailsWithNullArgs() {
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(null, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));
    }
    
//    @Test
//    public void testProcessInputFailsWithNullStream() {
//        Exception expectedException = assertThrows(CutException.class, () -> cutApp.processInput(true, false, null, true, 1, 2));
//        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
//    }
    
    @Test
    public void testCutFromFilesFailsWithNullFile() {
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.cutFromFiles(true, false, true, 1, 2, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));
    }
    
    @Test
    public void testInvalidRange() {
        String[] args = new String[]{"-c", "5,1", CUT1_FILE}; // NOPMD
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_INVALID_RANGE));
    }
    
//    @Test
//    public void testOutOfRange() {
//        String[] args = new String[]{"-c", "0,4", CUT1_FILE}; // NOPMD
//        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
//        assertTrue(expectedException.getMessage().contains(ERR_OUT_OF_RANGE));
//    }
    
    @Test
    public void testIllegalFlag() {
        String[] args = new String[]{"-f", "1,4", CUT1_FILE}; // NOPMD
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_INVALID_FLAG));
    }
    
    /**
     * When folder with no read permission is passed
     *
     * @throws IOException
     */
    @Test
    public void testInvalidUnreadableFile() throws IOException {
    
        Path path = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "unreadable"));
        File file = path.toFile();
        file.setReadable(false);
    
        Exception exception = assertThrows(Exception.class, ()
          -> cutApp.cutFromFiles(true, false, true, 1, 2, path.toString()));
        assertEquals(exception.getMessage(), "cut: " + ERR_NO_PERM);
    
        file.setReadable(true);
        file.delete();
    }
    
    @Test
    public void testFailsWithInsufficientArgs() {
        String[] args = new String[]{};
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_ARGS));
    }
    
    @Test
    public void testFailsWithInvalidFile() {
        String[] args = new String[]{"-c", "2", "invalid.txt"};
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }
    
    @Test
    public void testFailsWithInvalidDir() {
        String[] args = new String[]{"-c", "2", "dummyDir"};
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }
    
    @Test
    public void testFailsWithEmptyStdin() {
        String[] args = new String[]{"-c", "2", "-"};
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }
    
    @Test
    public void testCutOnFileWithSingleNumberUsingFlagC() throws AbstractApplicationException {
        String[] args = new String[]{"-c", "5", CUT1_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }
    
    @Test
    public void testCutOnStdinWithSingleNumberUsingFlagC() {
        String[] args = new String[]{"-c", "5", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("y" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }
    
    @Test
    public void testCutOnFileWithCommaSeparatedNumbersUsingFlagC() throws AbstractApplicationException {
        String[] args = new String[]{"-c", "5,10", CUT1_FILE}; // NOPMD
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }
    
    @Test
    public void testCutOnStdinWithCommaSeparatedNumbersUsingFlagC() {
        String[] args = new String[]{"-c", "5,10", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnFileWithRangeOfNumbersUsingFlagC() throws AbstractApplicationException {
        String[] args = new String[]{"-c", "5-10", CUT1_FILE}; // NOPMD
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE, stdout.toString()); // NOPMD
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }
    
    @Test
    public void testCutOnStdinWithRangeOfNumbersUsingFlagC() {
        String[] args = new String[]{"-c", "5-10", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnMultipleFilesWithRangeOfNumbersUsingFlagC() throws AbstractApplicationException {
        String[] args = new String[]{"-c", "5-10", CUT1_FILE, CUT2_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE +
              "y is W" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnFileWithSingleNumberUsingFlagB() throws AbstractApplicationException {
        String[] args = new String[]{"-b", "5", CUT1_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnStdinWithSingleNumberUsingFlagB() {
        String[] args = new String[]{"-b", "5", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("y" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnFileWithCommaSeparatedNumbersUsingFlagB() throws AbstractApplicationException {
        String[] args = new String[]{"-b", "5,10", CUT1_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnStdinWithCommaSeparatedNumbersUsingFlagB() {
        String[] args = new String[]{"-b", "5,10", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnFileWithRangeOfNumbersUsingFlagB() throws AbstractApplicationException {
        String[] args = new String[]{"-b", "5-10", CUT1_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnStdinWithRangeOfNumbersUsingFlagB() {
        String[] args = new String[]{"-b", "5-10", "-"};
        
        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testCutOnMultipleFilesWithRangeOfNumbersUsingFlagB() throws AbstractApplicationException {
        String[] args = new String[]{"-b", "5-10", CUT1_FILE, CUT2_FILE};
        try {
            cutApp.run(args, System.in, stdout);
            assertEquals("y is T" + StringUtils.STRING_NEWLINE +
              "y is W" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (CutException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
}
