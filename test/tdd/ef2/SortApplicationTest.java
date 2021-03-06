package tdd.ef2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;
import tdd.util.TestUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

/**
 * Tests for sort command.
 * <p>
 * Negative test cases:
 * - Null output stream
 * - File not found
 * - Is directory
 * - Empty stdin
 * <p>
 * Positive test cases:
 * - "-n" flag used
 * - "-r" flag used
 * - "-f" flag used
 * - "-nr" flags used
 * - "-nf" flags used
 * - "-rf" flags used
 * - "-nrf" flags used
 * - >1 files supplied
 * - No files supplied, use stdin
 */
@SuppressWarnings("PMD")
public class SortApplicationTest {
    private static SortApplication sortApp;
    private static OutputStream stdout;

    private static final String ORIGINAL_DIR = Environment.currentDirectory;

    @BeforeAll
    static void setupAll() {
        String path = ORIGINAL_DIR
          + StringUtils.fileSeparator() + "test"
          + StringUtils.fileSeparator() + "tdd"
          + StringUtils.fileSeparator() + "util"
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "SortTestFolder";
        if (Files.isDirectory(TestUtil.resolveFilePath(path))) {
            Environment.currentDirectory = TestUtil.resolveFilePath(path).toString();
        }
    }

    @BeforeEach
    void setUp() {
        sortApp = new SortApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    @AfterAll
    static void reset() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    @Test
    public void testFailsWithNullOutputstream() {
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(null, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }

    @Test
    public void testFailsWithInvalidFile() {
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(new String[]{"invalidFile.txt"}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void testFailsWithDirectory() {
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(new String[]{"dummyDir"}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }

    @Test
    public void testFailsWithEmptyStdin() {
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }


    @Test
    public void testNFlagNumberSort() {
        String[] args = new String[]{"-n", "numbersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("1" + StringUtils.STRING_NEWLINE +
              "3" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "5" + StringUtils.STRING_NEWLINE +
              "6"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }

    @Test
    public void testNFlagCharSort() {
        String[] args = new String[]{"-n", "charactersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("!" + StringUtils.STRING_NEWLINE +
              "#" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "%" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "@" + StringUtils.STRING_NEWLINE +
              "^"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFlagLettersSort() {
        String[] args = new String[]{"-n", "lettersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("A" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "G" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "f"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFlagMixedSort() {
        String[] args = new String[]{"-n", "mixed.txt"}; // NOPMD

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "G" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "f"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagNumberSort() {
        String[] args = new String[]{"-r", "numbersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("6" + StringUtils.STRING_NEWLINE +
              "5" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "3" + StringUtils.STRING_NEWLINE +
              "1"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagCharSort() {
        String[] args = new String[]{"-r", "charactersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("^" + StringUtils.STRING_NEWLINE +
              "@" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "%" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#" + StringUtils.STRING_NEWLINE +
              "!"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagLettersSort() {
        String[] args = new String[]{"-r", "lettersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "G" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "A"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagMixedSort() {
        String[] args = new String[]{"-r", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "G" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testFFlagLettersSort() {
        String[] args = new String[]{"-f", "lettersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("A" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testFFlagMixedSort() {
        String[] args = new String[]{"-f", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNRFlagsMixedSort() {
        String[] args = new String[]{"-nr", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "G" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFFlagsMixedSort() {
        String[] args = new String[]{"-nf", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFFlagsMixedSort() {
        String[] args = new String[]{"-rf", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNRFFlagsMixedSort() {
        String[] args = new String[]{"-nrf", "mixed.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testMoreThanOneFileSort() {
        String[] args = new String[]{"-nrf", "mixed.txt", "numbersOnly.txt"};

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "5" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "3" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testStdinSort() {
        String stdInString = "&" + StringUtils.STRING_NEWLINE +
          "*" + StringUtils.STRING_NEWLINE +
          "$" + StringUtils.STRING_NEWLINE +
          "#" + StringUtils.STRING_NEWLINE +
          "A" + StringUtils.STRING_NEWLINE +
          "b" + StringUtils.STRING_NEWLINE +
          "C" + StringUtils.STRING_NEWLINE +
          "d" + StringUtils.STRING_NEWLINE +
          "E" + StringUtils.STRING_NEWLINE +
          "f" + StringUtils.STRING_NEWLINE +
          "G" + StringUtils.STRING_NEWLINE +
          "4" + StringUtils.STRING_NEWLINE +
          "6" + StringUtils.STRING_NEWLINE +
          "8" + StringUtils.STRING_NEWLINE +
          "1";

        InputStream stdin = new ByteArrayInputStream(stdInString.getBytes());
        String[] args = new String[]{"-nrf"};

        try {
            sortApp.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("G" + StringUtils.STRING_NEWLINE +
              "f" + StringUtils.STRING_NEWLINE +
              "E" + StringUtils.STRING_NEWLINE +
              "d" + StringUtils.STRING_NEWLINE +
              "C" + StringUtils.STRING_NEWLINE +
              "b" + StringUtils.STRING_NEWLINE +
              "A" + StringUtils.STRING_NEWLINE +
              "8" + StringUtils.STRING_NEWLINE +
              "6" + StringUtils.STRING_NEWLINE +
              "4" + StringUtils.STRING_NEWLINE +
              "1" + StringUtils.STRING_NEWLINE +
              "*" + StringUtils.STRING_NEWLINE +
              "&" + StringUtils.STRING_NEWLINE +
              "$" + StringUtils.STRING_NEWLINE +
              "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
}
