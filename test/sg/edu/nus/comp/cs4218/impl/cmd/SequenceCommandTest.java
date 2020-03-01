package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * Tests commands used in sequence
 */
public class SequenceCommandTest {
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
        CallCommand echoCommand = new CallCommand(new ArrayList<>(Arrays.asList("echo", "hi")), new ApplicationRunner(), new ArgumentResolver());

        sequenceCommand = new SequenceCommand(new ArrayList<>(Arrays.asList(echoCommand, exitCommand)));
        Exception exception = assertThrows(ExitException.class, () -> sequenceCommand.evaluate(System.in, stdout));

        assertTrue(stdout.toString().contains("hi"));
        assertTrue(exception.toString().contains("terminating"));

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
        CallCommand echoCommand1 = new CallCommand(new ArrayList<>(Arrays.asList("echo", "hello")), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCommand2 = new CallCommand(new ArrayList<>(Arrays.asList("echo", "hi")), new ApplicationRunner(), new ArgumentResolver());

        sequenceCommand = new SequenceCommand(new ArrayList<>(Arrays.asList(echoCommand1, echoCommand2)));
        sequenceCommand.evaluate(System.in, stdout);

        assertTrue(stdout.toString().contains("hello" + StringUtils.STRING_NEWLINE + "hi"));

    }
}
