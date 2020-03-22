package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

public class CdIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    private OutputStream stdout;
    private ShellImpl shell;
    private String cmdLine = "";
    private String expected = "";

    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
            + StringUtils.fileSeparator() + "dummyTestFolder"
            + StringUtils.fileSeparator() + "IntegrationTestFolder");
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
            + StringUtils.fileSeparator() + "IntegrationTestFolder");
        stdout.flush();
    }

    @Test
    public void testCdThenGrep() throws AbstractApplicationException, ShellException {
        cmdLine = "cd SequencingFolder; grep -c Sometime textFile.txt";
        expected = "1" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testCdThenLs() throws AbstractApplicationException, ShellException {
        cmdLine = "cd GlobbingFolder; ls";
        expected = "dir" + StringUtils.STRING_NEWLINE
            + "wc1.txt" + StringUtils.STRING_NEWLINE
            + "wc100.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testCdThenFind() throws AbstractApplicationException, ShellException {
        cmdLine = "cd GlobbingFolder; find dir -name empty.txt";
        expected = "dir" + StringUtils.fileSeparator() + "empty.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testCdThenRm() throws AbstractApplicationException, ShellException, IOException {
        Path path = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "SequencingFolder", "temporaryFile" +
            ".txt"));
        cmdLine = "cd SequencingFolder; rm temporaryFile.txt";
        expected = "1" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertFalse(Files.exists(path));
    }

    @Test
    public void testCdThenPaste() throws AbstractApplicationException, ShellException {
        cmdLine = "cd ShellCommandFolder; paste old.txt";
        expected = "15" + StringUtils.STRING_NEWLINE
            + "2" + StringUtils.STRING_NEWLINE
            + "100" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testCdThenWc() throws AbstractApplicationException, ShellException {
        cmdLine = "cd SequencingFolder; wc emptyFile.txt";
        expected = "       0       0       0 emptyFile.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
}
