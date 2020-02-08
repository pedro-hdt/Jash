package impl.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class RmApplicationTest {

    // TODO Pedro: is there any problem with having a single rm app instance
    public static RmApplication rm = new RmApplication();

    /**
     * Remove a single simple file
     */
    @Test
    public void rmFile() throws IOException, RmException { // TODO how to handle exceptions in the tests?

        // create a temporary file and make sure it exists
        File testFile = File.createTempFile("rmFile", "test");
        Assertions.assertTrue(testFile.exists()); // TODO is this necessary?

        // assemble args and call rm
        String[] args = {testFile.getPath()};
        rm.run(args, System.in, System.out);

        // make sure file no longer exists afterwards
        Assertions.assertFalse(testFile.exists());
    }


    /**
     * Remove an empty folder with the -d flag
     */
    @Test
    public void rmEmptyFolderDirectory() throws IOException, RmException {

        // create a temporary directory and make sure it exists
        Path testDir = Files.createTempDirectory("rmEmptyFolderDirectory");

        // assemble args and call rm
        String[] args = {"-d", testDir.toString()};
        rm.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        Assertions.assertFalse(testDir.toFile().exists());
    }

    /**
     * Attempt to remove an empty folder <b>without</b> the -d flag
     * Should not remove folder
     */
    @Test
    public void rmEmptyFolderAttempt() throws IOException {

        // create a temporary directory
        Path testDir = Files.createTempDirectory("rmEmptyFolderAttempt");

        // assemble args and call rm expecting an exception
        String[] args = {testDir.toString()};
        Exception e = Assertions.assertThrows(RmException.class, () -> {
            rm.run(args, System.in, System.out);
        });

        // verify it was the correct exception
        Assertions.assertTrue(e.getMessage().contains(ERR_IS_DIR));

        // make sure directory still exists afterwards
        Assertions.assertTrue(testDir.toFile().exists());

        // cleanup
        testDir.toFile().delete();

    }

    /**
     * Attempt to remove a non-empty folder <b>with without the -d flag</b>, but no -r flag
     * Should not remove the folder
     */
    @Test
    public void rmNonEmptyFolderAttempt() throws IOException {

        final String testSignature = "rmNonEmptyFolderAttempt";

        // create a temporary directory and make sure it exists
        Path testDir = Files.createTempDirectory(testSignature);

        // create a file inside the temporary directory
        File testFile = File.createTempFile(testSignature, "testfile", testDir.toFile());

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

        // verify the correct exceptions were thrown
        String expectedMsg = ERR_IS_DIR;
        Assertions.assertTrue(e1.getMessage().contains(expectedMsg));
        Assertions.assertTrue(e2.getMessage().contains(expectedMsg));

        // make sure directory and file still exist afterwards
        Assertions.assertTrue(testFile.exists());
        Assertions.assertTrue(testDir.toFile().exists());

        // cleanup
        testFile.delete();
        testDir.toFile().delete();

    }


    /**
     * Remove a non empty folder recursively
     */
    @Test
    public void rmNonEmptyFolderRecursive() throws IOException, RmException {

        // create 4 levels of nested temporary directories
        Path l1TestDir = Files.createTempDirectory("rmNonEmptyFolderAttempt");
        Path l2TestDir = Files.createTempDirectory(l1TestDir, "rmNonEmptyFolderAttempt");
        Path l3TestDir = Files.createTempDirectory(l2TestDir, "rmNonEmptyFolderAttempt");
        Path l4TestDir = Files.createTempDirectory(l3TestDir, "rmNonEmptyFolderAttempt");

        // create files at each level in the tree
        File.createTempFile("rmNonEmptyFolderAttempt", "testfile", l4TestDir.toFile());
        File.createTempFile("rmNonEmptyFolderAttempt", "testfile", l3TestDir.toFile());
        File.createTempFile("rmNonEmptyFolderAttempt", "testfile", l2TestDir.toFile());
        File.createTempFile("rmNonEmptyFolderAttempt", "testfile", l1TestDir.toFile());

        // assemble args and call rm to delete the outer directory recursively
        String[] args = {"-r", l1TestDir.toString()};
        rm.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        Assertions.assertFalse(l1TestDir.toFile().exists());

    }

    /**
     * Remove an empty folder recursively
     */
    @Test
    public void rmEmptyFolderRecursive() throws IOException, RmException {

        // create a temporary directory
        Path testDir = Files.createTempDirectory("rmNonEmptyFolderAttempt");

        // assemble args and call rm to delete the directory recursively
        String[] args = {"-r", testDir.toString()};
        rm.run(args, System.in, System.out);

        // make sure directory no longer exists afterwards
        Assertions.assertFalse(testDir.toFile().exists());

    }

    /**
     * Remove a list of files and folders empty or non empty with -r flag
     */
    @Test
    public void rmMultFilesAndDirsRD() throws IOException, RmException {

        List<File> filesAndDirs = new LinkedList<>();
        final String testSignature = "rmMultFilesAndDirs";

        // populate list of files
        filesAndDirs.add(Files.createTempDirectory(testSignature).toFile());
        filesAndDirs.add(File.createTempFile(testSignature, ""));
        filesAndDirs.add(Files.createTempDirectory(testSignature).toFile());
        filesAndDirs.add(Files.createTempDirectory(testSignature).toFile());
        filesAndDirs.add(File.createTempFile(testSignature, ""));

        // put some files inside the directories to be deleted
        File.createTempFile(testSignature, "", filesAndDirs.get(2));
        File.createTempFile(testSignature, "", filesAndDirs.get(3));

        // assemble args and call rm to delete all recursively
        List<String> args = new LinkedList<>();
        args.add("-r");
        args.addAll(filesAndDirs.stream().map(File::getPath).collect(Collectors.toList()));
        rm.run(args.toArray(new String[0]), System.in, System.out);

        // check all files are gone
        Boolean allGone = filesAndDirs.stream().noneMatch(File::exists);
        Assertions.assertTrue(allGone);

    }

}
