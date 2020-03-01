package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

/**
 * Tests for mv command
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

    private static MvApplication mvApp;

    private static final String originalDir = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(originalDir + File.separator + "dummyTestFolder" + File.separator + "MvTestFolder");
    }

    @AfterAll
    static void reset() throws IOException {
        // Reset for test renaming file
        Files.createFile(IOUtils.resolveFilePath("oldName.txt"));
        Files.deleteIfExists(IOUtils.resolveFilePath("newName.txt"));

        // Reset for test renaming directory
        Files.createDirectory(IOUtils.resolveFilePath("dirOld"));
        Files.deleteIfExists(IOUtils.resolveFilePath("dirNew"));


        // Reset after moving file to dir
        Files.createFile(IOUtils.resolveFilePath("fileToMove.txt"));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "fileToMove.txt"));

        // Reset after moving dir to dir
        Files.createDirectory(IOUtils.resolveFilePath("initDir"));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "initDir"));

        // Reset after overwriting
        Files.createFile(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "overwriteFile.txt"));
        FileOutputStream outputStream = new FileOutputStream(new File(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "overWriteFile.txt").toString()));
        byte[] strToBytes = "first".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();

        outputStream = new FileOutputStream(IOUtils.resolveFilePath("overwriteFile.txt").toFile());
        strToBytes = "second".getBytes();
        outputStream.write(strToBytes);
        outputStream.close();


        // Reset after moving multiple files
        Files.createFile(IOUtils.resolveFilePath("moveFirst.txt"));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "moveFirst.txt"));
        Files.createFile(IOUtils.resolveFilePath("moveSecond.txt"));
        Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                + File.separator + "destDir" + File.separator + "moveSecond.txt"));

        Environment.setCurrentDirectory(originalDir);
    }


    @BeforeEach
    void setUp() {
        mvApp = new MvApplication();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void testFailsWithNullArgs() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(null, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));

    }

    @Test
    public void testFailsWithLessThan2Args() {
        Exception expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[0], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));

        expectedException = assertThrows(MvException.class, () -> mvApp.run(new String[1], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NO_FILE_ARGS));

    }

    @Test
    public void testRenamingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"file-not-present.txt", "newName.txt"}, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));

    }

    @Test
    public void testMovingFailsWhenFileNotPresent() {
        Exception expectedException = assertThrows(MvException.class, ()
                -> mvApp.run(new String[] {"file-not-present.txt", "destDir"}, null, null));
        assertTrue(expectedException.getMessage().contains(ERR_FILE_NOT_FOUND));

    }

    @Test
    public void testRenameExistingFile() {
        try {
            mvApp.run(new String[] {"oldName.txt", "newName.txt"}, null, null);

            assertTrue(Files.exists(IOUtils.resolveFilePath("newName.txt")));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    @Test
    public void testRenameExistingDirectory() {
        try {
            mvApp.run(new String[] {"dirOld", "dirNew"}, null, null);

            assertTrue(Files.exists(IOUtils.resolveFilePath("dirNew")));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }


    @Test
    public void testMoveFileToDir() {
        try {
            mvApp.run(new String[] {"fileToMove.txt", "destDir"}, null, null);

            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                    + File.separator + "destDir" + File.separator + "fileToMove.txt")));
            assertTrue(!Files.exists(IOUtils.resolveFilePath("fileToMove.txt")));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    @Test
    public void testMoveDirToAnotherDir() {
        try {
            mvApp.run(new String[] {"initDir", "destDir"}, null, null);

            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                    + File.separator + "destDir" + File.separator + "initDir")));
            assertTrue(!Files.exists(IOUtils.resolveFilePath("initDir")));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    @Test
    public void testDontOverwriteFile() throws IOException {
        try {
            mvApp.run(new String[] {"-n", "destDir/NotOverwriteThisFile.txt", "NotOverwriteThisFile.txt"}, null, null);

            assertFalse(Arrays.equals(Files.readAllBytes(IOUtils.resolveFilePath("NotOverwriteThisFile.txt")),
                    Files.readAllBytes(Paths.get(Environment.getCurrentDirectory()
                            + File.separator + "destDir" + File.separator + "NotOverwriteThisFile.txt"))));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    @Test
    public void testOverwriteFileSuccess() throws IOException {
        try {
            mvApp.run(new String[] {"destDir/overwriteFile.txt", "overwriteFile.txt"}, null, null);

            String s = new String(Files.readAllBytes(IOUtils.resolveFilePath("overwriteFile.txt")));
            assertTrue(s.contains("first"));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }

    @Test
    public void testMoveMultipleFiles() {
        try {
            mvApp.run(new String[] {"moveFirst.txt", "moveSecond.txt", "destDir"}, null, null);

            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                    + File.separator + "destDir" + File.separator + "moveFirst.txt")));
            assertTrue(!Files.exists(IOUtils.resolveFilePath("moveFirst.txt")));

            assertTrue(Files.exists(Paths.get(Environment.getCurrentDirectory()
                    + File.separator + "destDir" + File.separator + "moveSecond.txt")));
            assertTrue(!Files.exists(IOUtils.resolveFilePath("moveSecond.txt")));
        } catch (MvException e) {
            fail("should not fail:" + e);
        }
    }


}
