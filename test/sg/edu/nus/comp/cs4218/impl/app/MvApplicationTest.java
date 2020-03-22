package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CANNOT_OVERWRITE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

/**
 * Tests for mv command
 * <p>
 * NOTE: This must be run as a suite for effective testing
 *
 * <p>
 * Contains negative and positive test cases testing mv in isolation
 * </p>
 * <p>
 * Negative
 * - Null args throws exception
 * - Less than two arguments throws exception
 * - Fails when attempting to rename non-existent file
 * - Fails when moving non-existent file
 * - Fails when overwriting to non-empty directory
 * <p>
 * Positive
 * - Rename an existing file
 * - Rename a directory
 * - move 1 file to directory
 * - Move multiple files to directory
 * - Move 1 directory to another directory
 * - Move with no overwrite -n flag
 * - Move with no flag so that it overwrites
 */
public class MvApplicationTest {
    
    public static final String DEST_DIR = "destDir";
    
    public static final String OLD_NAME_FILE = "oldName.txt";
    public static final String NEW_NAME_FILE = "newName.txt";
    
    public static final String OLD_DIR = "dirOld";
    public static final String NEW_DIR = "dirNew";
    public static final String FILE_TO_MOVE_TXT = "fileToMove.txt";
    public static final String INIT_DIR = "initDir";
    public static final String OVERWRITE_FILE = "overwriteFile.txt";
    public static final String MOVE_FIRST_TXT = "moveFirst.txt";
    public static final String MOVE_SECOND_TXT = "moveSecond.txt";
    public static final String NO_OVERWRITE_FILE = "NotOverwriteThisFile.txt";
    public static final String FILE_B_TXT = "fileB.txt";
    public static final String DIR_A = "dirA";
    
