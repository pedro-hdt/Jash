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
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.CutApplicationTest.ERR_OUT_OF_RANGE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class TeamUBugs {

    ShellImpl shell = new ShellImpl();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayInputStream input;
    static String ORIGINAL_DIR;

    public static void assertMsgContains(Exception exception, String text) {
        assertTrue(exception.getMessage().contains(text));
    }

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
        Environment.currentDirectory =  ORIGINAL_DIR;
        output.reset();
    }



    /**
     * rm with -r flag will not remove non-empty directories. Prints “This is a directory”
     */
    @Test
    @DisplayName("Bug Number #1.1")
    public void testRmNonEmptyFolderRecursive() throws IOException, RmException {

        Files.deleteIfExists(IOUtils.resolveFilePath("CS4218-rmTest"));

        // create 4 levels of nested temporary directories with a temp file at each level
        // ignore refs to files because they should be deleted by rm
        Path[] testDirs = new Path[4];
        testDirs[0] = Files.createDirectory(IOUtils.resolveFilePath("CS4218-rmTest"));
        Files.createTempFile(testDirs[0], "rmtest", "");
        for (int i = 1; i < 4; i++) {
            testDirs[i] = Files.createTempDirectory(testDirs[i - 1], "rmTest");
            Files.createTempFile(testDirs[i], "rmTest", "");
        }

        Path testTree = testDirs[0];

        RmApplication rmApp = new RmApplication();

        // assemble args and call rm to delete the outer directory recursively
        String[] args = {"-r", testTree.toString()};
        rmApp.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testTree));

    }

    /**
     * rm with -r flag will not remove empty directories. Prints “This is a directory”
     */
    @Test
    @DisplayName("Bug Number #1.2")
    public void testRmEmptyFolderRecursive() throws IOException, RmException {

        Files.deleteIfExists(IOUtils.resolveFilePath("CS4218-rmTest"));

        // create a temporary directory
        Path testDir = Files.createDirectory(IOUtils.resolveFilePath("CS4218-rmTest"));

        RmApplication rmApp = new RmApplication();

        // assemble args and call rm to delete the directory recursively
        String[] args = {"-r", testDir.toString()};
        rmApp.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testDir));

    }

    /**
     * ‘rm -rd .’ removes all files in the current directory, which should not be possible
     */
    @Test
    @DisplayName("Bug #2")
    @Disabled("RUN WITH CAUTION: may delete everything in curr dir")
    public void testRmFailsDirEndInDot() {

        RmApplication rmApp = new RmApplication();

        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(new String[]{"-rd", "."}, System.in, System.out);
        });
        assertTrue(exception.getMessage().contains("'.' or '..'")); // verify the correct exceptions is thrown

    }

    /**
     * Ls Returns files .txt present.
     * Here ls *.txt should output all txt extension files, however it shows:
     * ls: cannot access 'a.txt': No such file or directory
     */
    @Test
    @DisplayName("Bug #3")
    public void testLsWithFiles() throws AbstractApplicationException, ShellException {

        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "GlobbingTest";

        shell.parseAndEvaluate("ls *.txt", output);

        assertEquals("f1.txt" + StringUtils.STRING_NEWLINE + "match1.txt" + StringUtils.STRING_NEWLINE
                + "mvFile.txt" + StringUtils.STRING_NEWLINE + "test.txt" + StringUtils.STRING_NEWLINE, output.toString());
    }


    /**
     * Double quotes interprets back quotes
     * CommandSubs within Double quotes adds extra spaces
     * Should not print an extra space before the period
     */
    @Test
    @DisplayName("Bug #4")
    public void testDoubleQuotesWithBackQuote() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is random:`echo \"random\"`.\"", output);

        assertEquals(output.toString(), "This is random:random.");
    }


    /**
     * cp copies a file to the same directory it is in, overwriting itself unnecessarily
     * Should throw an exception reporting src and dest are the same file like GNU cp
     */
    @Test
    @DisplayName("Bug #5.1")
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
    @DisplayName("Bug #5.2")
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
     * Tests if directory being moved overwrite non-empty directory
     * Throws error message if not able to write to output stream when error occurs in not being able to replace a NonEmptyDirectory
     * Exception wrongly caught and different error message given
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Bug #6")
    public void testCantMoveToNonEmptyDir() {

        try {

            // set curred dir to the folder with test assets
            Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "MvTestFolder";

            shell.parseAndEvaluate("mv toBeMoved abc", output);
            fail();
        } catch (Exception e) {
            assertEquals("mv: cannot overwrite", e.getMessage());
        }

    }


    /**
     * cp with null output stream displays Null Pointer Exception without specifying the cause
     */
    @Test
    @DisplayName("Bug #7")
    public void testCpFailsNullOutputStream() {

        CpApplication cpApp = new CpApplication();

        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(new String[0], System.in, null));

        assertTrue(cpException.getMessage().contains(ERR_NO_OSTREAM));

    }


    /**
     * cp directory into itself creates infinite recursive nesting
     * Should skip the directory and report that it is the same file
     */
    @Test
    @DisplayName("Bug #8")
    public void testCpFolderToItselfWithAnotherValidFile() throws IOException {

        String fileB = "fileB.txt";
        String dirA = "dirA";

        CpApplication cpApp = new CpApplication();

        Files.createFile(IOUtils.resolveFilePath(fileB));
        Files.createDirectory(IOUtils.resolveFilePath(dirA));

        try {
            String[] args = {dirA, fileB, dirA};
            cpApp.run(args, System.in, System.out);
            fail();
        } catch (CpException e) {
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, dirA, fileB)));
            assertEquals("cp: " + StringUtils.STRING_NEWLINE +
              "dirA skipped: 'dirA' and 'dirA' are the same file", e.getMessage());
        } finally {
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, dirA, fileB));
            Files.deleteIfExists(Paths.get(Environment.currentDirectory
              + StringUtils.fileSeparator() + dirA));
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, fileB));
        }

    }


    /**
     * paste with null input stream displays Null Pointer Exception without specifying the cause
     */
    @Test
    @DisplayName("Bug #9.1")
    public void testPasteNullInputStream() {

        PasteApplication pasteApp = new PasteApplication();

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[0], null, output));

        assertTrue(exception.getMessage().contains(ERR_NO_ISTREAM));

    }

    /**
     * paste with null output stream displays Null Pointer Exception without specifying the cause
     */
    @Test
    @DisplayName("Bug #9.2")
    public void testPasteNullOutputStream() {

        PasteApplication pasteApp = new PasteApplication();

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[0], System.in, null));

        assertTrue(exception.getMessage().contains(ERR_NO_OSTREAM));

    }


    /**
     * Incorrect error message when calling paste with a directory as argument.
     * “No such file or directory” implies the user provided a nonexistent file.
     */
    @Test
    @DisplayName("Bug #10")
    public void testPasteDirOnlyFiles() throws IOException {

        PasteApplication pasteApp = new PasteApplication();

        Files.deleteIfExists(IOUtils.resolveFilePath("dir"));
        Path dir = Files.createDirectory(IOUtils.resolveFilePath("dir"));

        PasteException exception =
          assertThrows(PasteException.class, () -> pasteApp.run(new String[]{"dir"}, System.in, System.out));

        Files.delete(dir);
        assertTrue(exception.getMessage().contains(ERR_IS_DIR));

    }


    /**
     * ‘paste - -’ causes input to stdin to be printed twice such that if the user inputs
     * ‘a’ on a line and ‘b’ on the following line then terminates the command, the output generated is:
     * a\ta\nb\tb\n
     * instead of:
     * a\tb\n
     */
    @Test
    @DisplayName("Bug #11")
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
     * File not found when using ‘paste - file’ (pasting files with stdin) because filenames
     * are not resolved against current directory
     */
    @Test
    @DisplayName("Bug #12")
    public void testPasteOneFileOneStdinArgs() throws PasteException, IOException {

        // set curred dir to the folder with test assets
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "PasteTestFolder";

        input = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath("FILE2")));

        PasteApplication pasteApp = new PasteApplication();

        pasteApp.run(new String[]{"pasteFile1.txt", "-"}, input, output);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath("pasteFiles1and2.txt")))
            + STRING_NEWLINE,
          output.toString());

    }


    /**
     * NoSuchElementException exposed to user when the user attempts to redirect output without
     * a destination file (‘cmd args >’, e.g. ‘echo hello >’).
     * Correct behavior would be to report a syntax error
     */
    @Test
    @DisplayName("Bug #13")
    public void testOutputRedirFailsNoDest() {

        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(
          Arrays.asList("echo", "hello", ">"),
          System.in,
          System.out,
          new ArgumentResolver()
        );

        ShellException shellException =
          assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());

        assertTrue(shellException.getMessage().contains(ERR_SYNTAX));
    }


    /**
     * Bug #14
     * Cannot finish executing paste command with stdin input without terminating the shell
     * 'paste -' only terminates on EOF, but EOF terminates the entire shell application.
     */


    /**
     * Find with globs causes an unexpected error: ‘Paths must precede -name’.
     * The problem should be that you can only search for a single name
     */
    @Test
    @DisplayName("Bug #15")
    public void testFindGlobInvalid() {
        try {
            shell.parseAndEvaluate("find ./ -name *", output);
            fail();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertMsgContains(e, "find: Only one filename is allowed");
        }
    }


    /**
     * Find with quoted glob causes inexplicable exception ‘Dangling meta character '*' near index 0’
     */
    @Test
    @DisplayName("Bug #16")
    public void testFindGlob() {

        Environment.currentDirectory = ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "GlobbingFolder";

        try {
            shell.parseAndEvaluate("find ./ -name \"*\"", output);
            assertEquals("./dir"
              + StringUtils.STRING_NEWLINE + "./dir/empty.txt"
              + StringUtils.STRING_NEWLINE + "./wc1.txt"
              + StringUtils.STRING_NEWLINE + "./wc100.txt" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
<<<<<<< HEAD
     * Try find command with multiple flags
     * The command works if last flag is -name. If any other flag is last, wrong message is displayed.
     */
    @Test
    @DisplayName("Bug #17")
    public void testFindWithMultipleFlags() {
        FindApplication findApplication = new FindApplication();
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{"A",
            "-name", "-i", "test"}, System.in, output));
        assertTrue(expectedException.getMessage().contains("Wrong flag provided"));
    }

    /**
     * Try find command with multiple file names
     * The command displays a wrong error message
     */
    @Test
    @DisplayName("Bug #18")
    public void testFindWithMultipleFileNames() {
        FindApplication findApplication = new FindApplication();
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[] {"Test-folder-2", "-name", "Test-folder-2-2", "textfile.txt"}, System.in, output));
        assertTrue(expectedException.getMessage().contains("Multiple file names provided"));
    }

     /**
     * Diff command didn’t check for invalid flag
     */
    @Test
    @DisplayName("Bug #19")
    public void testFailsInvalidFlag() {
        DiffApplication diffApp = new DiffApplication();

        Exception expectedException = assertThrows(DiffException.class, () -> diffApp.run(new String[]{"one", "two", "-inv"}, System.in, System.out));
        assertEquals(expectedException.getMessage(), ERR_INVALID_FLAG);
    }

    /**
     * Diff command didn’t follow output format specified by pdf
     */
    @Test
    @DisplayName("Bug #20")
    public void testDiffFilesWithDifferentContentUsingFlagQ() {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";
        String DIFF2_FILE = "diff2.txt";

        try {
            String expected = diffApp.diffTwoFiles(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE, false, false, true);
            assertEquals(expected, "Files [" + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE + " " + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF2_FILE + "] differ"); // NOPMD
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command should not output anything for different directories with files of same content
     */
    @Test
    @DisplayName("Bug #21")
    public void testDiffDirContainFilesWithSameContent() {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFFDIR1 = "diffDir1";
        String DIFFDIR1_IDENTICAL = "diffDir1-identical"; // NOPMD

        try {
            String expected = diffApp.diffTwoDir(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFFDIR1, DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFFDIR1_IDENTICAL, false, false, false);
            assertEquals(expected, ""); // No message represents a successful diff
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Diff command doesn’t read stdin as ‘-’ when displaying in the output
     */
    @Test
    @DisplayName("Bug #22")
    public void testDiffFileAndStdinWithSameContentUsingFlagS() {
        String DIFF_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "DiffTestFolder";
        DiffApplication diffApp = new DiffApplication();
        String DIFF1_FILE = "diff1.txt";

        try {
            InputStream inputStream = new FileInputStream(new File(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE)); //NOPMD
            String expected = diffApp.diffFileAndStdin(DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE, inputStream, true, false, false);
            assertEquals(expected, "Files [" + DIFF_TEST_DIR + StringUtils.fileSeparator() + DIFF1_FILE + " -] are identical");
        } catch (Exception e) {
            fail("should not fail: " + e.getMessage());
        }
    }

    /**
     * Cut command doesn’t detect out of range
     */
    @Test
    @DisplayName("Bug #23")
    public void testOutOfRange() {
        CutApplication cutApp = new CutApplication();
        String CUT1_FILE = "cut1.txt";

        String[] args = new String[]{"-c", "0,4", CUT1_FILE}; // NOPMD
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, output));
        assertEquals(expectedException.getMessage(), ERR_OUT_OF_RANGE);
    }

    /**
     * Cut command doesn’t detect invalid range
     */
    @Test
    @DisplayName("Bug #24")
    public void testInvalidRange() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String CUT1_FILE = "cut1.txt";

        String[] args = new String[]{"-c", "5,1", CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE}; // NOPMD
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, output));
        assertTrue(expectedException.getMessage().contains(ERR_INVALID_RANGE));
    }

    /**
     * Cut command doesn’t detect illegal flag
     */
    @Test
    @DisplayName("Bug #25")
    public void testIllegalFlag() {
        String CUT_TEST_DIR = ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CutTestFolder";
        CutApplication cutApp = new CutApplication();
        String CUT1_FILE = "cut1.txt";

        String[] args = new String[]{"-f", "1,4", CUT_TEST_DIR + StringUtils.fileSeparator() + CUT1_FILE}; // NOPMD
        Exception expectedException = assertThrows(CutException.class, () -> cutApp.run(args, System.in, output));
        assertEquals(expectedException.getMessage(), ERR_INVALID_FLAG);
    }

    /**
     * Extra '/' when running wc command then find command
     */
    @Test
    @DisplayName("Bug #26")
    public void testWcThenFind() throws ShellException, AbstractApplicationException {
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder" +
                StringUtils.fileSeparator() + "WcIntegrationFolder";

        String expected = "       2 wctest.txt" + STRING_NEWLINE +
                "../WcIntegrationFolder/wctest.txt" + STRING_NEWLINE;
        String cmdline = "wc -l wctest.txt; find ../ -name wctest.txt";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Extra unexpected output when running wc command then cut command
     */
    @Test
    @DisplayName("Bug #27")
    public void testWcThenCut() throws ShellException, AbstractApplicationException {
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder" +
                StringUtils.fileSeparator() + "WcIntegrationFolder";

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
    @DisplayName("Bug #28")
    public void testSortThenCut() throws ShellException, AbstractApplicationException {
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder" +
                StringUtils.fileSeparator() + "SortIntegrationFolder";

        String expected = "G" + STRING_NEWLINE;
        String cmdline = "sort -nrf sorttest.txt | cut -b 1 -";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Unexpected flags printed in the error message
     */
    @Test
    @DisplayName("Bug #29")
    public void testSortThenEchoNegative() throws ShellException, AbstractApplicationException {
        Environment.currentDirectory += StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder" +
                StringUtils.fileSeparator() + "SortIntegrationFolder";

        String expected = "sort: No such file or directory" + STRING_NEWLINE +
                "sorttest.txt" + STRING_NEWLINE;
        String cmdline = "sort -nrfe sorttest.txt; echo sorttest.txt";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    /**
     * Throws error that file doesn't exist when it should be creating the directory needed for files position
     * Create a file in non existent directory 2 level down and write into it
     */
    @Test
    @DisplayName("Bug #30")
    public void testCreateDirectoryThroughOutputstreamForFile() {
        try {
            shell.parseAndEvaluate("echo yo > dir1/dir2/out.txt", output);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Ls With -R in multiple nested directory gives result in wrong format
     * Missing line breaks and empty lines
     */
    @Test
    @DisplayName("Bug #31")
    public void testLsWithRecursiveDirectory() {
        try {

            Environment.currentDirectory = ORIGINAL_DIR
                    + StringUtils.fileSeparator() + "dummyTestFolder"
                    + StringUtils.fileSeparator() + "LsTestFolder";

            shell.parseAndEvaluate("ls -R dire", output);
            assertEquals("dire:\n" +
                    "abc\n" +
                    "def\n" +
                    "new.txt\n" +
                    "\n" +
                    "dire/abc:\n" +
                    "a.txt\n" +
                    "zz\n" +
                    "\n" +
                    "dire/abc/zz:\n" +
                    "b.txt\n" + StringUtils.STRING_NEWLINE + "dire/def:\n" +
                    "f.txt", output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Try find command with wrong flag suffix
     * The error thrown is wrong. It shows no file is provided where as the wrong flag is provided.
     */
    @Test
    @DisplayName("Bug #32")
    public void testFindWithWrongFlagSuffix() {
        FindApplication findApplication = new FindApplication();
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{"A",
            "-i", "test"}, System.in, output));
        assertTrue(expectedException.getMessage().contains("Wrong flag provided"));
    }

}
