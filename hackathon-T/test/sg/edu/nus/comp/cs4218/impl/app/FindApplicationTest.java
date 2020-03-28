package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.FindException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.FindApplication.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;

/**
 * Test Suite for find command
 * <p>
 * Contains negative and positive test cases
 */
class FindApplicationTest {


    public static final String FOLDER1 = "Test-folder-1";
    public static final String FOLDER2 = "Test-folder-2";
    public static final String TEXTFILE = "textfile.txt";
    public static final String TESTFILE = "test.txt";
    public static final String NAME_FLAG = "-name";
    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private static FindApplication findApplication;
    private static OutputStream stdout;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "FindTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }


    @BeforeEach
    void setUp() {
        findApplication = new FindApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Try find command with no arguments
     */
    @Test
    public void testFindWithNullArguments() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(null, System.in,
          stdout));
        assertTrue(expectedException.getMessage().contains(NULL_POINTER));
    }

    /**
     * Try find command with null stdout
     */
    @Test
    public void testFindWithNullOutputStream() {
        Exception expectedException = assertThrows(FindException.class, ()
          -> findApplication.run(new String[]{FOLDER1, NAME_FLAG, TEXTFILE}, System.in, null));
        assertTrue(expectedException.getMessage().contains("output stream is null"));
    }

    /**
     * When folder with no read permission is passed
     *
     * @throws IOException
     */
    @Test
    public void testInvalidUnreadableFile() throws IOException {

        Path path = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), "unreadable"));
        File file = path.toFile();
        file.setReadable(false);

        Exception exception = assertThrows(Exception.class, ()
          -> findApplication.findFolderContent(TEXTFILE, path.toString()));
        assertEquals(exception.getMessage(), "find: Permission Denied");

        file.setReadable(true);
        file.delete();
    }

    /**
     * Try find command with empty arguments
     */
    @Test
    public void testFindWithEmptyArguments() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{""},
          System.in, stdout));
        ;
        assertTrue(expectedException.getMessage().contains("Arguments should not be empty"));
    }

    /**
     * Try find command with wrong flag suffix
     */
    @Test
    public void testFindWithWrongFlagSuffix() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{"A",
          "-i", "test"}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(WRONG_FLAG_SUFFIX));
    }

    /**
     * Try find command with only folder
     */
    @Test
    public void testFindWithOnlyFolder() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{FOLDER1}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(NO_FILE));
    }

    /**
     * Try findFolderContentWithNullFile
     */
    @Test
    public void testFindFolderContentNullFile() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.findFolderContent(null, FOLDER1));
        assertTrue(expectedException.getMessage().contains(NO_FILE));
    }

    /**
     * Try findFolderContentWithNullFile
     */
    @Test
    public void testFindFolderContentNullFolder() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.findFolderContent("file1.txt", null));
        assertTrue(expectedException.getMessage().contains(NO_FOLDER));
    }

    /**
     * Try find command with no flag
     */
    @Test
    public void testFindWithNoFlag() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{FOLDER1, TESTFILE}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(NO_FILE));
    }

    /**
     * Try find command with no specified file name
     */
    @Test
    public void testFindWitNoFileSpecified() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{FOLDER1, NAME_FLAG}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(NO_FILE));
    }

    /**
     * Try find command with no specified folder name
     */
    @Test
    public void testFindWithNoFolderSpecified() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{
          NAME_FLAG, TESTFILE}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(NO_FOLDER));
    }

    /**
     * Try find command with wrong folder name
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testFindWithWrongFolderName() throws AbstractApplicationException {
        findApplication.run(new String[]{"Test-folder-10", NAME_FLAG, TESTFILE}, System.in, stdout);
        assertTrue(stdout.toString().contains(ERR_FILE_NOT_FOUND));
    }


    /**
     * Try find command with no matching file name
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testFindWithNoMatchingFile() throws AbstractApplicationException {
        findApplication.run(new String[]{FOLDER1, NAME_FLAG, TESTFILE}, System.in, stdout);
        assertEquals("", stdout.toString());
    }

    /**
     * Try find command with multiple file names
     */
    @Test
    public void testFindWithMultipleFileNames() {
        Exception expectedException = assertThrows(FindException.class, () -> findApplication.run(new String[]{FOLDER2, NAME_FLAG, "Test-folder-2-2", TEXTFILE}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(MULTIPLE_FILES));
    }

    /**
     * Try find command with correct input
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testFindWithMatchingFile() throws AbstractApplicationException {
        findApplication.run(new String[]{FOLDER1, NAME_FLAG, TEXTFILE}, System.in, stdout);
        assertEquals(FOLDER1 + StringUtils.CHAR_FILE_SEP + TEXTFILE + StringUtils.STRING_NEWLINE,
          stdout.toString());
    }

    /**
     * Try find command with correct input found recursively
     *
     * @throws AbstractApplicationException
     */
    @Test
    public void testFindWithMatchingFileRecursively() throws AbstractApplicationException {
        findApplication.run(new String[]{FOLDER2, NAME_FLAG, TEXTFILE}, System.in, stdout);
        assertEquals(FOLDER2 + StringUtils.CHAR_FILE_SEP
          + "Test-folder-2-2" + StringUtils.CHAR_FILE_SEP + "Test-folder-2-3" + StringUtils.CHAR_FILE_SEP
          + TEXTFILE + StringUtils.STRING_NEWLINE, stdout.toString());
    }
}
