import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;

public class TeamTBugs {

    ShellImpl shell = new ShellImpl();
    OutputStream output = new ByteArrayOutputStream();
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
        output.flush();
        Environment.currentDirectory = ORIGINAL_DIR;
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
            assertEquals("abc 1 2 3xyz4 5 6" + StringUtils.STRING_NEWLINE, output.toString());
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
    public void testFailsSingleFileToSameDir() {
    
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
    public void testFailsSingleFileToItself() {
        
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
}
