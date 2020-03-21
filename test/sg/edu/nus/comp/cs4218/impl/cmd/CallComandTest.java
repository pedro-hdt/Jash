package sg.edu.nus.comp.cs4218.impl.cmd;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Tests for the call commands
 * <p>
 * Application runner is mocked such that the actual applications are not used
 * <p>
 * Positive test cases:
 * - evaluate "echo hello" command
 * <p>
 * Negative test cases:
 * - attempt to evaluate an invalid command
 * - attempt to evaluate a command with null args
 */
public class CallComandTest {

    public CallCommand cmd;
    static ByteArrayOutputStream stdout;

    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    private static ApplicationRunner mockAppRunner = mock(ApplicationRunner.class);


    @BeforeAll
    static void setupAll() {
        // This is kept in case more test cases are added with commands that change the directory
        stdout = new ByteArrayOutputStream();
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
        cmd = null;
        stdout.reset();
    }

    @Test
    public void testEchoCall() throws ShellException, AbstractApplicationException {

        String hello = "hello";
        String expectedResult = hello + STRING_NEWLINE;

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws IOException {
                stdout.write(expectedResult.getBytes());
                return null;
            }
        }).when(mockAppRunner).runApp("echo", new String[]{hello}, System.in, stdout);

        cmd = new CallCommand(
                Arrays.asList("echo", hello),
                mockAppRunner,
                new ArgumentResolver());
        cmd.evaluate(System.in, stdout);

        assertEquals(expectedResult, stdout.toString());

        verify(mockAppRunner).runApp("echo", new String[]{hello}, System.in, stdout);

    }


    @Test
    public void testFailsInvalidCommandCall() throws ShellException, AbstractApplicationException {

        String invalidCommand = "invalid";

        doThrow(new ShellException(invalidCommand + ": " + ERR_INVALID_APP))
                .when(mockAppRunner).runApp(eq(invalidCommand), any(String[].class), eq(System.in), eq(stdout));

        cmd = new CallCommand(
                Arrays.asList(invalidCommand),
                mockAppRunner,
                new ArgumentResolver());

        ShellException exception = assertThrows(ShellException.class, () -> cmd.evaluate(System.in, stdout));

        assertMsgContains(exception, ERR_INVALID_APP);

        verify(mockAppRunner).runApp(eq(invalidCommand), any(String[].class), eq(System.in), eq(stdout));

    }

    @Test
    public void testFailsNullArgs() throws ShellException {

        cmd = new CallCommand(
                null,
                new ApplicationRunner(),
                new ArgumentResolver());

        ShellException exception = assertThrows(ShellException.class, () -> cmd.evaluate(System.in, stdout));

        assertMsgContains(exception, ERR_SYNTAX);

    }

}
