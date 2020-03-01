package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;


/**
 * Provides unit tests of CdApplication
 * <p>
 * method mkdir(parent) used to create directories and schedule them
 * to be deleted automatically
 * <p>
 * Positive test cases:
 * - cd with a single argument
 * - cd with multiple arguments (should ignore all but first)
 * <p>
 * Negative test cases:
 * - cd without any arguments (should throw no args exception)
 * - cd with inexistent directory (should throw no such dir exception)
 */
class CdApplicationTest {

    public static CdApplication cd;
    public static final String initDir = Environment.currentDirectory; // TODO is this problematic in any way?

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

    @BeforeEach
    public void setCd() {
        cd = new CdApplication();
    }

    @AfterEach
    public void resetCurrentDirectory() {
        Environment.currentDirectory = initDir;
    }

    /**
     * Calls cd with a single argument
     */
    @Test
    public void cdSingleArg() throws IOException, CdException {

        // create directory inside the initial directory
        Path testDir = mkdir(Paths.get(Environment.currentDirectory));

        // change to newly created dir
        cd.run(new String[]{testDir.toString()}, System.in, System.out);

        // verify effect of change
        assertEquals(testDir.toString(), Environment.currentDirectory);

    }

    /**
     * Attempt to call cd with multiple arguments
     * Assumption: only first argument is considered and all others ignored
     */
    @Test
    public void cdMultArgs() throws IOException, CdException {

        // create 2 directories in current
        Path testDir1 = mkdir(Paths.get(Environment.currentDirectory));
        Path testDir2 = mkdir(Paths.get(Environment.currentDirectory));

        cd.run(new String[]{testDir1.toString(), testDir2.toString()}, System.in, System.out);
        assertEquals(testDir1.toString(), Environment.currentDirectory);

    }

    /**
     * Call cd to a directory 2 levels below:
     * e.g. if pwd prints `/home/test` and we call `cd /home/test/outer/inner/`
     */
    @Test
    public void cdTwoLevelsDown() throws IOException, CdException {

        // create a dir with another nested inside
        Path outer = mkdir(Paths.get(Environment.currentDirectory));
        Path inner = mkdir(outer);

        cd.run(new String[]{inner.toString()}, System.in, System.out);
        assertEquals(inner.toString(), Environment.currentDirectory);
    }

    /**
     * Attempts to call cd without args
     * Assumption: this should throw an exception with the ERR_NO_ARGS text in the message
     */
    @Test
    public void cdNoArgs() {
        CdException e = assertThrows(CdException.class, () -> cd.run(new String[]{}, System.in, System.out));
        assertTrue(e.getMessage().contains(ERR_NO_ARGS));
    }

    /**
     * Call cd to a non existent directory
     * Assumption: should throw a CdException containing the text in ERR_FILE_NOT_FOUND
     */
    @Test
    public void cdNonExistentDirectory() throws IOException {

        // Create a directory, save its name, then delete it
        Path testDir = mkdir(Paths.get(Environment.currentDirectory));
        String dirName = testDir.toString();
        Files.delete(testDir);

        CdException e = assertThrows(CdException.class, () -> cd.run(new String[]{dirName}, System.in, System.out));
        assertTrue(e.getMessage().contains(ERR_FILE_NOT_FOUND));
    }

}