    private static MvApplication mvApp;
    private static OutputStream stdout;
    
    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    
    
    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "MvTestFolder");
    }
    
    @AfterAll
    static void reset() throws IOException {
        // Reset for test renaming file
        Files.createFile(IOUtils.resolveFilePath(OLD_NAME_FILE));
        Files.deleteIfExists(IOUtils.resolveFilePath(NEW_NAME_FILE));
        
        
        // Reset after moving file to dir
        Files.createFile(IOUtils.resolveFilePath(FILE_TO_MOVE_TXT));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + FILE_TO_MOVE_TXT));
        
        
        // Reset after overwriting
        Files.createFile(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + OVERWRITE_FILE));
        FileOutputStream outputStream = new FileOutputStream(new File(Paths.get(Environment.getCurrentDirectory() //NOPMD
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + OVERWRITE_FILE).toString()));
        byte[] strToBytes = "first".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
        
        outputStream = new FileOutputStream(IOUtils.resolveFilePath(OVERWRITE_FILE).toFile());
        strToBytes = "second".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
        
        
        // Reset after moving multiple files
        Files.createFile(IOUtils.resolveFilePath(MOVE_FIRST_TXT));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_FIRST_TXT));
        Files.createFile(IOUtils.resolveFilePath(MOVE_SECOND_TXT));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_SECOND_TXT));
        
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    
    @BeforeEach
    void setUp() {
        mvApp = new MvApplication();
        stdout = new ByteArrayOutputStream();
    }
    
    
    /**
     * Tests failing when null args passed
     */
    @Test
    public void testFailsWithNullArgs() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));
    
    }
    
    // Test mv app with null outputstream
    @Test
    void testNullOutputStream() {
        try {
            String[] args = {};
            mvApp.run(args, System.in, null);
            fail();
        } catch (MvException e) {
            assertEquals("mv: OutputStream not provided", e.getMessage());
        }
    }
    
    /**
     * Tests failing when insufficient args passed
     */
    @Test
    public void testFailsWithLessThan2Args() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[0], null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));
    
        expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[1], null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));
    
    }
    
    /**
     * Tests failing when illegal args passed
     */
    @Test
    public void testIllegalArgsException() {
    
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[]{"no-file.txt", "no-dir", "-f"}, null, stdout));
        assertTrue(expectedException.getMessage().contains("illegal option"));
    
    }
    
    
    /**
     * Tests failing when file not present to be renamed
     */
    @Test
    public void testRenamingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{"file-not-present.txt", NEW_NAME_FILE}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    
    }
    
    /**
     * Tests failing scenario when file not present to be moved
     */
    @Test
    public void testMovingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{"file-not-present.txt", DEST_DIR}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));
    
    }
    
    /**
     * Tests renaming an existing file
     */
    @Test
    public void testRenameExistingFile() {
        try {
            mvApp.run(new String[]{OLD_NAME_FILE, NEW_NAME_FILE}, null, stdout);
    
            assertTrue(Files.exists(IOUtils.resolveFilePath(NEW_NAME_FILE)));
        } catch (MvException e) {
            fail("should not fail:" + e);//NOPMD -  Suppressed as its fine to have similar fail with different exception msg
        }
    }
    
    /**
     * Tests attempt overwriting multiple files
     */
    @Test
    public void testAttemptOverwritingMultipleFiles() {
    
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{OLD_NAME_FILE, MOVE_FIRST_TXT, OLD_NAME_FILE}, null, stdout));
        assertTrue(expectedException.getMessage().contains("'" + OLD_NAME_FILE + "' is not a directory."));
    
    }
    
    /*
     * Test mv app move a folder into itself, and move a txt file into said folder
     * The txt file will be successfully moved into the folder
     */
    @Test
    void testMoveSameFolderWithAnotherValidFile() throws IOException {
    
        Files.createFile(IOUtils.resolveFilePath(FILE_B_TXT));
        Files.createDirectory(IOUtils.resolveFilePath(DIR_A));
    
        String dirA = DIR_A;
        try {
            String[] args = {dirA, FILE_B_TXT, dirA};
            mvApp.run(args, System.in, System.out);
            fail();
        } catch (MvException e) {
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, DIR_A, FILE_B_TXT)));
            assertEquals("mv: " + "error moving file", e.getMessage());
        } finally {
            Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), DIR_A, FILE_B_TXT));
            Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
              + StringUtils.fileSeparator() + DIR_A));
        }
    }
    
    /**
     * Tests rename multiple files exception
     */
    @Test
    public void testRenameMultipleFilesException() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[]{OLD_NAME_FILE, MOVE_FIRST_TXT, "random-new-name.txt"}, null, stdout));
        assertTrue(expectedException.getMessage().contains("can't rename multiple files"));
    }
    
    /**
     * Tests renaming an existing directory
     */
    @Test
    public void testRenameExistingDirectory() throws Exception {
    
        Files.createDirectory(IOUtils.resolveFilePath(OLD_DIR));
    
        mvApp.run(new String[]{OLD_DIR, NEW_DIR}, null, stdout);
        assertTrue(Files.exists(IOUtils.resolveFilePath(NEW_DIR)));
    
        Files.deleteIfExists(IOUtils.resolveFilePath(NEW_DIR));
    
    }
    
    
    /**
     * Tests moving a file to directory
     */
    @Test
    public void testMoveFileToDir() {
        try {
            mvApp.run(new String[]{FILE_TO_MOVE_TXT, DEST_DIR}, null, stdout);
    
            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
              + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + FILE_TO_MOVE_TXT)));
            assertTrue(!Files.exists(IOUtils.resolveFilePath(FILE_TO_MOVE_TXT)));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }
    
    /**
     * Tests moving of a directory to another directory
     *
     * @throws Exception
     */
    @Test
    public void testMoveDirToAnotherDir() throws Exception {
        // Reset after moving dir to dir
        Files.createDirectory(IOUtils.resolveFilePath(INIT_DIR));
    
        mvApp.run(new String[]{INIT_DIR, DEST_DIR}, null, stdout);
    
        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + INIT_DIR)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(INIT_DIR)));
    
        // Cleanup
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + INIT_DIR));
    }
    
    /**
     * Tests if -n passed to not overwrite a file
     *
     * @throws Exception
     */
    @Test
    public void testDontOverwriteFile() throws Exception {
    
        assertFalse(Arrays.equals(Files.readAllBytes(IOUtils.resolveFilePath(NO_OVERWRITE_FILE)),
          Files.readAllBytes(Paths.get(Environment.getCurrentDirectory()
            + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + NO_OVERWRITE_FILE))));
    
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{"-n", DEST_DIR + StringUtils.fileSeparator()
          + NO_OVERWRITE_FILE, NO_OVERWRITE_FILE}, null, stdout));
        assertTrue(expectedException.getMessage().contains("is a file and replacement not allowed"));
    
    }
    
    /**
     * Tests if directory being moved overwrite non-empty directory
     *
     * @throws Exception
     */
    @Test
    public void testCantMoveToNonEmptyDir() {
    
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{"toBeMoved", "abc"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_CANNOT_OVERWRITE));
    
    }
    
    /**
     * Tests to successfully overwrite a file and its content
     *
     * @throws IOException
     */
    @Test
    public void testOverwriteFileSuccess() throws IOException {
        try {
            mvApp.run(new String[]{DEST_DIR + StringUtils.fileSeparator() + OVERWRITE_FILE, OVERWRITE_FILE}, null, stdout);
    
            String str = new String(Files.readAllBytes(IOUtils.resolveFilePath(OVERWRITE_FILE)));
            assertTrue(str.contains("first"));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }
    
    /**
     * Tests to move multiple files with no overwriting
     *
     * @throws MvException
     */
    @Test
    public void testMoveMultipleFiles() throws MvException {
        mvApp.run(new String[]{MOVE_FIRST_TXT, MOVE_SECOND_TXT, DEST_DIR, "-n"}, null, stdout);
    
        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_FIRST_TXT)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(MOVE_FIRST_TXT)));
    
        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
          + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_SECOND_TXT)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(MOVE_SECOND_TXT)));
    
    }
    
    /*
     * Test mv app when a file with same name is being moved without overwrite
     */
    @Test
    void testFileAlreadyExistsWithNoOverwriteAllowed() throws IOException {
        String dirB = "dirB";
        String fileA = "fileA.txt";
    
        Files.createFile(IOUtils.resolveFilePath(fileA));
        Files.createDirectory(IOUtils.resolveFilePath(dirB));
        Files.createFile(Paths.get(Environment.currentDirectory, dirB, fileA));
    
        Exception expectedException = assertThrows(MvException.class, ()
          -> mvApp.run(new String[]{fileA, dirB, "-n"}, null, stdout));
        assertEquals("mv: " + ERR_CANNOT_OVERWRITE + " file already exists: fileA.txt", expectedException.getMessage());
    
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), dirB, fileA));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), dirB));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), fileA));
    }
    
    
}
