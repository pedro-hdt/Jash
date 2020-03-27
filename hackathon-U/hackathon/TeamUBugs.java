import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;

public class TeamUBugs {
    
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
    }

    /**
     * Double quotes interprets back quotes
     * CommandSubs within Double quotes adds extra spaces
     * Should not print an extra space before the period
     */
    @Test
    @DisplayName("Bug Number #4")
    public void testDoubleQuotesWithBackQuote() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is random:`echo \"random\"`.\"", output);
    
        assertEquals(output.toString(), "This is random:random.");
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
    
    
}
