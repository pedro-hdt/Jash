package integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class FindIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    private OutputStream stdout;
    private ShellImpl shell;
    private String cmdLine = "";
    private String expected = "";

    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
            + StringUtils.fileSeparator() + "dummyTestFolder"
            + StringUtils.fileSeparator() + "IntegrationTestFolder"
        + StringUtils.fileSeparator() + "FindIntegrationTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setup() {
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
            + StringUtils.fileSeparator() + "dummyTestFolder"
            + StringUtils.fileSeparator() + "IntegrationTestFolder"
            + StringUtils.fileSeparator() + "FindIntegrationTestFolder");
        stdout.flush();
    }

    @Test
    public void testFindThenRm() throws ShellException, AbstractApplicationException, IOException {
        Path path = Files.createFile(Paths.get(Environment.getCurrentDirectory(),  "Test-folder-1", "temporaryFile.txt"));
        cmdLine ="find Test-folder-1 -name temporaryFile.txt; cd Test-folder-1; rm temporaryFile.txt";
        expected = "Test-folder-1/temporaryFile.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
        assertFalse(Files.exists(path));
    }

    @Test
    public void testFindThenGrep() throws ShellException, AbstractApplicationException {
        cmdLine = "find Test-folder-1 -name textfile.txt; cd Test-folder-1; grep -c PATTERN textfile.txt";
        expected = "Test-folder-1/textfile.txt" + StringUtils.STRING_NEWLINE
        + "0" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testFindThenPaste() throws ShellException, AbstractApplicationException {
        cmdLine = "find Test-folder-1 -name textfile.txt ; cd Test-folder-1 ; paste textfile.txt";
        expected = "Test-folder-1/textfile.txt" + StringUtils.STRING_NEWLINE
            + "pattern" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testInvalidFindWithValidCommand() throws ShellException, AbstractApplicationException {
        cmdLine = "find Not-Present-Folder -name textfile.txt ; ls";
        expected = "find: Not-Present-Folder: No such file or directory" + StringUtils.STRING_NEWLINE
            + "Test-folder-1" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testValidFindWithInvalidCommand() throws ShellException, AbstractApplicationException {
        cmdLine = "find Test-folder-1 -name textfile.txt ; cd test-folder-1";
        expected = "Test-folder-1/textfile.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
}
