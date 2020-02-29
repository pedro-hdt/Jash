package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Tests for sort command.
 *
 * Negative test cases:
 *  - Null output stream
 *  - File not found
 *  - Is directory
 *  - Empty stdin
 *
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
public class SortApplicationTest {
    private static SortApplication sortApp;
    private static OutputStream stdout;

    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "SortTestFolder");
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
        Environment.setCurrentDirectory(ORIGINAL_DIR);
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
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(new String[] { "dummyDir" }, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_IS_DIR));
    }

    @Test
    public void testFailsWithEmptyStdin() {
        Exception expectedException = assertThrows(SortException.class, () -> sortApp.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }

    @Test
    public void testNFlagNumberSort() {
        String[] args = new String[] { "-n", "numbersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("1\n" +
                    "3\n" +
                    "4\n" +
                    "5\n" +
                    "6"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFlagCharSort() {
        String[] args = new String[] { "-n", "charactersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("!\n" +
                    "#\n" +
                    "$\n" +
                    "%\n" +
                    "&\n" +
                    "@\n" +
                    "^"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFlagLettersSort() {
        String[] args = new String[] { "-n", "lettersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("A\n" +
                    "C\n" +
                    "E\n" +
                    "G\n" +
                    "b\n" +
                    "d\n" +
                    "f"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFlagMixedSort() {
        String[] args = new String[] { "-n", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#\n" +
                    "$\n" +
                    "&\n" +
                    "*\n" +
                    "1\n" +
                    "4\n" +
                    "6\n" +
                    "8\n" +
                    "A\n" +
                    "C\n" +
                    "E\n" +
                    "G\n" +
                    "b\n" +
                    "d\n" +
                    "f"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagNumberSort() {
        String[] args = new String[] { "-r", "numbersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("6\n" +
                    "5\n" +
                    "4\n" +
                    "3\n" +
                    "1"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagCharSort() {
        String[] args = new String[] { "-r", "charactersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("^\n" +
                    "@\n" +
                    "&\n" +
                    "%\n" +
                    "$\n" +
                    "#\n" +
                    "!"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagLettersSort() {
        String[] args = new String[] { "-r", "lettersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f\n" +
                    "d\n" +
                    "b\n" +
                    "G\n" +
                    "E\n" +
                    "C\n" +
                    "A"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFlagMixedSort() {
        String[] args = new String[] { "-r", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f\n" +
                    "d\n" +
                    "b\n" +
                    "G\n" +
                    "E\n" +
                    "C\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "4\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testFFlagLettersSort() {
        String[] args = new String[] { "-f", "lettersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("A\n" +
                    "b\n" +
                    "C\n" +
                    "d\n" +
                    "E\n" +
                    "f\n" +
                    "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testFFlagMixedSort() {
        String[] args = new String[] { "-f", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#\n" +
                    "$\n" +
                    "&\n" +
                    "*\n" +
                    "1\n" +
                    "4\n" +
                    "6\n" +
                    "8\n" +
                    "A\n" +
                    "b\n" +
                    "C\n" +
                    "d\n" +
                    "E\n" +
                    "f\n" +
                    "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNRFlagsMixedSort() {
        String[] args = new String[] { "-nr", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("f\n" +
                    "d\n" +
                    "b\n" +
                    "G\n" +
                    "E\n" +
                    "C\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "4\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNFFlagsMixedSort() {
        String[] args = new String[] { "-nf", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("#\n" +
                    "$\n" +
                    "&\n" +
                    "*\n" +
                    "1\n" +
                    "4\n" +
                    "6\n" +
                    "8\n" +
                    "A\n" +
                    "b\n" +
                    "C\n" +
                    "d\n" +
                    "E\n" +
                    "f\n" +
                    "G"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testRFFlagsMixedSort() {
        String[] args = new String[] { "-rf", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G\n" +
                    "f\n" +
                    "E\n" +
                    "d\n" +
                    "C\n" +
                    "b\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "4\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testNRFFlagsMixedSort() {
        String[] args = new String[] { "-nrf", "mixed.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G\n" +
                    "f\n" +
                    "E\n" +
                    "d\n" +
                    "C\n" +
                    "b\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "4\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testMoreThanOneFileSort() {
        String[] args = new String[] { "-nrf", "mixed.txt", "numbersOnly.txt" };

        try {
            sortApp.run(args, System.in, stdout);
            assertTrue(stdout.toString().contains("G\n" +
                    "f\n" +
                    "E\n" +
                    "d\n" +
                    "C\n" +
                    "b\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "6\n" +
                    "5\n" +
                    "4\n" +
                    "4\n" +
                    "3\n" +
                    "1\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testStdinSort() {
        String stdInString = "&\n" +
                "*\n" +
                "$\n" +
                "#\n" +
                "A\n" +
                "b\n" +
                "C\n" +
                "d\n" +
                "E\n" +
                "f\n" +
                "G\n" +
                "4\n" +
                "6\n" +
                "8\n" +
                "1";

        InputStream stdin = new ByteArrayInputStream(stdInString.getBytes());
        String[] args = new String[] { "-nrf" };

        try {
            sortApp.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("G\n" +
                    "f\n" +
                    "E\n" +
                    "d\n" +
                    "C\n" +
                    "b\n" +
                    "A\n" +
                    "8\n" +
                    "6\n" +
                    "4\n" +
                    "1\n" +
                    "*\n" +
                    "&\n" +
                    "$\n" +
                    "#"));
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
}
