package integration;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class QuoteShellIntegrationTest {
    private ShellImpl shell = new ShellImpl();
    private String cmdline = "";
    private String expected = "";
    private ByteArrayOutputStream output = new ByteArrayOutputStream();;

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
}
