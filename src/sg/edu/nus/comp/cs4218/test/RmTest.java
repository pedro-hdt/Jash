package sg.edu.nus.comp.cs4218.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class RmTest {

    public static RmApplication rm = new RmApplication();

    /**
     * Remove a single simple file
     */
    @Test
    public void rmFile() throws IOException, RmException { // TODO how to handle exceptions in the tests?

        // create a temporary file and make sure it exists
        File testFile = File.createTempFile("rmFile", "test");
        Assertions.assertTrue(testFile.exists());

        // assemble args and call rm
        String[] args = {testFile.getPath()};
        rm.run(args, System.in, System.out);

        // make sure directory still exists afterwards
        Assertions.assertTrue(testFile.exists()); // TODO is this overkill?
    }

    /**
     * Attempt to remove an empty folder without the -d flag
     */
    @Test
    public void rmEmptyFolderDirectory() throws IOException, RmException {

        // create a temporary directory and make sure it exists
        Path testDir = Files.createTempDirectory("rmEmptyFolderDirectory");
        Assertions.assertTrue(testDir.toFile().exists());

        // assemble args and call rm
        String[] args = {"-d", testDir.toString()};
        rm.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        Assertions.assertFalse(testDir.toFile().exists());
    }

    /**
     * Remove an empty folder with the -d flag
     */
    @Test
    public void rmEmptyFolderAttempt() throws IOException {

        // create a temporary directory and make sure it exists
        Path testDir = Files.createTempDirectory("rmEmptyFolderAttempt");
        Assertions.assertTrue(testDir.toFile().exists());

        // assemble args and call rm expecting an exception
        String[] args = {testDir.toString()};
        Exception e = Assertions.assertThrows(RmException.class, () -> {
            rm.run(args, System.in, System.out);
        });

        // verify it was the correct exception
        String expectedMsg = ERR_IS_DIR;
        Assertions.assertTrue(e.getMessage().contains(expectedMsg));

        // make sure directory still exists afterwards
        Assertions.assertTrue(testDir.toFile().exists());

        // delete it
        testDir.toFile().delete(); // TODO maybe requeste deleteOnExit() or maybe just delete all at the end

    }

    /**
     * Attempt to remove a non-empty folder with AND without the -d flag, but no -r flag
     */
    @Test
    public void rmNonEmptyFolderAttempt() throws IOException {

        // create a temporary directory and make sure it exists
        Path testDir = Files.createTempDirectory("rmNonEmptyFolderAttempt");
        Assertions.assertTrue(testDir.toFile().exists());

        // create a file inside the temporary directory
        File.createTempFile("rmNonEmptyFolderAttempt", "testfile", testDir.toFile());

        // assemble args and call rm expecting an exception
        String[] args1 = {testDir.toString()};
        Exception e1 = Assertions.assertThrows(RmException.class, () -> {
            rm.run(args1, System.in, System.out);
        });

        // assemble args again, this time with the -d flag and call rm expecting an exception
        String[] args2 = {"-d", testDir.toString()};
        Exception e2 = Assertions.assertThrows(RmException.class, () -> {
            rm.run(args2, System.in, System.out);
        });

        // verify the correct exception were thrown
        String expectedMsg = ERR_IS_DIR;
        Assertions.assertTrue(e1.getMessage().contains(expectedMsg));
        Assertions.assertTrue(e2.getMessage().contains(expectedMsg));

        // make sure file still exists afterwards
        Assertions.assertTrue(testDir.toFile().exists());

    }


    /**
     * Remove a non empty folder recursively
     */
    @Test
    public void rmNonEmptyFolderRecursive() {

    }

    /**
     * Remove a list of files and folders
     */
    @Test
    public void rmMultFilesAndDirectories() {

    }

    /**
     * Cleans up all temporary files generated during the tests
     */
    @AfterAll
    public void cleanUp() {

    }

}
