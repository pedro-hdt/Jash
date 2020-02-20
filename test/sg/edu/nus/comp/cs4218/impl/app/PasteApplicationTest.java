package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {

    private static final String PASTE_FILE1 = "test/assets/pasteFile1.txt";
    private static final String PASTE_FILE2 = "test/assets/pasteFile2.txt";
    private static final String PASTE_FILES1AND2 = "test/assets/pasteFiles1and2.txt";
    private static final String PASTE_FILE1_2COLS = "test/assets/pasteFile1-2cols.txt";
    private static final String PASTE_FILE1_2COLSAND2 = "test/assets/pasteFile1-2colsand2.txt";


    private static PasteApplication paste;
    private static ByteArrayInputStream stdin;
    private static ByteArrayOutputStream stdout;

    @BeforeAll
    public static void setUp() {
        stdout = new ByteArrayOutputStream();
    }

    @BeforeEach
    public void init() {
        paste = new PasteApplication();
        stdout.reset();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        stdout.close();
    }

    /**
     * Call paste without any arguments
     */
    @Test
    public void pasteNoArgs() {

        PasteException expectedException =
                assertThrows(PasteException.class, () -> paste.run(new String[0], System.in, stdout));

        assertTrue(expectedException.getMessage().contains(ERR_NO_ARGS));

    }

    /**
     * Call paste with single file arg
     * Should print the file followed by a new line
     */
    @Test
    public void pasteSingleFileArg() throws PasteException, IOException {

        paste.run(new String[]{PASTE_FILE1}, System.in, stdout);

        assertEquals(new String(Files.readAllBytes(IOUtils.resolveFilePath(PASTE_FILE1))) + STRING_NEWLINE,
                stdout.toString());

    }

    /**
     * Call paste with two file args
     * Should behave like GNU paste
     */
    @Test
    public void pasteTwoFileArgs() throws PasteException, IOException {

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
