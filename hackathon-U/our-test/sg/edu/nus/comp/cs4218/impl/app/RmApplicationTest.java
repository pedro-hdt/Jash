package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.ArgsParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * Provides unit tests for the RmApplication class
 * A number of utility/wrapper methods are used to facilitate file and directory creation
 * Negative test cases are suffixed with the word "Attempt"
 * <p>
 * Positive test cases:
 * - removing a single file
 * - removing an empty directory with the -d flag
 * - removing a non-empty directory with the -r flag
 * - removing an empty directory with the -r flag
 * - removing a list of files and both empty and non-empty directories
 * <p>
 * Negative test cases:
 * - removing an empty directory without any flags
 * - removing a non-empty directory without any flags
 * - removing a non-empty directory with the -d flag
 */
public class RmApplicationTest {

    public static final String ERR_DOT_DIR = "refusing to remove '.' or '..' directory";
    
    private static RmApplication rmApp;
    
    // we keep this string as suffix in the filenames created
    private static final String RM_TEST_CLASS = "RmApplicationTest";
    
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    
    
    @BeforeEach
    public void setRm() {
        rmApp = new RmApplication();
    }
    
    @AfterAll
    public static void tearDown() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    /**
     * Utility method to create a temporary file
     */
    public Path mkFile() throws IOException {
        return Files.createTempFile(RM_TEST_CLASS, "");
    }
    
    
    /**
     * Utility method to create a temporary file in a specific directory
     */
    public Path mkFile(Path dir) throws IOException {
        return Files.createTempFile(dir, RM_TEST_CLASS, "");
    }
    
    
    /**
     * Utility method to create an empty temporary directory
     */
    public Path mkEmptyDir() throws IOException {
        return Files.createTempDirectory(RM_TEST_CLASS);
    }
    
    
    /**
     * Utility method to create a temporary directory with a file inside it
     * The reference to such file is lost so this must be used carefully
     */
    public Path mkDirWithFile() throws IOException {
        Path dir = mkEmptyDir();
        mkFile(dir);
        return dir;
    }
    
    
    /**
     * Utility method to create a populated directory tree
     * Used to test recursive removal
     */
    public Path mkTree() throws IOException {
        Path[] testDirs = new Path[4];
        testDirs[0] = Files.createTempDirectory(RM_TEST_CLASS);
        Files.createTempFile(testDirs[0], RM_TEST_CLASS, "");
        for (int i = 1; i < 4; i++) {
            testDirs[i] = Files.createTempDirectory(testDirs[i - 1], RM_TEST_CLASS);
            Files.createTempFile(testDirs[i], RM_TEST_CLASS, "");
        }
        return testDirs[0];
    }
    
    
    /**
     * Remove a single file
     */
    @Test
    public void rmFile() throws IOException, AbstractApplicationException {
    
        // create a temporary file
        Path testFile = mkFile();
    
        // assemble args and call rm
        String[] args = {testFile.toString()};
        rmApp.run(args, System.in, System.out);
    
        // make sure file no longer exists afterwards
        assertFalse(Files.exists(testFile));
    }
    
    
    /**
     * Remove an empty folder with the -d flag
     */
    @Test
    public void rmEmptyFolderDFlag() throws IOException, RmException {
    
        // create a temporary directory
        Path testDir = mkEmptyDir();
    
        // assemble args and call rm
        String[] args = {"-d", testDir.toString()};
        rmApp.run(args, System.in, System.out);
    
        // make sure directory no longer exists afterwards
        assertFalse(testDir.toFile().exists());
    }
    
    
    /**
     * Remove a non empty folder recursively
     */
    @Test
    public void rmNonEmptyFolderRecursive() throws IOException, RmException {
    
        // create 4 levels of nested temporary directories with a temp file at each level
        // can afford to ignore refs to files because they will be deleted by rm
        Path testTree = mkTree();
    
        // assemble args and call rm to delete the outer directory recursively
        String[] args = {"-r", testTree.toString()};
        rmApp.run(args, System.in, System.out);
    
        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testTree));
    
    }
    
    
    /**
     * Remove an empty folder recursively
     */
    @Test
    public void rmEmptyFolderRecursive() throws IOException, RmException {
    
        // create a temporary directory
        Path testDir = mkEmptyDir();
    
        // assemble args and call rm to delete the directory recursively
        String[] args = {"-r", testDir.toString()};
        rmApp.run(args, System.in, System.out);
    
        // make sure directory no longer exists afterwards
        assertFalse(Files.exists(testDir));
    
    }
    
    /**
     * Remove a list of files and folders empty or non empty with -r flag
     */
    @Test
    public void rmMultFilesAndDirsRecursive() throws IOException, RmException {
    
        List<Path> filesAndDirs = new LinkedList<>();
    
        // populate list of files
        filesAndDirs.add(mkEmptyDir());
        filesAndDirs.add(mkFile());
        filesAndDirs.add(mkDirWithFile());
        filesAndDirs.add(mkTree());
    
        // assemble args and call rm to delete all recursively
        List<String> args = new LinkedList<>();
        args.add("-r");
        args.addAll(filesAndDirs.stream().map(Path::toString).collect(Collectors.toList()));
        rmApp.run(args.toArray(new String[0]), System.in, System.out);
    
        // check all files are gone
        assertTrue(filesAndDirs.stream().noneMatch(Files::exists));
    
    }
    
    
    /**
     * Attempt to remove an empty folder <b>without any flags</b>
     * Should not remove folder
     */
    @Test
    public void rmEmptyFolderNoFlagsAttempt() throws IOException {
    
        // create a temporary directory
        Path testDir = mkEmptyDir();
    
        // assemble args and call rm expecting an exception
        String[] args = {testDir.toString()};
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(args, System.in, System.out);
        });
    
        // verify it was the correct exception
        assertMsgContains(exception, ERR_IS_DIR);
    
        // make sure directory still exists afterwards
        assertTrue(Files.exists(testDir));
    
        // cleanup
        Files.delete(testDir);
    
    }
    
    
    /**
     * Attempt to remove a non-empty folder <b>with the -d flag</b>, but no -r flag
     * Should <b>not</b> remove the folder
     */
    @Test
    public void rmNonEmptyFolderDFlagAttempt() throws IOException {
    
        // create a temporary directory and make sure it exists
        Path testDir = mkEmptyDir();
    
        // create a file inside the temporary directory
        Path testFile = mkFile(testDir);
    
        // assemble args again, this time with the -d flag and call rm expecting an exception
        String[] args = {"-d", testDir.toString()};
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(args, System.in, System.out);
        });
        assertMsgContains(exception, ERR_IS_DIR); // verify the correct exceptions is thrown
    
        // make sure directory AND file still exist afterwards
        assertTrue(Files.exists(testFile));
        assertTrue(Files.exists(testDir));
    
        // cleanup
        Files.delete(testFile);
        Files.delete(testDir);
    
    }
    
    
    /**
     * Attempt to remove a non-empty folder <b>without any flags</b>
     * Should <b>not</b> remove the folder
     */
    @Test
    public void rmNonEmptyFolderNoFlagsAttempt() throws IOException {
    
        // create a temporary directory with a file inside
        Path testDir = mkEmptyDir();
    
        // create a file inside the temporary directory
        Path testFile = mkFile(testDir);
    
        // assemble args and call rm expecting an exception
        String[] args = {testDir.toString()};
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(args, System.in, System.out);
        });
        assertMsgContains(exception, ERR_IS_DIR); // verify the correct exceptions is thrown
    
        // make sure directory and file still exist afterwards
        assertTrue(Files.exists(testFile));
        assertTrue(Files.exists(testDir));
    
        // cleanup
        Files.delete(testFile);
        Files.delete(testDir);
    
    }
    
    
    /**
     * Attempt to call remove with no arguments
     */
    @Test
    public void rmNoArguments() {
    
        // call rm expecting an exception
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(new String[0], System.in, System.out);
        });
        assertMsgContains(exception, ERR_NO_ARGS); // verify the correct exceptions is thrown
    
    }
    
    
    /**
     * Attempt to call rm with an invalid flag
     */
    @Test
    public void rmInvlaidFlag() {
    
        // call rm expecting an exception
        String[] args = {"-a", "fakeFile"};
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(args, System.in, System.out);
        });
        assertMsgContains(exception, ArgsParser.ILLEGAL_FLAG_MSG); // verify the correct exceptions is thrown
    
    }
    
    /**
     * Attempt to call rm with null args
     */
    @Test
    public void failsNullArgs() {
    
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(null, System.in, System.out);
        });
        assertMsgContains(exception, ERR_NULL_ARGS); // verify the correct exceptions is thrown
    
    }
    
    /**
     * Attempt to call rm with null args
     */
    @Test
    public void failsDirEndInDot() {
    
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(new String[]{"."}, System.in, System.out);
        });
        assertMsgContains(exception, ERR_DOT_DIR); // verify the correct exceptions is thrown
    
    }
    
    /**
     * Call rm with multiple files, some of them inexistent
     */
    @Test
    public void rmIMultFilesSomeNonExistent() throws IOException {
    
        Path actualFile1 = mkFile();
        actualFile1.toFile().deleteOnExit();
        Path actualFile2 = mkFile();
        actualFile2.toFile().deleteOnExit();
    
        // call rm expecting an exception
        String[] args = {actualFile1.toString(), "fakeFile", actualFile2.toString()};
        RmException exception = assertThrows(RmException.class, () -> {
            rmApp.run(args, System.in, System.out);
        });
        assertMsgContains(exception, ERR_FILE_NOT_FOUND); // verify the correct exceptions is thrown
        assertMsgContains(exception, "fakeFile");
        assertFalse(Files.exists(actualFile1));
        assertFalse(Files.exists(actualFile2));
    
    }
    
}
