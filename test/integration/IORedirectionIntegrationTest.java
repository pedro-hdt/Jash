package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class IORedirectionIntegrationTest {

    private static ShellImpl shell;
    private static ByteArrayOutputStream out;
    private static final String FILE1 = "intermediateFile1.txt";
    private static final String FILE2 = "intermediateFile2.txt";


    @BeforeAll
    public static void setUp() throws IOException {
        shell = new ShellImpl();
        out = new ByteArrayOutputStream();
        Files.createFile(IOUtils.resolveFilePath(FILE1));
        Files.createFile(IOUtils.resolveFilePath(FILE2));
    }

    @AfterEach
    public void finalize() {
        out.reset();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        Files.delete(IOUtils.resolveFilePath(FILE1));
        Files.delete(IOUtils.resolveFilePath(FILE2));
    }

    @Test
    @DisplayName("echo \"hello\" > file ; paste - < file")
    public void testSimpleIORedir1() throws AbstractApplicationException, ShellException {

        shell.parseAndEvaluate(String.format("echo \"hello\" > %s ; paste - < %1$s", FILE1), out);
        assertEquals("hello" + STRING_NEWLINE, out.toString());

    }

    @Test
    @DisplayName("ls > file ; paste - < file")
    public void testSimpleIORedir2() throws AbstractApplicationException, ShellException, IOException {

        String ORIGINAL_DIR = Environment.getCurrentDirectory();
        Environment.setCurrentDirectory(Environment.getCurrentDirectory()
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "LsTestFolder");

        shell.parseAndEvaluate(String.format("ls > %s ; paste - < %1$s", FILE1), out);
        assertTrue(out.toString().contains("textfile.txt"));
        Files.delete(IOUtils.resolveFilePath(FILE1));

        Environment.setCurrentDirectory(ORIGINAL_DIR);

    }

    @Test
    @DisplayName("echo \"hello\" > file1 ; sed s/hello/goodby/ < file1 > file2 ; grep good < file2")
    public void testDoubleIORedir1() throws AbstractApplicationException, ShellException {

        shell.parseAndEvaluate(
                String.format("echo \"hello\" > %s ; sed s/hello/goodbye/ < %1$s > %s ; grep good < %2$s", FILE1, FILE2), out);
        assertTrue(out.toString().contains("goodbye"));

    }

    @Test
    @DisplayName("echo \"hello\" > file1 ; paste - < file1 > file2 ; wc -c < file2")
    public void testDoubleIORedir2() throws AbstractApplicationException, ShellException {

        shell.parseAndEvaluate(
                String.format("echo \"hello\" > %s ; paste - < %1$s > %s ; wc -c < %2$s", FILE1, FILE2), out);
        assertTrue(out.toString().contains("6"));

    }

    @Test
    @DisplayName("echo \"hello\" > file ; invalid < file")
    public void testRedirInputToInvalidApp() throws AbstractApplicationException, ShellException {

        shell.parseAndEvaluate(String.format("echo \"hello\" > %s ; invalid < %1$s", FILE1), out);
        assertTrue(out.toString().contains(ERR_INVALID_APP));

    }

    @Test
    @DisplayName("invalid > file")
    public void testRedirOutputOfInvalidApp() {

        ShellException shellException = assertThrows(ShellException.class,
                () -> shell.parseAndEvaluate(String.format("invalid > %s", FILE1), out));
        assertMsgContains(shellException, ERR_INVALID_APP);

    }

}
