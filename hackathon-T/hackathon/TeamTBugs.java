import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
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
     */
    @Test
    @DisplayName("Bug #7")
    public void testExitUntestable() {
        try {

            shell.parseAndEvaluate("exit", output);
            assertEquals("       5 hella.txt -        6 hellz.txt" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
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
        assertMsgContains(cpException, "same file");
    
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
        assertMsgContains(cpException, "same file");
        
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
    
        assertMsgContains(exception, ERR_NO_OSTREAM);
    
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
        assertMsgContains(exception, ERR_IS_DIR);
    
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
    
        assertMsgContains(exception, ERR_FILE_NOT_FOUND);
    
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
    
    @Test
    @DisplayName("Bug #19")
    @Disabled("RUN WITH CAUTION: may delete everything in curr dir")
    public void failsDirEndInDot() {
        
        RmApplication rmApp = new RmApplication();
        
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(new String[]{"-rd", "."}, System.in, System.out);
        });
        assertMsgContains(exception, "'.' or '..'"); // verify the correct exceptions is thrown
        
    }
    
    
}
