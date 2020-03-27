package tddmod.ef1;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.DiffException;
import sg.edu.nus.comp.cs4218.impl.app.DiffApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

/**
 * Tests for diff command.
 * <p>
 * Negative test cases:
 * - Invalid file
 * - Invalid directory
 * - Directory without files
 * <p>
 * Positive test cases:
 * - No flag used + Files/stdin with same content
 * - "-s" flag used + Files/stdin with same content
 * - "-B" flag used + Files/stdin with same content
 * - "-sB" flags used + Files with same content
 * - No flag used + Directories containing Files with same content
 * - "-s" flag used + Directories containing Files with same content
 * <p>
 * - No flag used + Files/stdin with different content
 * - "-q" flag used + Files/stdin with different content
 * - "-Bq" flags used + Files/stdin with different content
 * - "sBq" flags used + Files/stdin with different content
 * - No flag used + Directories containing Files with different content
 * - "-q" flag used + Directories containing Files with different content
 */
@SuppressWarnings("PMD")
public class DiffApplicationTest { // NOPMD
    private static DiffApplication diffApp;
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String DIFF_TEST_DIR = ORIGINAL_DIR
      + StringUtils.fileSeparator() + "test"
      + StringUtils.fileSeparator() + "tddmod/util"
      + StringUtils.fileSeparator() + "dummyTestFolder"
      + StringUtils.fileSeparator() + "DiffTestFolder";
    private static OutputStream stdout;
    
    private static final String DIFF1_FILE = "diff1.txt";
    private static final String DIFF1_IDENTICAL_FILE = "diff1-identical.txt"; // NOPMD
    private static final String DIFF1_BLANK_LINES_FILE = "diff1-blank-lines.txt"; // NOPMD
    private static final String DIFF2_FILE = "diff2.txt";
    
