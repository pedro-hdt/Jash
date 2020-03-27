package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.TestUtils;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {
    
    private static final String FILE1 = "pasteFile1.txt";
    private static final String FILE2 = "pasteFile2.txt";
    private static final String FILES1AND2 = "pasteFiles1and2.txt";
    private static final String FILE1_2COLS = "pasteFile1-2cols.txt";
    private static final String FILE1_2COLSAND2 = "pasteFile1-2colsand2.txt";
    private static final String EMPTY_FILE = "empty.txt";
    
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    
    private static PasteApplication paste;
    private static ByteArrayInputStream stdin;
    private static ByteArrayOutputStream stdout;
    
    @BeforeAll
    public static void setUp() {
        stdout = new ByteArrayOutputStream();
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "PasteTestFolder");
    }
    
    @BeforeEach
    public void init() {
        paste = new PasteApplication();
        stdout.reset();
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        stdout.close();
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    /**
     * Call paste without any arguments
     * Assumption: should fail with insufficient arguments exception
     */
    @Test
    public void testNoArgs() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[0], System.in, stdout));
    
        assertMsgContains(exception, ERR_NO_ARGS);
    
    }
    
    /**
     * Call paste with null args
     */
    @Test
    public void testNullArgs() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(null, System.in, stdout));
    
        assertMsgContains(exception, ERR_NULL_ARGS);
    
    }
    
    /**
     * Call paste with null input stream
     */
    @Test
    public void testNullInputStream() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[0], null, stdout));
    
        assertMsgContains(exception, ERR_NO_ISTREAM);
    
    }
    
    /**
     * Call paste with null output stream
     */
    @Test
    public void testNullOutputStream() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[0], System.in, null));
    
        assertMsgContains(exception, ERR_NO_OSTREAM);
    
    }
    
    /**
     * Call paste with null a directory as argument
     */
    @Test
    public void testPasteDirOnlyFiles() throws IOException {
    
        Path dir = Files.createDirectory(IOUtils.resolveFilePath("dir")); //NOPMD
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[]{"dir"}, System.in, System.out));
    
        Files.delete(dir);
        assertMsgContains(exception, ERR_IS_DIR);
    
    }
    
    /**
     * Call paste with a directory and stdin as args
     */
    @Test
    public void testPasteDirFilesAndStdIn() throws IOException {
    
        Path dir = Files.createDirectory(IOUtils.resolveFilePath("dir"));
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[]{"dir", "-"}, System.in, System.out));
    
        Files.delete(dir);
        assertMsgContains(exception, ERR_IS_DIR);
    
    }
    
    /**
     * Call paste with nonexistent file
     */
    @Test
    public void testNonexistentFile() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[]{"fakefile"}, System.in, System.out));
    
        assertMsgContains(exception, ERR_FILE_NOT_FOUND);
    
    }
    
    /**
     * Call paste with nonexistent file and stdin
     */
    @Test
    public void testNonexistentFileAndStdin() {
    
        PasteException exception =
          assertThrows(PasteException.class, () -> paste.run(new String[]{"fakefile", "-"}, System.in, System.out));
    
        assertMsgContains(exception, ERR_FILE_NOT_FOUND);
    
    }
    
    /**
     * Test paste app whether output exception is thrown when there is an IOException
     */
    @Test
    void testWritingResultToOutputStreamException() {
        try {
            OutputStream baos = TestUtils.getMockExceptionThrowingOutputStream();//NOPMD
    
            paste.run(new String[]{FILE1}, System.in, baos);
            fail("Exception expected");
        } catch (PasteException e) {
            assertEquals("paste: " + ERR_WRITE_STREAM, e.getMessage());
        }
    }
    
    /**
     * Call paste with single file arg
     * Should print the file followed by a new line
     */
    @Test
    public void testSingleFileArg() throws PasteException, IOException {
    
        paste.run(new String[]{FILE1}, System.in, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with two file args
     * Should behave like GNU paste
     */
    @Test
    public void testTwoFileArgs() throws PasteException, IOException {
    
        paste.run(new String[]{FILE1, FILE2}, System.in, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILES1AND2))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with single stdin (-) arg
     */
    @Test
    public void pasteSingleStdinArgs() throws PasteException, IOException {
    
        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(FILE1)));
    
        paste.run(new String[]{"-"}, stdin, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with two stdin (-) args
     */
    @Test
    public void pasteTwoStdinArgs() throws PasteException, IOException {
    
        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(FILE1)));
    
        paste.run(new String[]{"-", "-"}, stdin, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1_2COLS))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with 1 stdin (-) arg and one file arg
     */
    @Test
    public void pasteOneFileOneStdinArgs() throws PasteException, IOException {
    
        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(FILE2)));
    
        paste.run(new String[]{FILE1, "-"}, stdin, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILES1AND2))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with 2 stdin (-) args and one file arg
     */
    @Test
    public void pasteOneFileTwoStdinArgs() throws PasteException, IOException {
    
        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(FILE1)));
    
        paste.run(new String[]{"-", "-", FILE2}, stdin, stdout);
    
        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(FILE1_2COLSAND2))) + STRING_NEWLINE,
          stdout.toString());
    
    }
    
    /**
     * Call paste with an empty file as arg
     */
    @Test
    public void testEmptyFile() throws PasteException, IOException {
    
        paste.run(new String[]{EMPTY_FILE}, System.in, stdout);
    
        assertEquals(STRING_NEWLINE, stdout.toString());
    
    }
    
}
