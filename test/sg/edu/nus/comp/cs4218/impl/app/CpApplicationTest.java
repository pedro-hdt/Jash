package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

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

    private static final String FILE_B_TXT = "fileb.txt";
    private static final String DIR_A = "dirA";
    private static CpApplication cpApp;
    private static final String DEST_DIR = "destDir";
    private static final String SRC1 = "src1";
    private static final String SRC2 = "src2";
    private static final String DEST_FILE = "destFile";
    
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
            MessageDigest msgDigest = MessageDigest.getInstance("SHA-256");
    
            msgDigest.update(Files.readAllBytes(file1));
            hash1 = msgDigest.digest();
    
            msgDigest.reset();
    
            msgDigest.update(Files.readAllBytes(file2));
            hash2 = msgDigest.digest();
    
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }
    
        return Arrays.equals(hash1, hash2);
    }
    
    
    @BeforeEach
    public void init() {
        cpApp = new CpApplication();
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "CpTestFolder");
    }
    
    @AfterEach
    public void resetCurrentDirectory() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    @AfterAll
    public static void cleanUp() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "CpTestFolder");
        for (File f : IOUtils.resolveFilePath(DEST_DIR).toFile().listFiles()) {
            if (!f.getName().equals("emptyFile.txt")) {
                f.delete();
            }
        }
        Files.delete(IOUtils.resolveFilePath("inexistent2"));
        PrintWriter writer = new PrintWriter(IOUtils.resolveFilePath(DEST_FILE).toFile()); //NOPMD
        writer.print("");
        writer.close(); // empty the destination file so validation is meaningful in following runs
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    
    /**
     * Call cp without args
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_NO_ARGS string
     */
    @Test
    public void testFailsWithNoArg() {
    
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(new String[0], System.in, System.out));
    
        assertMsgContains(cpException, ERR_NO_ARGS);
    
    }
    
    
    /**
     * Call cp with only a filename
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_NO_ARGS string
     */
    @Test
    public void testFailsWithSingleArg() {
    
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(new String[]{"someFileName"}, System.in, System.out));
    
        assertMsgContains(cpException, ERR_NO_ARGS);
    
    }
    
    
    /**
     * Attempts to copy a file to the same directory it is in
     * Assumption: should fail and throw an exception with "same file" in its message
     */
    @Test
    public void testFailsSingleFileToSameDir() {
    
        String[] args = {SRC1, Environment.getCurrentDirectory()};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        // In UNIX cp prints "<FILE> and <FILE> are the same file" so we
        // assume this replicates such behavior
        assertMsgContains(cpException, "same file");
    
    }
    
    
    /**
     * Attempts to copy a non existent file into a folder
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_FILE_NOT_FOUND string
     */
    @Test
    public void testFailsNonexistentFileToFolder() {
    
        String[] args = {"nonexistent1", DEST_DIR};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        assertMsgContains(cpException, ERR_FILE_NOT_FOUND);
    
    }
    
    /**
     * Attempts to copy a non existent file into another file
     * Assumption: the exception thrown uses the text in the ErrorConstants.ERR_FILE_NOT_FOUND string
     */
    @Test
    public void testFailsNonexistentFileToFile() {
    
        String[] args = {"nonexistent1", DEST_FILE};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        assertMsgContains(cpException, ERR_FILE_NOT_FOUND);
    
    }
    
    /**
     * Attempts to copy a file into itself
     */
    @Test
    public void testFailsFileToItself() {
    
        String[] args = {SRC1, SRC1};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        assertMsgContains(cpException, "same file");
    
    }
    
    /**
     * Attempts to copy an nonexistent file to another new file
     */
    @Test
    public void testFailsNonexistentFileToNewFile() {
    
        String[] args = {"inexistent1", "newfile"};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        assertMsgContains(cpException, ERR_FILE_NOT_FOUND);
    
    }
    
    /**
     * Call cp with null outstream
     */
    @Test
    public void testFailsWithNullOutputStream() {
    
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(new String[0], System.in, null));
    
        assertMsgContains(cpException, ERR_NO_OSTREAM);
    
    }
    
    /**
     * Call cp with null args
     */
    @Test
    public void testFailsWithNullArgs() {
    
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(null, System.in, System.out));
    
        assertMsgContains(cpException, ERR_NULL_ARGS);
    
    }
    
    /**
     * Call cp with multiple files into single existing one
     */
    @Test
    public void testFailsWithMultIntoSingle() {
    
        String[] args = {SRC1, SRC2, DEST_FILE};
        CpException cpException =
          assertThrows(CpException.class, () -> cpApp.run(args, System.in, System.out));
    
        assertMsgContains(cpException, "is not a directory");
    
    }
    
    
    /**
     * Copy file to another directory
     */
    @Test
    public void testSingleFileToOtherDir() throws CpException, IOException {
    
        Path src = IOUtils.resolveFilePath(SRC1);
        Path dest = IOUtils.resolveFilePath("destDir/src1");
    
        String[] args = {src.toString(), DEST_DIR};
        cpApp.run(args, System.in, System.out);
    
        assertTrue(Files.exists(dest));
        assertTrue(hashesMatch(src, dest));
    
    }
    
    
    /**
     * Copy file into another new (non-existent) file
     */
    @Test
    public void testSingleFileToNewFile() throws IOException, AbstractApplicationException {
    
        Path src = IOUtils.resolveFilePath(SRC1);
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
    
        Path src = IOUtils.resolveFilePath(SRC1);
        Path dest = IOUtils.resolveFilePath(DEST_FILE);
    
        String[] args = {src.toString(), dest.toString()};
        cpApp.run(args, System.in, System.out);
    
        assertTrue(hashesMatch(src, dest));
    
    }
    
    
    /**
     * Copy multiple files into another directory
     */
    @Test
    public void testMultFilesToDir() throws IOException, AbstractApplicationException {
    
        Path src1 = IOUtils.resolveFilePath(SRC1);
        Path src2 = IOUtils.resolveFilePath(SRC2);
        Path dest = IOUtils.resolveFilePath(DEST_DIR);
    
        String[] args = {src1.toString(), src2.toString(), dest.toString()};
        cpApp.run(args, System.in, System.out);
    
        Path copy1 = IOUtils.resolveFilePath("destDir/src1");
        Path copy2 = IOUtils.resolveFilePath("destDir/src2");
    
        assertTrue(Files.exists(copy1));
        assertTrue(Files.exists(copy2));
        assertTrue(hashesMatch(src1, copy1));
        assertTrue(hashesMatch(src2, copy2));
    
    }

    @Test
    void testMoveSameFolderWithAnotherValidFile() throws IOException {

        Files.createFile(IOUtils.resolveFilePath(FILE_B_TXT));
        Files.createDirectory(IOUtils.resolveFilePath(DIR_A));

        String dirA = DIR_A;
        try {
            String[] args = {dirA, FILE_B_TXT, dirA};
            cpApp.run(args, System.in, System.out);
            fail();
        } catch (CpException e) {
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, DIR_A, FILE_B_TXT)));
            assertEquals("cp: " + StringUtils.STRING_NEWLINE +
                    "dirA skipped: 'dirA' and 'dirA' are the same file", e.getMessage());
        } finally {
            Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), DIR_A, FILE_B_TXT));
            Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory()
                    + StringUtils.fileSeparator() + DIR_A));
            Files.deleteIfExists(Paths.get(Environment.getCurrentDirectory(), FILE_B_TXT));
        }
    }
    
}