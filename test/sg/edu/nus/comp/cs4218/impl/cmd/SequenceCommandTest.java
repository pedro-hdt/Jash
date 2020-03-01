package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.TestUtils;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Tests commands used in sequence
 */
public class SequenceCommandTest {
    public static final String ECHO_CMD = "echo";
    public SequenceCommand sequenceCommand;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "SequenceTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        sequenceCommand = null;
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Throws exit exception if one of them is exit command
     */
    @Test
    public void testExitCommandWithEcho() {
        CallCommand exitCommand = new CallCommand(new ArrayList<>(Arrays.asList("exit")), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCommand = new CallCommand(new ArrayList<>(Arrays.asList(ECHO_CMD, "shout")), new ApplicationRunner(), new ArgumentResolver());

        sequenceCommand = new SequenceCommand(new ArrayList<>(Arrays.asList(echoCommand, exitCommand)));
        Exception exception = assertThrows(ExitException.class, () -> sequenceCommand.evaluate(System.in, stdout));

        assertTrue(stdout.toString().contains("shout"));
        assertTrue(exception.toString().contains("terminating"));

    }

    /**
     * Throws ShellException when command starts with ;
     */
    @Test
    public void testExceptionWhenNewCmdWithSemicolon() {

        ShellImpl shell = new ShellImpl();
        Exception exception = assertThrows(ShellException.class, () -> shell.parseAndEvaluate(";", stdout));

        TestUtils.assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * Asserts if exception thrown when invalid commands are piped
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testThrowsShellExceptionForEmptyPipedCommand() throws AbstractApplicationException, ShellException {
        CallCommand invalidCmd = new CallCommand(new ArrayList<>(), new ApplicationRunner(), new ArgumentResolver());

        sequenceCommand = new SequenceCommand(Arrays.asList(invalidCmd));

        sequenceCommand.evaluate(System.in, stdout);
        assertTrue(stdout.toString().contains(ERR_SYNTAX));
    }

    /**
     * Test to echo two different strings
     */
    @Test
    public void testSameCommandsInSequence() throws Exception {
        CallCommand echoCommand1 = new CallCommand(new ArrayList<>(Arrays.asList(ECHO_CMD, "abc")), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCommand2 = new CallCommand(new ArrayList<>(Arrays.asList(ECHO_CMD, "def")), new ApplicationRunner(), new ArgumentResolver());

        sequenceCommand = new SequenceCommand(new ArrayList<>(Arrays.asList(echoCommand1, echoCommand2)));
        sequenceCommand.evaluate(System.in, stdout);

        assertTrue(stdout.toString().contains("abc" + StringUtils.STRING_NEWLINE + "def"));

    }

    /**
     * Test integration with parser
     */
    @Test
    public void testSequenceOnShell() throws Exception {

        ShellImpl shell = new ShellImpl();
        shell.parseAndEvaluate("echo hello; echo hi", stdout);

        assertTrue(stdout.toString().contains("hello" + StringUtils.STRING_NEWLINE + "hi"));

    }


    /**
     * Test integration of sequence with pipe
     */
    @Test
    public void testSequenceWithPipeOnShell() throws Exception {

        ShellImpl shell = new ShellImpl();
        shell.parseAndEvaluate("echo piping; ls | grep present", stdout);

        assertTrue(stdout.toString().contains("piping" + StringUtils.STRING_NEWLINE + "present.txt"));

    }

    /**
     * Test integration on shell
     */
    @Test
    public void testPipeWithSequence() throws Exception {

        ShellImpl shell = new ShellImpl();
        shell.parseAndEvaluate("ls | grep present ; echo boy", stdout);

        assertTrue(stdout.toString().contains("present.txt" + StringUtils.STRING_NEWLINE + "boy"));

    }
}
