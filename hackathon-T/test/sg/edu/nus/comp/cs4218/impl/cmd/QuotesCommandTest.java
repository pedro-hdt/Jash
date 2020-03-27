package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.TestUtils;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * Tests Quotes in commands (double and single quotes)
 */
public class QuotesCommandTest {

    public ShellImpl shell;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "QuotesCommandTest");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Quotes test with invalid nesting with unmatched double quotes
     */
    @Test
    public void testInvalidCommandQuotes() {
        Exception exception = assertThrows(ShellException.class, ()
          -> shell.parseAndEvaluate("echo \" \"hi\"", stdout));

        TestUtils.assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * Fails when no double Quoting for escaping characters
     */
    @Test
    public void testFailsNoQuotingForFileNameWithSpaces() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls file with spaces.txt ", stdout);

        assertTrue(stdout.toString().contains("cannot access 'file': No such file or directory"));
        assertTrue(stdout.toString().contains("cannot access 'with': No such file or directory"));
        assertTrue(stdout.toString().contains("cannot access 'spaces.txt': No such file or directory"));

    }

    /**
     * Quoting helps escape characters
     */
    @Test
    public void testQuotingForFileNameWithSpaces() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("ls \"file with spaces.txt\"", stdout);

        assertTrue(stdout.toString().contains("file with spaces.txt"));

    }


    /**
     * Double quotes disable interpretation of special symbols except backquote
     */
    @Test
    public void testInterpretationOfSpace() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is space:`echo \" \"`.\"", stdout);

        assertEquals("This is space: .", stdout.toString());
        assertTrue(stdout.toString().contains("This is space: ."));
    }


    /**
     * Double quotes interprets back quotes
     */
    @Test
    public void testDoubleQuotesWithBackQuote() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is random:`echo \"random\"`.\"", stdout);

        assertTrue(stdout.toString().contains("This is random:random."));
    }

    /**
     * Fails if unclosed back quote in argument
     */
    @Test
    public void testBackQuoteWithoutSingleQuote() {
        Exception exception = assertThrows(ShellException.class, () -> shell.parseAndEvaluate("echo  he`llo", stdout));

        TestUtils.assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * Single quotes doesn't interpret back quotes
     */
    @Test
    public void testSingleQuoteIgnoresBackQuote() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo 'hello `backquote'", stdout);

        assertTrue(stdout.toString().contains("hello `backquote"));
    }

    /**
     * Single quotes disable interpretation of backquote
     */
    @Test
    public void testSingleQuotesDoesntSubstituteCmd() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo 'This works:`echo \" \"`.'", stdout);

        assertTrue(stdout.toString().contains("This works:`echo \" \"`."));
    }

    /**
     * Single quotes disable interpretation of double quotes
     */
    @Test
    public void testSingleQuotesIgnoresDouble() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo '\"This works\":`echo \" \"`.'", stdout);

        assertTrue(stdout.toString().contains("\"This works\":`echo \" \"`."));
    }

    /**
     * Single quotes inside double quotes
     */
    @Test
    public void testSingleQuotesInDouble() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \" hap'p'y\"", stdout);

        assertTrue(stdout.toString().contains(" hap'p'y"));
    }

    /**
     * Single quotes and Double quotes ignore special Chars
     */
    @Test
    public void testSpecialCharsIngored() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \" |h<a>*p'p'\ty;\"", stdout);

        assertTrue(stdout.toString().contains(" |h<a>*p'p'\ty;"));
    }


}