    private static final String DIFFDIR1 = "diffDir1";
    private static final String DIFFDIR1_IDENTICAL = "diffDir1-identical"; // NOPMD
    private static final String DIFFDIR2 = "diffDir2";
    
    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(DIFF_TEST_DIR);
    }
    
    @BeforeEach
    void setUp() {
        diffApp = new DiffApplication();
        stdout = new ByteArrayOutputStream();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
        Environment.setCurrentDirectory(DIFF_TEST_DIR);
    }
    
    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    @Test
    public void testFailsWithInvalidFile() {
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoFiles("invalidFile.txt", "invalidFile.txt", false, false, false));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }
    
    @Test
    public void testFailsWithInvalidDir() {
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoFiles("invalidDir", "invalidDir", false, false, false));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }
    
    @Test
    @Disabled("The GNU's implementation of diff allows comparison of directories with empty files")
    public void testFailsWithDirWithoutFiles() {
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoDir("dummyDir", "dummyDir", false, false, false));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }
    
    @Test
    public void testDiffFilesWithSameContent() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_IDENTICAL_FILE, false, false, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithSameContent() throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE)); //NOPMD
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFilesWithSameContentUsingFlagS() {
        try {
            String expected = diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_IDENTICAL_FILE, true, false, false);
            assertEquals(expected, "Files [" + DIFF1_FILE + " " + DIFF1_IDENTICAL_FILE + "] are identical"); // NOPMD
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithSameContentUsingFlagS() throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, true, false, false);
            assertEquals(expected, "Files [" + DIFF1_FILE + " -] are identical");
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFilesWithSameContentUsingFlagB() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_BLANK_LINES_FILE, false, true, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithSameContentUsingFlagB() throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_BLANK_LINES_FILE)); //NOPMD
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, true, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFilesWithSameContentUsingFlagSB() {
        try {
            String expected = diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_BLANK_LINES_FILE, true, true, false);
            assertEquals(expected, "Files [" + DIFF1_FILE + " " + DIFF1_BLANK_LINES_FILE + "] are identical");
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffDirContainFilesWithSameContent() throws Exception {
        try {
            String expected = diffApp.diffTwoDir(DIFFDIR1, DIFFDIR1_IDENTICAL, false, false, false);
            assertEquals(expected, ""); // No message represents a successful diff
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffDirContainFilesWithSameContentUsingFlagS() {
        try {
            String expected = diffApp.diffTwoDir(DIFFDIR1, DIFFDIR1_IDENTICAL, true, false, false);
            assertEquals(expected, "Files [" + DIFFDIR1 + StringUtils.fileSeparator() + DIFF1_FILE + " " + DIFFDIR1_IDENTICAL + StringUtils.fileSeparator() + DIFF1_FILE + "] are identical" + STRING_NEWLINE +
              "Files [" + DIFFDIR1 + StringUtils.fileSeparator() + DIFF1_IDENTICAL_FILE + " " + DIFFDIR1_IDENTICAL + StringUtils.fileSeparator() + DIFF1_IDENTICAL_FILE + "] are identical");
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFilesWithDifferentContent() throws Exception {
        try {
            String expected = diffApp.diffTwoFiles(DIFF1_FILE, DIFF2_FILE, false, false, false);
            assertEquals(expected, "< test A" + STRING_NEWLINE +
              "< test B" + STRING_NEWLINE +
              "< test C" + STRING_NEWLINE +
              "> test D" + STRING_NEWLINE +
              "> test E" + STRING_NEWLINE +
              "> test F" + STRING_NEWLINE);
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithDifferentContent() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, false);
            assertEquals(expected, "< test A" + STRING_NEWLINE +
              "< test B" + STRING_NEWLINE +
              "< test C" + STRING_NEWLINE +
              "> test D" + STRING_NEWLINE +
              "> test E" + STRING_NEWLINE +
              "> test F" + STRING_NEWLINE);
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFilesWithDifferentContentUsingFlagQ() throws Exception {
        try {
            String expected = diffApp.diffTwoFiles(DIFF1_FILE, DIFF2_FILE, false, false, true);
            assertEquals(expected, "Files [" + DIFF1_FILE + " " + DIFF2_FILE + "] differ"); // NOPMD
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagQ() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, true);
            assertEquals(expected, "Files [" + DIFF1_FILE + " -] differ"); // NOPMD
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFilesWithDifferentContentUsingFlagBQ() {
        try {
            String expected = diffApp.diffTwoFiles(DIFF2_FILE, DIFF1_BLANK_LINES_FILE, false, true, true);
            assertEquals(expected, "Files [" + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + "] differ"); // NOPMD
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagBQ() throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_BLANK_LINES_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF2_FILE, inputStream, false, true, true);
            assertEquals(expected, "Files [" + DIFF2_FILE + " -] differ");
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffFilesWithDifferentContentUsingFlagSBQ() {
        try {
            String expected = diffApp.diffTwoFiles(DIFF2_FILE, DIFF1_BLANK_LINES_FILE, true, true, true);
            assertEquals(expected, "Files [" + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + "] differ");
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagSBQ() throws Exception {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_BLANK_LINES_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF2_FILE, inputStream, true, true, true);
            assertEquals(expected, "Files [" + DIFF2_FILE + " -] differ");
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffDirContainFilesWithDifferentContent() throws Exception {
        try {
            String expected = diffApp.diffTwoDir(DIFFDIR1, DIFFDIR2, false, false, false);
            assertEquals(expected, "Only in diffDir1: diff1.txt" + STRING_NEWLINE +
              "Only in diffDir1: diff1-identical.txt" + STRING_NEWLINE +
              "Only in diffDir2: diff2.txt");
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
    
    @Test
    public void testDiffDirContainFilesWithDifferentContentUsingFlagQ() {
        try {
            String expected = diffApp.diffTwoDir(DIFFDIR1, DIFFDIR2, false, false, true);
            assertEquals(expected, "Only in diffDir1: diff1.txt" + STRING_NEWLINE +
              "Only in diffDir1: diff1-identical.txt" + STRING_NEWLINE +
              "Only in diffDir2: diff2.txt");
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void testDiffIdenticalFilesWithRun() {
        String expectResult = "";
        String[] args = {DIFF1_FILE, DIFF1_IDENTICAL_FILE};
        assertDoesNotThrow(() -> {
            diffApp.run(args, System.in, stdout);
            assertEquals(expectResult, stdout.toString());
        });
    }
    
    @Test
    void testDiffDifferentFilesWithRun() {
        String expectResult = "< test A" + STRING_NEWLINE +
          "< test B" + STRING_NEWLINE +
          "< test C" + STRING_NEWLINE +
          "> test D" + STRING_NEWLINE +
          "> test E" + STRING_NEWLINE +
          "> test F" + STRING_NEWLINE + STRING_NEWLINE;
        String[] args = {DIFF1_FILE, DIFF2_FILE};
        assertDoesNotThrow(() -> {
            diffApp.run(args, System.in, stdout);
            assertEquals(expectResult, stdout.toString());
        });
    }
    
    @Test
    void testDiffDifferentDirectoriesWithRun() {
        String expectResult = "Only in diffDir1: diff1.txt" + STRING_NEWLINE +
          "Only in diffDir1: diff1-identical.txt" + STRING_NEWLINE +
          "Only in diffDir2: diff2.txt" + STRING_NEWLINE;
        String[] args = {DIFFDIR1, DIFFDIR2};
        assertDoesNotThrow(() -> {
            diffApp.run(args, System.in, stdout);
            assertEquals(expectResult, stdout.toString());
        });
    }
}
