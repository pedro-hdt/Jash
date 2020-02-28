package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Tests for wc command.
 *
 * Negative test cases:
 * - Null output stream
 * - Empty stdin
 *
 * Positive test cases:
 * - No flags used with file/stdin
 * - "-c" flag used with file/stdin
 * - "-l" flag used with file/stdin
 * - "-w" flag used with file/stdin
 * - "cl" flags used with file/stdin
 * - "lw" flags used with file/stdin
 * - "clw" flags used with file/stdin
 * - No flags used with multiple files
 * - "clw" flags used with multiple files
 */
public class WcApplicationTest {
    private static WcApplication wcApp;
    private static OutputStream stdout;

    private static final String originalDir = Environment.getCurrentDirectory();
    private static final String WC_TEST_DIR = originalDir + File.separator + "dummyTestFolder" + File.separator + "WcTestFolder";
    private static final String WC1_FILE = "wc1.txt";
    private static final String WC2_FILE = "wc2.txt";

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(WC_TEST_DIR);
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(originalDir);
    }

    @BeforeEach
    void setUp() {
        wcApp = new WcApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    @Test
    public void testFailsWithNullOutputstream() {
        Exception expectedException = assertThrows(WcException.class, () -> wcApp.run(null, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));
    }

    @Test
    public void testFailsWithEmptyStdin() {
        Exception expectedException = assertThrows(WcException.class, () -> wcApp.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_GENERAL));
    }

    @Test
    public void testWcOfFile() {
        String[] args = new String[] { WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1       2      14 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdin() {
        try {
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(null, inputStream, stdout);
            assertEquals("       1       2      14\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagC() {
        String[] args = new String[] { "-c", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("      14 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagC() {
        try {
            String[] args = new String[] { "-c" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("      14\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagL() {
        String[] args = new String[] { "-l", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagL() {
        try {
            String[] args = new String[] { "-l" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("       1\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagW() {
        String[] args = new String[] { "-w", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       2 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagW() {
        try {
            String[] args = new String[] { "-w" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("       2\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagCL() {
        String[] args = new String[] { "-cl", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1      14 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagCL() {
        try {
            String[] args = new String[] { "-cl" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("       1      14\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagLW() {
        String[] args = new String[] { "-lw", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1       2 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagLW() {
        try {
            String[] args = new String[] { "-lw" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("       1       2\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfFileUsingFlagCLW() {
        String[] args = new String[] { "-clw", WC1_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1       2      14 wc1.txt\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfStdinUsingFlagCLW() {
        try {
            String[] args = new String[] { "-clw" };
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + File.separator + WC1_FILE));
            wcApp.run(args, inputStream, stdout);
            assertEquals("       1       2      14\n", stdout.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfMultipleFiles() {
        String[] args = new String[] { WC1_FILE, WC2_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1       2      14 wc1.txt\n" +
                    "       2       5      41 wc2.txt\n" +
                    "       3       7      55 total\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    @Test
    public void testWcOfMultipleFilesUsingFlagCLW() {
        String[] args = new String[] { "-clw", WC1_FILE, WC2_FILE };
        try {
            wcApp.run(args, System.in, stdout);
            assertEquals("       1       2      14 wc1.txt\n" +
                    "       2       5      41 wc2.txt\n" +
                    "       3       7      55 total\n", stdout.toString());
        } catch (WcException e) {
            fail("should not fail: " + e.getMessage());
        }
    }
}
