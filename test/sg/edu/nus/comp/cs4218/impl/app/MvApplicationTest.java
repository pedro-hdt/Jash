package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CANNOT_OVERWRITE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NOT_MOVABLE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Tests for mv command
 *
 * NOTE: This must be run as a suite for effective testing
 *
 * <p>
 *     Contains negative and positive test cases testing mv in isolation
 * </p>
 *
 * Negative
 * - Null args throws exception
 * - Less than two arguments throws exception
 * - Fails when attempting to rename non-existent file
 * - Fails when moving non-existent file
 *
 * Positive
 * - Rename an existing file
 * - Rename a directory
 * - move 1 file to directory
 * - Move multiple files to directory
 * - Move 1 directory to another directory
 * - Move with no overwrite -n flag
 * - Move with no flag so that it overwrites
 *
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

    private static MvApplication mvApp;

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
        FileOutputStream outputStream = new FileOutputStream(new File(Paths.get(Environment.getCurrentDirectory()
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
    }


    /**
     * Tests failing when null args passed
     */
    @Test
    public void testFailsWithNullArgs() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(null, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));

    }

    /**
     * Tests failing when insufficient args passed
     */
    @Test
    public void testFailsWithLessThan2Args() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[0], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));

        expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[1], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));

    }

    /**
     * Tests failing when illegal args passed
     */
    @Test
    public void testIllegalArgsException() {

        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[]{"no-file.txt", "no-dir", "-f"}, null, null));
        assertTrue(expectedException.getMessage().contains("illegal option"));

    }


    /**
     * Tests failing when file not present to be renamed
     */
    @Test
    public void testRenamingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"file-not-present.txt", NEW_NAME_FILE}, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));

    }

    /**
     * Tests failing scenario when file not present to be moved
     */
    @Test
    public void testMovingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"file-not-present.txt", DEST_DIR}, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));

    }

    /**
     * Tests renaming an existing file
     */
    @Test
    public void testRenameExistingFile() {
        try {
            mvApp.run(new String[] {OLD_NAME_FILE, NEW_NAME_FILE}, null, null);

            assertTrue(Files.exists(IOUtils.resolveFilePath(NEW_NAME_FILE)));
        } catch (MvException e) {
            fail("should not fail:" + e);//NOPMD -  Suppressed as its fine to have similar fail with different exception msg
        }
    }

    /**
     * Tests renaming an existing directory
     */
    @Test
    public void testRenameExistingDirectory() throws Exception {

        Files.createDirectory(IOUtils.resolveFilePath(OLD_DIR));

        mvApp.run(new String[] {OLD_DIR, NEW_DIR}, null, null);
        assertTrue(Files.exists(IOUtils.resolveFilePath(NEW_DIR)));

        Files.deleteIfExists(IOUtils.resolveFilePath(NEW_DIR));

    }


    /**
     * Tests moving a file to directory
     */
    @Test
    public void testMoveFileToDir() {
        try {
            mvApp.run(new String[] {FILE_TO_MOVE_TXT, DEST_DIR}, null, null);

            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                    + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + FILE_TO_MOVE_TXT)));
            assertTrue(!Files.exists(IOUtils.resolveFilePath(FILE_TO_MOVE_TXT)));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    /**
     * Tests moving of a directory to another directory
     * @throws Exception
     */
    @Test
    public void testMoveDirToAnotherDir() throws Exception {
        // Reset after moving dir to dir
        Files.createDirectory(IOUtils.resolveFilePath(INIT_DIR));

        mvApp.run(new String[] {INIT_DIR, DEST_DIR}, null, null);

        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + INIT_DIR)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(INIT_DIR)));

        // Cleanup
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + INIT_DIR));
    }

    /**
     * Tests if -n passed to not overwrite a file
     * @throws Exception
     */
    @Test
    public void testDontOverwriteFile() throws Exception {

        assertFalse(Arrays.equals(Files.readAllBytes(IOUtils.resolveFilePath(NO_OVERWRITE_FILE)),
                Files.readAllBytes(Paths.get(Environment.getCurrentDirectory()
                        + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + NO_OVERWRITE_FILE))));

        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"-n", DEST_DIR + StringUtils.fileSeparator()
                    + NO_OVERWRITE_FILE, NO_OVERWRITE_FILE}, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NOT_MOVABLE));

    }

    /**
     * Tests if directory being moved overwrite non-empty directory
     * @throws Exception
     */
    @Test
    public void testCantMoveToNonEmptyDir() {

        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"toBeMoved", "abc"}, null, null));
        System.out.println(expectedException.getMessage());
        assertTrue(expectedException.getMessage().contains(ERR_CANNOT_OVERWRITE));

    }

    /**
     * Tests to successfully overwrite a file and its content
     * @throws IOException
     */
    @Test
    public void testOverwriteFileSuccess() throws IOException {
        try {
            mvApp.run(new String[] {DEST_DIR + StringUtils.fileSeparator() + OVERWRITE_FILE, OVERWRITE_FILE}, null, null);

            String str = new String(Files.readAllBytes(IOUtils.resolveFilePath(OVERWRITE_FILE)));
            assertTrue(str.contains("first"));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    /**
     * Tests to move multiple files with no overwriting
     * @throws MvException
     */
    @Test
    public void testMoveMultipleFiles() throws MvException {
        mvApp.run(new String[] {MOVE_FIRST_TXT, MOVE_SECOND_TXT, DEST_DIR, "-n"}, null, null);

        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_FIRST_TXT)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(MOVE_FIRST_TXT)));

        assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + DEST_DIR + StringUtils.fileSeparator() + MOVE_SECOND_TXT)));
        assertTrue(!Files.exists(IOUtils.resolveFilePath(MOVE_SECOND_TXT)));

    }


}
