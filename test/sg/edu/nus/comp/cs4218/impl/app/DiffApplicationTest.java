package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.DiffException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;

/**
 * Tests for diff command.
 *
 * Negative test cases:
 * - Invalid file
 * - Invalid directory
 * - Directory without files
 *
 * Positive test cases:
 * - No flag used + Files/stdin with same content
 * - "-s" flag used + Files/stdin with same content
 * - "-B" flag used + Files/stdin with same content
 * - "-sB" flags used + Files with same content
 * - No flag used + Directories containing Files with same content
 * - "-s" flag used + Directories containing Files with same content
 *
 * - No flag used + Files/stdin with different content
 * - "-q" flag used + Files/stdin with different content
 * - "-Bq" flags used + Files/stdin with different content
 * - "sBq" flags used + Files/stdin with different content
 * - No flag used + Directories containing Files with different content
 * - "-q" flag used + Directories containing Files with different content
 */
public class DiffApplicationTest {
    private static DiffApplication diffApp;
    private static final String originalDir = Environment.getCurrentDirectory();
    private static final String DIFF_TEST_DIR = originalDir + File.separator + "dummyTestFolder" + File.separator + "DiffTestFolder";
    private static OutputStream stdout;

    private static final String DIFF1_FILE = "diff1.txt";
    private static final String DIFF1_IDENTICAL_FILE = "diff1-identical.txt";
    private static final String DIFF1_BLANK_LINES_FILE = "diff1-blank-lines.txt";
    private static final String DIFF2_FILE = "diff2.txt";

    private static final String DIFFDIR1 = "diffDir1";
    private static final String DIFFDIR1_IDENTICAL = "diffDir1-identical";
    private static final String DIFFDIR2 = "diffDir1";

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
    }

    @Test
    public void testFailsWithInvalidFile() {
        // TODO: Instantiate diffApp when there is actual implementation
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoFiles("invalidFile.txt", "invalidFile.txt", false ,false, false));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void testFailsWithInvalidDir() {
        // TODO: Instantiate diffApp when there is actual implementation
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoFiles("invalidDir", "invalidDir", false ,false, false));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }

    @Test
    public void testFailsWithDirWithoutFiles() {
        // TODO: Instantiate diffApp when there is actual implementation
        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.diffTwoDir("dummyDir", "dummyDir", false ,false, false));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }

    @Test
    public void testDiffFilesWithSameContent() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_IDENTICAL_FILE, false, false, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithSameContent() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF1_FILE));
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithSameContentUsingFlagS() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_IDENTICAL_FILE, true, false, false);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " " + DIFF1_IDENTICAL_FILE + " are identical"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithSameContentUsingFlagS() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF1_FILE));
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, true, false, false);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " - are identical"));
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
        }
    }

    @Test
    public void testDiffFileAndStdinWithSameContentUsingFlagB() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF1_BLANK_LINES_FILE));
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, true, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithSameContentUsingFlagSB() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF1_BLANK_LINES_FILE, true, true, false);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " " + DIFF1_BLANK_LINES_FILE + " are identical"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffDirContainFilesWithSameContent() {
        try {
            diffApp.diffTwoDir(DIFFDIR1, DIFFDIR1_IDENTICAL, false, false, false);
            assertTrue(stdout.toString().contains("")); // No message represents a successful diff
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffDirContainFilesWithSameContentUsingFlagS() {
        try {
            diffApp.diffTwoDir(DIFFDIR1, DIFFDIR1_IDENTICAL, true, false, false);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " " + DIFF1_FILE + " are identical\n" +
                    "Files " + DIFF1_IDENTICAL_FILE + " " + DIFF1_IDENTICAL_FILE + " are identical"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithDifferentContent() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF2_FILE, false, false, false);
            assertTrue(stdout.toString().contains("< test A\n" +
                    "< test B\n" +
                    "< test C\n" +
                    "> test D\n" +
                    "> test E\n" +
                    "> test F"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithDifferentContent() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF2_FILE));
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, false);
            assertTrue(stdout.toString().contains("< test A\n" +
                    "< test B\n" +
                    "< test C\n" +
                    "> test D\n" +
                    "> test E\n" +
                    "> test F"));
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithDifferentContentUsingFlagQ() {
        try {
            diffApp.diffTwoFiles(DIFF1_FILE, DIFF2_FILE, false, false, true);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " " + DIFF2_FILE + " differ"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagQ() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF2_FILE));
            diffApp.diffFileAndStdin(DIFF1_FILE, inputStream, false, false, true);
            assertTrue(stdout.toString().contains("Files " + DIFF1_FILE + " " + DIFF2_FILE + " differ"));
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithDifferentContentUsingFlagBQ() {
        try {
            diffApp.diffTwoFiles(DIFF2_FILE, DIFF1_BLANK_LINES_FILE, false, true, true);
            assertTrue(stdout.toString().contains("Files " + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + " differ"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagBQ() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF1_BLANK_LINES_FILE));
            diffApp.diffFileAndStdin(DIFF2_FILE, inputStream, false, true, true);
            assertTrue(stdout.toString().contains("Files " + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + " differ"));
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFilesWithDifferentContentUsingFlagSBQ() {
        try {
            diffApp.diffTwoFiles(DIFF2_FILE, DIFF1_BLANK_LINES_FILE, true, true, true);
            assertTrue(stdout.toString().contains("Files " + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + " differ"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffFileAndStdinWithDifferentContentUsingFlagSBQ() throws DiffException {
        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + File.separator + DIFF1_BLANK_LINES_FILE));
            diffApp.diffFileAndStdin(DIFF2_FILE, inputStream, true, true, true);
            assertTrue(stdout.toString().contains("Files " + DIFF2_FILE + " " + DIFF1_BLANK_LINES_FILE + " differ"));
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffDirContainFilesWithDifferentContent() {
        try {
            diffApp.diffTwoDir(DIFFDIR1, DIFFDIR2, false, false, false);
            assertTrue(stdout.toString().contains("Only in diffDir1: diff1-identical.txt\n" +
                    "Only in diffDir1: diff1.txt\n" +
                    "Only in diffDir2: diff2.txt"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testDiffDirContainFilesWithDifferentContentUsingFlagQ() {
        try {
            diffApp.diffTwoDir(DIFFDIR1, DIFFDIR2, false, false, true);
            assertTrue(stdout.toString().contains("Only in diffDir1: diff1-identical.txt\n" +
                    "Only in diffDir1: diff1.txt\n" +
                    "Only in diffDir2: diff2.txt"));
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
}
