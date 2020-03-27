import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
        
        // create a temporary directory
        Path testDir = Files.createDirectory(IOUtils.resolveFilePath("CS4218-rmTest"));
        
        RmApplication rmApp = new RmApplication();
        
        // assemble args and call rm to delete the directory recursively
        String[] args = {"-r", testDir.toString()};
        rmApp.run(args, System.in, System.out);
        
        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testDir));
        
    }
    
}
