package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class QuoteShellIntegrationTest {
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    private ShellImpl shell = new ShellImpl();
    private String cmdline = "";
    private String expected = "";
    private ByteArrayOutputStream output = new ByteArrayOutputStream();;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "QuoteShellFolder");

    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "QuoteShellFolder");

        output.flush();
    }

    @Test
    public void testDoubleQuoteOutputRedirection()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest > test" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest \"> test\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteOutputRedirection()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest > test" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest '> test'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithTab() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'\\t test\'";
        String expected = "\\t test" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testDoubleQuotesInSingleQuote()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest \"quoteshelltest\" quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo 'quoteshelltest \"quoteshelltest\" quoteshelltest'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteInDoubleQuotes()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest 'quoteshelltest' quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo \"quoteshelltest 'quoteshelltest' quoteshelltest\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithPipe() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'| quoteshelltest\'";
        String expected = "| quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithInputRedirection() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'< quoteshelltest\'";
        String expected = "< quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithOutputRedirection() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'> quoteshelltest\'";
        String expected = "> quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithSemicolon() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'; quoteshelltest\'";
        String expected = "; quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testDoubleQuoteWithGlobbing() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \"* quoteshelltest\"";
        String expected = "* quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        String actual = output.toString();

        assertEquals(expected, actual);
    }

    @Test
    void testDoubleQuoteWithBacktick() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \" `echo \"quoteshelltest\"`\"";
        String expected = " quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        String actual = output.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testDoubleQuotesEscaped()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest ; echo quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest \"; echo quoteshelltest\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWithSemicolonBeside()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest ; echo quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest '; echo quoteshelltest'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    void testSingleQuoteWithGlobbing() throws AbstractApplicationException, ShellException {
        String cmdLine = "echo \'* quoteshelltest\'";
        String expected = "* quoteshelltest" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, output);
        String actual = output.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testDoubleQuoteWithPipeAndCommand()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest| grep quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest\"| grep quoteshelltest\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWithPipeAndCommand()
            throws ShellException, AbstractApplicationException {
        expected = "quoteshelltest| grep quoteshelltest" + STRING_NEWLINE;
        cmdline = "echo quoteshelltest'| grep quoteshelltest'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testDoubleQuoteWrappedWithGlobbing()
            throws ShellException, AbstractApplicationException {
        expected = "*" + STRING_NEWLINE;
        cmdline = "echo \"*\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWrappedWithGlobbing()
            throws ShellException, AbstractApplicationException {
        expected = "*" + STRING_NEWLINE;
        cmdline = "echo '*'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testDoubleQuoteWithCut()
            throws ShellException, AbstractApplicationException {
        expected = "qe" + STRING_NEWLINE;
        cmdline = "cut -c 1,8 \"quoteshelltest.txt\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWithCut()
            throws ShellException, AbstractApplicationException {
        expected = "qe" + STRING_NEWLINE;
        cmdline = "cut -c 1,8 \'quoteshelltest.txt\'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testDoubleQuoteWithDiff()
            throws ShellException, AbstractApplicationException {
        expected = "";
        cmdline = "diff \"quoteshelltest.txt\" \"quoteshelltest-identical.txt\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWithDiff()
            throws ShellException, AbstractApplicationException {
        expected = "";
        cmdline = "diff \'quoteshelltest.txt\' \'quoteshelltest-identical.txt\'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testDoubleQuoteWithWc()
            throws ShellException, AbstractApplicationException {
        expected = "       0 quoteshelltest.txt" + STRING_NEWLINE;
        cmdline = "wc -l \"quoteshelltest.txt\"";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

    @Test
    public void testSingleQuoteWithWc()
            throws ShellException, AbstractApplicationException {
        expected = "       0 quoteshelltest.txt" + STRING_NEWLINE;
        cmdline = "wc -l \'quoteshelltest.txt\'";

        shell.parseAndEvaluate(cmdline, output);
        assertEquals(expected, output.toString());
    }

}