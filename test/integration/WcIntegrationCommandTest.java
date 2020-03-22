package integration;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class WcIntegrationCommandTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    private OutputStream stdout;
    private ShellImpl shell;
    private String cmdline = "";
    private String expected = "";

    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "WcIntegrationFolder");
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
                + StringUtils.fileSeparator() + "WcIntegrationFolder");
        stdout.flush();
    }

    @Test
    public void testWcThenEcho() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE +
                "wctest.txt" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt; echo wctest.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenEchoNegative() throws ShellException, AbstractApplicationException {
        expected = "wc: Invalid flag option supplied" + STRING_NEWLINE +
                "wctest.txt" + STRING_NEWLINE;
        cmdline = "wc -lz wctest.txt; echo wctest.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenGrep() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt | grep 2";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenGrepNegative() throws ShellException, AbstractApplicationException {
        expected = "";
        cmdline = "wc -l wctest.txt | grep ABC";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenCut() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE +
                "1" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt; cut -b 2 wctest.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenCutNegative() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE +
                "cut: Out of range" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt; cut -b 0 wctest.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenFind() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE +
                "../WcIntegrationFolder/wctest.txt" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt; find ../ -name wctest.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void testWcThenFindNegative() throws ShellException, AbstractApplicationException {
        expected = "       2 wctest.txt" + STRING_NEWLINE;
        cmdline = "wc -l wctest.txt; find ../ -name wcteste.txt";

        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
}
