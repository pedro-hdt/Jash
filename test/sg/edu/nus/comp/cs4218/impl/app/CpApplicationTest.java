package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;

/**
 * Tests for cp command
 *
 * <p>
 * Contains negative and positive test cases testing cp in isolation
 * </p>
 * <p>
 * In GNU cp, the behavior regarding directories is that they are not copied unless the
 * -R flag is specified. Since there is no mention of this flag in the spec, for the sake of simplicity,
 * we assume that this cp utility does not support copying directories.
 * </p>
 * <p>
 * To verify that the copying process succeeds and we indeed observe a copy, we use two randomly
 * generated files as test assets, and hash the files with SHA-256, and compare their digests.
 * </p>
 * <p>
 * Some other assumptions were made regarding the messages of the exceptions thrown in negative test
 * cases. Those are specified below, in their respective documentation.
 * </p>
 * <p>
 * Negative
 * - Single argument (no destination provided) throws exception
 * - Copying a file to its current directory
 * - Copying a non existent file
 * <p>
 * Positive
 * - Copy single file into another directory
 * - Copy single file into another non-existent file
 * - Copy single file into another existent file
 * - Copy multiple files into another directory
 */
class CpApplicationTest {

    private static CpApplication cpApp;

    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();


    /**
     * Hashes two files using SHA-256 and returns whether the hashes match
     *
     * @return Path to directory created
     * @throws IOException
     */
    public static boolean hashesMatch(Path file1, Path file2) throws IOException {

        byte[] hash1 = null, hash2 = null;
        try {

            // SHA-256 is probably overkill but fine with such small files
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(Files.readAllBytes(file1));
            hash1 = md.digest();

            md.reset();

            md.update(Files.readAllBytes(file2));
            hash2 = md.digest();

        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }

        return Arrays.equals(hash1, hash2);
    }


    @BeforeAll
    public static void setUp() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "CpTestFolder");
    }


    @BeforeEach
    public void init() {
        cpApp = new CpApplication();
    }


    @AfterAll
    public static void cleanUp() throws IOException {
        for (File f : (new File("destDir")).listFiles()) {
            f.delete();
        }
        Files.delete(IOUtils.resolveFilePath("inexistent2"));
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }


    /**
     * Call cp with only a filename
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_NO_ARGS string
     */
    @Test
    public void testFailsWithSingleArg() {

        CpException e =
                assertThrows(CpException.class, () -> cpApp.run(new String[0], System.in, System.out));

        assertTrue(e.getMessage().contains(ERR_NO_ARGS));

    }


    /**
     * Attempts to copy a file to the same directory it is in
     * Assumption: should fail and throw an exception with "same file" in its message
     */
    @Test
    public void testFailsSingleFileToSameDir() {

        String[] args = {"src1", Environment.getCurrentDirectory()};
        CpException e =
                assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));

        // In UNIX cp prints "<FILE> and <FILE> are the same file" so we
        // assume this replicates such behavior
        assertTrue(e.getMessage().contains("same file"));

    }


    /**
     * Attempts to copy a non existent file
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_FILE_NOT_FOUND string
     */
    @Test
    public void testFailsNonexistentFile() {

        String[] args = {"inexistent1", "destDir"};
        CpException e =
                assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));

        assertTrue(e.getMessage().contains(ERR_FILE_NOT_FOUND));

    }


    /**
     * Copy file to another directory
     */
    @Test
    public void testSingleFileToOtherDir() throws CpException, IOException {

        Path src = IOUtils.resolveFilePath("src1");
        Path dest = IOUtils.resolveFilePath("destDir/src1");

        String[] args = {src.toString(), "destDir"};
        cpApp.run(args, System.in, System.out);

        assertTrue(Files.exists(dest));
        assertTrue(hashesMatch(src, dest));

    }


    /**
     * Copy file into another new (non-existent) file
     */
    @Test
    public void testSingleFileToNewFile() throws IOException, AbstractApplicationException {

        Path src = IOUtils.resolveFilePath("src1");
        Path dest = IOUtils.resolveFilePath("inexistent2");

        String[] args = {src.toString(), dest.toString()};
        cpApp.run(args, System.in, System.out);

        assertTrue(Files.exists(dest));
        assertTrue(hashesMatch(src, dest));

    }


    /**
     * Copy single file into another existent file, overwriting it
     */
    @Test
    public void testSingleFileToExistentFile() throws IOException, AbstractApplicationException {

        Path src = IOUtils.resolveFilePath("src1");
        Path dest = IOUtils.resolveFilePath("destFile");

        String[] args = {src.toString(), dest.toString()};
        cpApp.run(args, System.in, System.out);

        assertTrue(hashesMatch(src, dest));

    }


    /**
     * Copy multiple files into another directory
     */
    @Test
    public void testMultFilesToDir() throws IOException, AbstractApplicationException {

        Path src1 = IOUtils.resolveFilePath("src1");
        Path src2 = IOUtils.resolveFilePath("src2");
        Path dest = IOUtils.resolveFilePath("destDir");

        String[] args = {src1.toString(), src2.toString(), dest.toString()};
        cpApp.run(args, System.in, System.out);

        Path copy1 = IOUtils.resolveFilePath("destDir/src1");
        Path copy2 = IOUtils.resolveFilePath("destDir/src2");

        assertTrue(Files.exists(copy1));
        assertTrue(Files.exists(copy2));
        assertTrue(hashesMatch(src1, copy1));
        assertTrue(hashesMatch(src2, copy2));

    }

}