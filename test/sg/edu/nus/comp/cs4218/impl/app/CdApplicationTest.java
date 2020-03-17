package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;


/**
 * Provides unit tests of CdApplication
 * <p>
 * method mkdir(parent) used to create directories and schedule them
 * to be deleted automatically
 * <p>
 * Positive test cases:
 * - cd with a single argument
 * - cd with multiple arguments (should ignore all but first)
 * - cd without any arguments (should have no effect)
 * - cd with not an absolute path
 * <p>
 * Negative test cases:
 * - cd with in-existent directory (should throw no such dir exception)
 * - cd with null arg
 * - cd with file instead of directory (should throw an exception)
 */
class CdApplicationTest {

    public static CdApplication cdApp;
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    /**
     * Creates a directory inside the given one and schedules it to be deleted one exit
     * Facilitates cleaning up
     *
     * @param parent directory inside which to create the new one
     */
    public static Path mkdir(Path parent) throws IOException {
        Path dir = Files.createTempDirectory(parent, "cdTest");
        dir.toFile().deleteOnExit();
        return dir;
    }

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "CdTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setCd() {
        cdApp = new CdApplication();
    }

    @AfterEach
    public void resetCurrentDirectory() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "CdTestFolder");
    }


    /**
     * Call cd with null args for run()
     */
    @Test
    public void testNullArgs() {
        Exception exception = assertThrows(CdException.class, () -> cdApp.run(null, System.in, System.out));

        assertMsgContains(exception, ERR_NULL_ARGS);

    }
    /**
     * Calls cd with a single argument
     */
    @Test
    public void testSingleArg() throws IOException, CdException {

        // create directory inside the initial directory
        Path testDir = mkdir(Paths.get(Environment.currentDirectory));

        // change to newly created dir
        cdApp.run(new String[]{testDir.toString()}, System.in, System.out);

        // verify effect of change
        assertEquals(testDir.toString(), Environment.currentDirectory);

    }

    /**
     * Attempt to call cd with multiple arguments
     * Assumption: only first argument is considered and all others ignored
     */
    @Test
    public void testMultArgs() throws IOException, CdException {

        // create 2 directories in current
        Path testDir1 = mkdir(Paths.get(Environment.currentDirectory));
        Path testDir2 = mkdir(Paths.get(Environment.currentDirectory));

        cdApp.run(new String[]{testDir1.toString(), testDir2.toString()}, System.in, System.out);
        assertEquals(testDir1.toString(), Environment.currentDirectory);

    }

    /**
     * Call cd to a directory 2 levels below:
     * e.g. if pwd prints `/home/test` and we call `cd /home/test/outer/inner/`
     */
    @Test
    public void testTwoLevelsDown() throws IOException, CdException {

        // create a dir with another nested inside
        Path outer = mkdir(Paths.get(Environment.currentDirectory));
        Path inner = mkdir(outer);

        cdApp.run(new String[]{inner.toString()}, System.in, System.out);
        assertEquals(inner.toString(), Environment.currentDirectory);
    }

    /**
     * Call cd without args. Should have no effect
     * NOTE: Ignore cause EF1 bug. Run again after changing to @Test
     */
    @Test
    public void testNoArgs() throws CdException {

        String dirBefore = Environment.getCurrentDirectory();

        cdApp.run(new String[]{}, System.in, System.out);
        assertEquals(dirBefore, Environment.getCurrentDirectory());
    }

    /**
     * Call cd to a non existent directory
     * Assumption: should throw a CdException containing the text in ERR_FILE_NOT_FOUND
     */
    @Test
    public void testFailsNonExistentDirectory() throws IOException {

        // Create a directory, save its name, then delete it
        Path testDir = mkdir(Paths.get(Environment.currentDirectory));
        String dirName = testDir.toString();
        Files.delete(testDir);

        CdException exception = assertThrows(CdException.class, () -> cdApp.run(new String[]{dirName}, System.in, System.out));
        assertMsgContains(exception, ERR_FILE_NOT_FOUND);
    }

    /**
     * Call cd to a non directory
     * Assumption: should throw a CdException containing the text in ERR_IS_NOT_DIR
     */
    @Test
    public void testFailsWhenNotADirectory() {

        CdException exception = assertThrows(CdException.class, () -> cdApp.run(new String[]{"randomFile.txt"}, System.in, System.out));
        assertMsgContains(exception, ERR_IS_NOT_DIR);
    }

    /**
     * Call cd to a non absolute path
     */
    @Test
    public void testNonAbsolutePath() throws CdException {
        String dirBefore = Environment.getCurrentDirectory();

        cdApp.run(new String[]{"directory"}, System.in, System.out);
        assertEquals(dirBefore + StringUtils.fileSeparator() + "directory", Environment.getCurrentDirectory());

    }

}