package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {

    private static final String PASTE_FILE1 = "pasteFile1.txt";
    private static final String PASTE_FILE2 = "pasteFile2.txt";
    private static final String PASTE_FILES1AND2 = "pasteFiles1and2.txt";
    private static final String PASTE_FILE1_2COLS = "pasteFile1-2cols.txt";
    private static final String PASTE_FILE1_2COLSAND2 = "pasteFile1-2colsand2.txt";

    private static String ORIGINAL_DIR;

    private static PasteApplication paste;
    private static ByteArrayInputStream stdin;
    private static ByteArrayOutputStream stdout;

    @BeforeAll
    public static void setUp() {
        stdout = new ByteArrayOutputStream();
        ORIGINAL_DIR = Environment.getCurrentDirectory();
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + File.separator + "dummyTestFolder"
                + File.separator + "PasteTestFolder");
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
     * Call paste with single file arg
     * Should print the file followed by a new line
     */
    @Test
    public void testSingleFileArg() throws PasteException, IOException {

        paste.run(new String[]{PASTE_FILE1}, System.in, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with two file args
     * Should behave like GNU paste
     */
    @Test
    public void testTwoFileArgs() throws PasteException, IOException {

        paste.run(new String[]{PASTE_FILE1, PASTE_FILE2}, System.in, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILES1AND2))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with single stdin (-) arg
     */
    @Test
    public void pasteSingleStdinArgs() throws PasteException, IOException {

        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1)));

        paste.run(new String[]{"-"}, stdin, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with two stdin (-) args
     */
    @Test
    public void pasteTwoStdinArgs() throws PasteException, IOException {

        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1)));

        paste.run(new String[]{"-", "-"}, stdin, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1_2COLS))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with 1 stdin (-) arg and one file arg
     */
    @Test
    public void pasteOneFileOneStdinArgs() throws PasteException, IOException {

        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE2)));

        paste.run(new String[]{PASTE_FILE1, "-"}, stdin, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILES1AND2))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with 2 stdin (-) args and one file arg
     */
    @Test
    public void pasteOneFileTwoStdinArgs() throws PasteException, IOException {

        stdin = new ByteArrayInputStream(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1)));

        paste.run(new String[]{"-", "-", PASTE_FILE2}, stdin, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1_2COLSAND2))) + STRING_NEWLINE,
                stdout.toString());

    }

}
