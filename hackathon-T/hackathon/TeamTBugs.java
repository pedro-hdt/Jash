import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.app.*;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;


import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class TeamTBugs {

    ShellImpl shell = new ShellImpl();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayInputStream input;
    static String ORIGINAL_DIR;

    @BeforeAll
    static void setupAll() {
        ORIGINAL_DIR = Environment.currentDirectory;
    }

    @AfterAll
    static void reset() {
    }

    @BeforeEach
    public void setup() {
        shell = new ShellImpl();
        output = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        output.reset();
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    /**
     * Spaces around the code which should be ignored and command evaluated
     * lots of spaces to ignore after tokenizing but handle quotes
     * Command:       echo     ''   hi   ' '
     */
    @Test()
    @DisplayName("Bug #1")
    public void testTokeniseWithQuote() {
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(3), () -> shell.parseAndEvaluate("      echo     ''   hi   ' '     ", output));
            assertEquals(" hi  " + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Double backquote nesting results in wrong execution of commands in CommandSubs
     * For multiple command subs in same command, the combination should be as expected
     * Command: echo abc `echo 1 2 3`xyz`echo 4 5 6`
     */
    @Test
    @DisplayName("Bug #2")
    public void testNestedQuotesMultiple() {
        try {

            shell.parseAndEvaluate("echo abc `echo 1 2 3`xyz`echo 4 5 6`", output);
            assertEquals("abc 1 2 3xyz4 5 testTokeniseWithQuote6" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * If `ls *.txt` returns two files, the paste result should contain both files but here it’s only one first files
     * Fault: Command Subs doesn’t return the result as list of tokenized arguments for the outer command
     * Command: paste `ls x*.txt`
     */
    @Test
    @DisplayName("Bug #3")
    public void testPasteWithLs() {
        try {
            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "CommandSubsFolder";

            shell.parseAndEvaluate("paste `ls x*.txt`", output);
            assertEquals("hi\tboy" + StringUtils.STRING_NEWLINE +
                    "hello\tgirl" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Double quotes disable interpretation of special symbols except backquote
     * Should print a space before the period.
     * Fault: CommandSubs within Double quotes trims spaces
     * Command: echo "This is space:`echo " "`."
     */
    @Test
    @DisplayName("Bug #4")
    public void testInterpretationOfSpace() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is space:`echo \" \"`.\"", output);

        assertEquals("This is space: .", output.toString());
    }

    /**
     * Globbing which returns multiple files
     * Ls should not return empty line between files
     *
     * Unnecessary extra lines between ls output of files
     * Command: ls *.txt
     */
    @Test
    @DisplayName("Bug #5")
    public void testGlobbingWithMultipleMatches() throws AbstractApplicationException, ShellException {

        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "GlobbingTest";

        shell.parseAndEvaluate("ls *.txt", output);

        assertEquals("f1.txt" + StringUtils.STRING_NEWLINE + "match1.txt" + StringUtils.STRING_NEWLINE
                + "mvFile.txt" + StringUtils.STRING_NEWLINE + "test.txt" + StringUtils.STRING_NEWLINE, output.toString());
    }

    /**
     * Irrelevant trimming of actual output when command subs used
     * Trims wc content when integrated with command subs
     *
     * CommandSubs has irrelevant trimming
     * Command: echo "`wc -c hella.txt` - `wc -c hellz.txt`"
     */
    @Test
    @DisplayName("Bug #6")
    public void testEchoWithWc() {
        try {

            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "CommandSubsFolder";

            shell.parseAndEvaluate("echo \"`wc -c hella.txt` - `wc -c hellz.txt`\"", output);
            assertEquals("       5 hella.txt -        6 hellz.txt" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail();
        }
    }


    /**
     * ExitCommand untestable due to wrong implementation
     * Ends test suite process
     * Command: exit
     */
    @Test
    @DisplayName("Bug #7")
    public void testExitUntestable() {
        try {

            shell.parseAndEvaluate("exit", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Wc command should not recognize stdin as a null argument
     */
    @Test
    @DisplayName("Bug #8")
    public void testWcOfStdin() {
        try {
            String WC_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "WcTestFolder";
            String WC1_FILE = "wc1.txt";
            InputStream inputStream = new FileInputStream(new File(WC_TEST_DIR + StringUtils.fileSeparator() + WC1_FILE)); //NOPMD
            WcApplication wcApp = new WcApplication();
            wcApp.run(null, inputStream, output);
            assertEquals("       1       2      14" + StringUtils.STRING_NEWLINE, output.toString()); // filename is empty for standard input
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required for some flags
     */
    @Test
    @DisplayName("Bug #9.1")
    public void testNRFFlagsMixedSort() {
        String SORT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "SortTestFolder";
        String[] args = new String[]{"-nrf", SORT_TEST_DIR + StringUtils.fileSeparator() + "mixed.txt"};
        SortApplication sortApp = new SortApplication();

        try {
            sortApp.run(args, System.in, output);
            assertEquals( "G" + StringUtils.STRING_NEWLINE +
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
                    "#", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required for some flags
     */
    @Test
    @DisplayName("Bug #9.2")
    public void testNFlagMixedSort() {
        String SORT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "SortTestFolder";
        String[] args = new String[]{"-n", SORT_TEST_DIR + StringUtils.fileSeparator() + "mixed.txt"};
        SortApplication sortApp = new SortApplication();

        try {
            sortApp.run(args, System.in, output);
            assertEquals("#" + StringUtils.STRING_NEWLINE +
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
                    "f", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required for some flags
     */
    @Test
    @DisplayName("Bug #9.3")
    public void testNFFlagsMixedSort() {
        String SORT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "SortTestFolder";
        String[] args = new String[]{"-nf", SORT_TEST_DIR + StringUtils.fileSeparator() + "mixed.txt"};
        SortApplication sortApp = new SortApplication();

        try {
            sortApp.run(args, System.in, output);
            assertEquals("#" + StringUtils.STRING_NEWLINE +
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
                    "G", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required for some flags
     */
    @Test
    @DisplayName("Bug #9.4")
    public void testNRFlagsMixedSort() {
        String SORT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "SortTestFolder";
        String[] args = new String[]{"-nr", SORT_TEST_DIR + StringUtils.fileSeparator() + "mixed.txt"};
        SortApplication sortApp = new SortApplication();

        try {
            sortApp.run(args, System.in, output);
            assertEquals("f" + StringUtils.STRING_NEWLINE +
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
                    "#", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required when stdin is read with some flags
     */
    @Test
    @DisplayName("Bug #9.5")
    public void testStdinSort() {
        SortApplication sortApp = new SortApplication();
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
            sortApp.run(args, stdin, output);
            assertEquals("G" + StringUtils.STRING_NEWLINE +
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
                    "#", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Sort command implementation output differs from what is required when > 1 file is read with some flags
     */
    @Test
    @DisplayName("Bug #9.6")
    public void testMoreThanOneFileSort() {
        String SORT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "SortTestFolder";
        SortApplication sortApp = new SortApplication();
        String[] args = new String[]{"-nrf", SORT_TEST_DIR + StringUtils.fileSeparator() + "mixed.txt", SORT_TEST_DIR + StringUtils.fileSeparator() + "numbersOnly.txt"};

        try {
            sortApp.run(args, System.in, output);
            assertEquals("G" + StringUtils.STRING_NEWLINE +
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
                    "#", output.toString());
        } catch (SortException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command show different output format than what is expected
     */
    @Test
    @DisplayName("Bug #10.1")
    public void testDiffFilesWithDifferentContentUsingFlagQ() {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";
        String DIFF2_FILE = "diff2.txt";

        try {
            String actual = diffApp.diffTwoFiles(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE, false, false, true);
            assertEquals("Files [" + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE + " " + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE + "] differ", actual); // NOPMD
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command show different output format than what is expected
     */
    @Test
    @DisplayName("Bug #10.2")
    public void testDiffFileAndStdinWithSameContentUsingFlagS() throws FileNotFoundException {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";
        InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE)); //NOPMD

        try {
            String actual = diffApp.diffFileAndStdin(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, inputStream, true, false, true);
            assertEquals("Files [" + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE + " -] are identical", actual);
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command show different output format than what is expected
     */
    @Test
    @DisplayName("Bug #10.3")
    public void testDiffFilesWithSameContentUsingFlagSB() {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";
        String DIFF1_BLANK_LINES_FILE = "diff1-blank-lines.txt";

        try {
            String actual = diffApp.diffTwoFiles(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_BLANK_LINES_FILE, true, true, true);
            assertEquals("Files [" + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE + " " + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_BLANK_LINES_FILE + "] are identical", actual); // NOPMD
        } catch (DiffException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command has implementation error that differ from expectations
     */
    @Test
    @DisplayName("Bug #10.4")
    public void testDiffFileAndStdinWithDifferentContent() throws DiffException {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";
        String DIFF2_FILE = "diff2.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE)); //NOPMD
            String actual = diffApp.diffFileAndStdin(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, inputStream, false, false, false);
            assertEquals("< test A" + StringUtils.STRING_NEWLINE +
                    "< test B" + StringUtils.STRING_NEWLINE +
                    "< test C" + StringUtils.STRING_NEWLINE +
                    "> test D" + StringUtils.STRING_NEWLINE +
                    "> test E" + StringUtils.STRING_NEWLINE +
                    "> test F" + StringUtils.STRING_NEWLINE, actual);
        } catch (IOException e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Cut command output has an extra carriage return
     */
    @Test
    @DisplayName("Bug #11.1")
    public void testCutOnStdinWithSingleNumberUsingFlagB() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String[] args = new String[]{"-b", "5", "-"};
        String CUT1_FILE = "cut1.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, output);
            assertEquals("y" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Cut command output has an extra carriage return
     */
    @Test
    @DisplayName("Bug #11.2")
    public void testCutOnStdinWithSingleNumberUsingFlagC() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String[] args = new String[]{"-c", "5", "-"};
        String CUT1_FILE = "cut1.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, output);
            assertEquals("y" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage()); // NOPMD
        }
    }

    /**
     * Cut command output has an extra carriage return
     */
    @Test
    @DisplayName("Bug #11.3")
    public void testCutOnStdinWithCommaSeparatedNumbersUsingFlagB() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String[] args = new String[]{"-b", "5,10", "-"};
        String CUT1_FILE = "cut1.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, output);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Cut command output has an extra carriage return
     */
    @Test
    @DisplayName("Bug #11.4")
    public void testCutOnStdinWithCommaSeparatedNumbersUsingFlagC() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String[] args = new String[]{"-c", "5,10", "-"};
        String CUT1_FILE = "cut1.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE)); //NOPMD
            cutApp.run(args, inputStream, output);
            assertEquals("yT" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }


    /**
     * cp copies a file to the same directory it is in, overwriting itself unnecessarily
     * Should throw an exception reporting src and dest are the same file like GNU cp
     */
    @Test
    @DisplayName("Bug #12.1")
    public void testCpFailsSingleFileToSameDir() {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "CpTestFolder";

        String[] args = {"src1", Environment.currentDirectory};
        CpApplication cpApp = new CpApplication();
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));

        // In UNIX cp prints "<FILE> and <FILE> are the same file" so we assume this replicates such behavior
        assertTrue(cpException.getMessage().contains("same file"));

    }


    /**
     * cp copies file into itself with similar behavior
     * Should throw an exception reporting src and dest are the same file like GNU cp
     */
    @Test
    @DisplayName("Bug #12.2")
    public void testCpFailsSingleFileToItself() {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "CpTestFolder";

        String[] args = {"src1", "src1"};
        CpApplication cpApp = new CpApplication();
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));

        // In UNIX cp prints "<FILE> and <FILE> are the same file" so we assume this replicates such behavior
        assertTrue(cpException.getMessage().contains("same file"));

    }


    /**
     * cp with multiple files ignores all following files when there is an error with one of them
     * The expected behavior would be for that error to be reported and the remaining files to be copied correctly
     * This test assumes the error is reported through an exception and not to stdout
     */
    @Test
    @DisplayName("Bug #13")
    public void testCpMultipleFilesOneFails() throws IOException {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "CpTestFolder";

        Path src1 = IOUtils.resolveFilePath("src1");
        Path nonexistent = IOUtils.resolveFilePath("nonexistent");
        Path src2 = IOUtils.resolveFilePath("src2");
        Path destDir = IOUtils.resolveFilePath("destDir");

        // delete any leftover files from previous failes test runs
        Files.deleteIfExists(destDir.resolve(src1));
        Files.deleteIfExists(destDir.resolve(src2));

        String[] args = {src1.toString(), nonexistent.toString(), src2.toString(), destDir.toString()};
        CpApplication cpApp = new CpApplication();

        CpException cpException = assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));

        // ensure the right error is reported
        assertTrue(cpException.getMessage().contains(ERR_FILE_NOT_FOUND));

        // ensure initial files still exist
        assertTrue(Files.exists(src1));
        assertTrue(Files.exists(src2));

        // ensure the nonexistent file was not created
        assertFalse(Files.exists(nonexistent));

        // ensure existing files were correctly copied
        assertTrue(Files.exists(destDir.resolve(src1)));
        assertTrue(Files.exists(destDir.resolve(src2)));

        // clean up
        Files.deleteIfExists(destDir.resolve(src1));
        Files.deleteIfExists(destDir.resolve(src2));

    }

    /**
     * paste with a null output stream reports 'No InputStream and no filenames'
     */
    @Test
    @DisplayName("Bug #14")
    public void testPasteNullOutputStream() {

        PasteApplication pasteApp = new PasteApplication();

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[0], System.in, null));

        assertTrue(exception.getMessage().contains(ERR_NO_OSTREAM));

    }

    /**
     * paste with a directory as the only argument reports 'No InputStream and no filenames'
     * Instead it should report 'This is a directory' (ERR_IS_DIR)
     */
    @Test
    @DisplayName("Bug #15")
    public void testPasteDirOnlyArg() throws IOException {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "PasteTestFolder";

        Path dir = Files.createDirectory(IOUtils.resolveFilePath("dir")); //NOPMD

        PasteApplication pasteApp = new PasteApplication();

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"dir"}, System.in, System.out));

        Files.delete(dir);
        assertTrue(exception.getMessage().contains(ERR_IS_DIR));

    }

    /**
     * Paste with nonexistent file reports 'No InputStream and no filenames'
     * Instead it should report 'No such file or directory' (ERR_FILE_NOT_FOUND)
     */
    @Test
    @DisplayName("Bug #16")
    public void testPasteNonexistentFile() {

        PasteApplication pasteApp = new PasteApplication();

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"fakefile"}, System.in, System.out));

        assertTrue(exception.getMessage().contains(ERR_FILE_NOT_FOUND));

    }

    /**
     * ‘paste - -’ causes input to stdin to be printed twice such that if the user inputs
     * ‘a’ on a line and ‘b’ on the following line then terminates the command, the output generated is:
     * a\ta\nb\tb\n
     * instead of:
     * a\tb\n
     */
    @Test
    @DisplayName("Bug #17")
    public void testPasteTwoStdinArgs() throws IOException, PasteException {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "PasteTestFolder";

        input = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath("pasteFile1.txt")));

        PasteApplication pasteApp = new PasteApplication();

        pasteApp.run(new String[]{"-", "-"}, input, output);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath("pasteFile1-2cols.txt")))
            + STRING_NEWLINE,
          output.toString());

    }

    /**
     * rm with -r flag will not remove empty directories. Displays 'Permission denied'
     * Should remove folder normally
     */
    @Test
    @DisplayName("Bug #18")
    public void testRmEmptyFolderRecursive() throws IOException, RmException {

        // create a temporary directory
        Files.deleteIfExists(IOUtils.resolveFilePath("CS4218-rmTest"));
        Path testDir = Files.createDirectory(IOUtils.resolveFilePath("CS4218-rmTest"));

        // make sure the right permissions are given
        testDir.getParent().toFile().setExecutable(true, true);
        testDir.getParent().toFile().setWritable(true, true);

        RmApplication rmApp = new RmApplication();

        // assemble args and call rm to delete the directory recursively
        String[] args = {"-r", testDir.toString()};
        rmApp.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testDir));

    }

    /**
     * ‘rm -rd .’ removes everything in the current directory.
     * This should never be allowed (GNU rm does not allow removing ‘.’ or `..`
     */
    @Test
    @DisplayName("Bug #19")
    @Disabled("RUN WITH CAUTION: may delete everything in curr dir")
    public void testRmFailsDirEndInDot() {

        RmApplication rmApp = new RmApplication();

        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(new String[]{"-rd", "."}, System.in, System.out);
        });
        assertTrue(exception.getMessage().contains("'.' or '..'")); // verify the correct exceptions is thrown

    }

    /**
     * Extra unexpected output when running wc command then cut command
     */
    @Test
    @DisplayName("Bug #20")
    public void testWcThenCut() throws ShellException, AbstractApplicationException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "WcIntegrationFolder");

        String expected = "       2 wctest.txt" + STRING_NEWLINE +
                "1" + STRING_NEWLINE;
        String cmdline = "wc -l wctest.txt; cut -b 2 wctest.txt";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Unexpected output when running sort command and then piping output to cut command
     */
    @Test
    @DisplayName("Bug #21")
    public void testSortThenCut() throws ShellException, AbstractApplicationException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "SortIntegrationFolder");

        String expected = "G" + STRING_NEWLINE;
        String cmdline = "sort -nrf sorttest.txt | cut -b 1 -";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }


    /**
     * Unexpected amount of carriage returns when running sort command and then piping output to cut command
     */
    @Test
    @DisplayName("Bug #22")
    public void testSortThenCutNegative() throws ShellException, AbstractApplicationException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "SortIntegrationFolder");

        String expected = "" + STRING_NEWLINE;
        String cmdline = "sort -nrf sorttest.txt | cut -b 2 -";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Wrong order of sort command output
     */
    @Test
    @DisplayName("Bug #23")
    public void testSortThenFindNegative() throws ShellException, AbstractApplicationException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "SortIntegrationFolder");

        String expected = "G" + STRING_NEWLINE +
                "f" + STRING_NEWLINE +
                "E" + STRING_NEWLINE +
                "d" + STRING_NEWLINE +
                "C" + STRING_NEWLINE +
                "b" + STRING_NEWLINE +
                "A" + STRING_NEWLINE +
                "8" + STRING_NEWLINE +
                "6" + STRING_NEWLINE +
                "4" + STRING_NEWLINE +
                "1" + STRING_NEWLINE +
                "*" + STRING_NEWLINE +
                "&" + STRING_NEWLINE +
                "$" + STRING_NEWLINE +
                "#" + STRING_NEWLINE;
        String cmdline = "sort -nrf sorttest.txt; find ../ -name wcteste.txt";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Stdin should be printed as ‘-’
     */
    @Test
    @DisplayName("Bug #24.1")
    public void testEchoThenDiff() throws Exception {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "DiffIntegrationFolder");

        String expected = "Files [- difftest.txt] differ" + STRING_NEWLINE;
        String argument = "echo difftestdifftest | diff -q - difftest.txt";
        shell.parseAndEvaluate(argument, output);

        assertEquals(expected, output.toString());
    }

    /**
     * Stdin should be printed as ‘-’
     */
    @Test
    @DisplayName("Bug #24.2")
    public void testEchoThenDiff2() throws Exception {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "DiffIntegrationFolder");

        String expected = "Files [- difftest.txt] are identical" + STRING_NEWLINE;
        String argument = "echo 'difftest!!!!!!!' | diff -s - difftest.txt";
        shell.parseAndEvaluate(argument, output);

        assertEquals(expected, output.toString());
    }

    /**
     * Stdin should be printed as ‘-’
     */
    @Test
    @DisplayName("Bug #24.3")
    public void testPasteThenDiff2() throws Exception {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "DiffIntegrationFolder");

        String cmdline = "paste difftest.txt | diff -s difftest.txt - ";
        String expected = "Files [difftest.txt -] are identical" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Extra carriage return in diff command
     */
    @Test
    @DisplayName("Bug #25")
    public void testPasteThenDiff() throws Exception {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "DiffIntegrationFolder");

        String cmdline = "paste difftest.txt | diff difftest.txt - ";
        String expected = "";
        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * SubCommands with nested substitution with same commands
     * Multiple command Subs produces extra empty line at the end
     */
    @Test
    @DisplayName("Bug #26")
    public void testSameCommandSubsNested() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo `echo `echo hello ``", output);

        assertEquals("echo hello", output.toString());
    }


    /**
     * StringUtils.isBlank() For loop condition is always true defaulting to infinite loop if not an empty string
     * Bug found using test generated from automated testing tool EvoSuite
     * Part of initial injected bugs: missing loop increment
     */
    @Test
    @DisplayName("Bug #27")
    public void nonEmptyInputToStringUtils() {
        boolean boolean0 = assertTimeoutPreemptively(Duration.ofSeconds(3), () -> StringUtils.isBlank("\n"));
        assertTrue(boolean0);
    }

    /**
     * Throws error that file doesn't exist when it should be creating the directory needed for files position
     * Create a file in non existent directory 2 level down and write into it
     */
    @Test
    @DisplayName("Bug #28")
    public void testCreateDirectoryThroughOutputstreamForFile() {
        try {
            shell.parseAndEvaluate("echo yo > dir1/dir2/out.txt", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Ls Integration with Diff gives no result and ununderstable error messsage when there shouldnt be one
     * The tokenisation of sub command is done wrong making outer diff to produce wrong result
     */
    @Test
    @DisplayName("Bug #29")
    public void testLsWithDiffCommandSubs() {
        try {

            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "LsIntegrationFolder";

            shell.parseAndEvaluate("diff `ls diff*.txt`", output);
            assertEquals("< azz" + StringUtils.STRING_NEWLINE +
                    "< ccc" + StringUtils.STRING_NEWLINE +
                    "> zza" + StringUtils.STRING_NEWLINE +
                    "> ccd" + StringUtils.STRING_NEWLINE + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Wrong tokenisation of result of command subs.
     * Only first word retained and everything else removed and not passed into echo
     */
    @Test
    @DisplayName("Bug #30")
    public void testEchoWithDiffCommandSubs() {
        try {
            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "CommandSubsFolder";

            shell.parseAndEvaluate("echo `diff hella.txt hellz.txt`", output);
            assertEquals("< first > second" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Command subs deletes first word of commandResult and only passed the rest
     * Second should also be displayed
     *
     * Filters file names present in findInFile.txt and sorts those files. The first output hellz.txt is ignored from result of grep
     */
    @Test
    @DisplayName("Bug #31")
    public void testGrepWithSort() {
        try {

            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "CommandSubsFolder";

            shell.parseAndEvaluate("sort `grep hell findInFile.txt`", output);
            assertEquals("first" + StringUtils.STRING_NEWLINE + "second" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Extra empty lines being outputted by cut on a single line to be cut passed through echo
     */
    @Test
    @DisplayName("Bug #32")
    public void testPipeWithEchoAndCut() {
        try {
            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "IntegrationTestFolder"
                    + StringUtils.fileSeparator() + "PipeTestFolder";

            shell.parseAndEvaluate("echo 'not yet cut' | cut -c 9-11 ", output);
            assertEquals("cut" + STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail();
        }

    }

    /**
     * Grep doesn't throw an error when no file is provided and stdin
     *  @throws AbstractApplicationException
     */
    @Test
    @DisplayName("Bug #33")
    public void testGrepWhenNoFileIsProvided() throws AbstractApplicationException {
        GrepApplication grepApplication = new GrepApplication();
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[]{"ff"}
            , System.in, output));
        assertTrue(expectedException.getMessage().contains("No input provided even after long time"));
    }

    /**
     * Changing directory to a non-readable directory should throw an error
     */
    @Test
    @DisplayName("Bug #34")
    public void testChangeToDirectoryWithNoReadPermission() {
        CdApplication cdApp = new CdApplication();
        String cdpath = Environment.getCurrentDirectory() + StringUtils.CHAR_FILE_SEP + "cd_test" + StringUtils.CHAR_FILE_SEP;

        File testDir = new File(cdpath);
        testDir.mkdir();
        testDir.setExecutable(false);

        Exception exception = assertThrows(Exception.class, () -> {
            cdApp.changeToDirectory(cdpath);
        });
        assertEquals("cd: " + cdpath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    /**
     * Null AppRunner for command
     */
    @Test
    @DisplayName("Bug #35")
    public void testFailsNullAppRunner() throws ShellException {

        ShellException exception = assertThrows(ShellException.class,
                () -> new CallCommand(
                        Arrays.asList("echo", "hello"),
                        null,
                        new ArgumentResolver())
        );

        assertMsgContains(exception, "Null App Runner");

    }

    /**
     * Null ArgumentResolver for command
     */
    @Test
    @DisplayName("Bug #36")
    public void testFailsNullArgResolver() throws ShellException {

        ShellException exception = assertThrows(ShellException.class,
                () -> new CallCommand(
                        Arrays.asList("echo", "hello"),
                        new ApplicationRunner(),
                        null)
        );

        assertMsgContains(exception, "Null Argument Resolver");

    }

}
