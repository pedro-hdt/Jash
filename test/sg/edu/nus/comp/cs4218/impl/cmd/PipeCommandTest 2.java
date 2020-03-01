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
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REP_RULE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * Tests for Pipe operator used in combining commands
 */
public class PipeCommandTest {

    public static final String ECHO_CMD = "echo";
    public PipeCommand pipeCommand;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + File.separator + "dummyTestFolder" + File.separator + "PipeTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        pipeCommand = null;
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Asserts if error message printed when ShellException thrown
     */
    @Test
    public void testThrowsShellExceptionForEmptyPipedCommand() {
        CallCommand invalidCmd = new CallCommand(new ArrayList<>(), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCmd = new CallCommand(Arrays.asList(ECHO_CMD, "hi"), new ApplicationRunner(), new ArgumentResolver());

        pipeCommand = new PipeCommand(Arrays.asList(invalidCmd, echoCmd));

        Exception exception = assertThrows(ShellException.class, () -> pipeCommand.evaluate(System.in, stdout));
        TestUtils.assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * Asserts if error message printed when AbstractApplicationException thrown
     */
    @Test
    public void testThrowsCmdExceptionForInvalidCmd() {
        CallCommand invalidSedCmd = new CallCommand(Arrays.asList("sed", "s|he|"), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCmd = new CallCommand(Arrays.asList(ECHO_CMD, "hi"), new ApplicationRunner(), new ArgumentResolver());

        pipeCommand = new PipeCommand(Arrays.asList(invalidSedCmd, echoCmd));

        Exception exception = assertThrows(AbstractApplicationException.class, () -> pipeCommand.evaluate(System.in, stdout));
        TestUtils.assertMsgContains(exception, ERR_INVALID_REP_RULE);
    }

    /**
     * Test to filter out only files which have filtered in their name
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testLsWithGrepUsingPipe() throws AbstractApplicationException, ShellException {
        CallCommand lsCommand = new CallCommand(new ArrayList<>(Collections.singleton("ls")), new ApplicationRunner(), new ArgumentResolver());
        CallCommand grepCommand = new CallCommand(new ArrayList<>(Arrays.asList("grep", "filtered")), new ApplicationRunner(), new ArgumentResolver());

        pipeCommand = new PipeCommand(new ArrayList<>(Arrays.asList(lsCommand, grepCommand)));
        pipeCommand.evaluate(System.in, stdout);

        assertTrue(stdout.toString().contains("filtered.txt"));
        assertFalse(stdout.toString().contains("random.txt"));

    }

    /**
     * Test to echo a string by piping the input
     *
     * @throws AbstractApplicationException
     * @throws ShellException
     */
    @Test
    public void testEchoCommandsWithPipe() throws AbstractApplicationException, ShellException {
        CallCommand echoCmd1 = new CallCommand(new ArrayList<>(Collections.singleton(ECHO_CMD)), new ApplicationRunner(), new ArgumentResolver());
        CallCommand echoCmd2 = new CallCommand(new ArrayList<>(Arrays.asList(ECHO_CMD, "hello")), new ApplicationRunner(), new ArgumentResolver());

        pipeCommand = new PipeCommand(new ArrayList<>(Arrays.asList(echoCmd1, echoCmd2)));
        pipeCommand.evaluate(System.in, stdout);

        assertTrue(stdout.toString().contains("hello"));

    }

    /**
     * Test integration with parser
     */
    @Test
    public void testSequenceOnShell() throws Exception {

        ShellImpl shell = new ShellImpl();
        shell.parseAndEvaluate("echo hello | echo hi", stdout);

        assertTrue(stdout.toString().contains("hi"));

    }

}
