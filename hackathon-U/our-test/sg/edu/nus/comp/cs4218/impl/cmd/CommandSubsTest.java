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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

/**
 * Tests command substitution with integrating commands
 * <p>
 * Command Substitution is done in ArgsResolver and is evaluated in it's private method called evaluateSubCommand
 * The private method can't be mock using mockito.
 * Hence, the tests here will use echo command for all command substitution assuming echo application is bug free.
 */
public class CommandSubsTest {

    public ShellImpl shell;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.currentDirectory;

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + StringUtils.fileSeparator() + "dummyTestFolder" + StringUtils.fileSeparator() + "CommandSubsTest");
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
     * SubCommands with invalid nesting with unmatched back quotes
     */
    @Test
    public void testInvalidCommandSubs() {
        Exception exception = assertThrows(ShellException.class, ()
          -> shell.parseAndEvaluate("echo `ls echo `test1.txt`", stdout));

        TestUtils.assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * SubCommands with same command inside
     */
    @Test
    public void testSimpleSubCommand() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo `echo hello`", stdout);

        assertTrue(stdout.toString().contains("hello"));
    }

    /**
     * SubCommands with substitution of one output as input to another command
     */
    @Test
    public void testCommandSubs() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo `ls`", stdout);

        assertTrue(stdout.toString().contains("test1.txt"));
    }

    /**
     * SubCommands with nested substitution
     */
    @Test
    public void testCommandSubsNested() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo `ls echo `test1.txt``", stdout);

        assertTrue(stdout.toString().contains("test1.txt"));
    }

    /**
     * SubCommands with nested substitution with same commands
     */
    @Test
    public void testSameCommandSubsNested() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo `echo `echo hello``", stdout);

        assertTrue(stdout.toString().contains("echo hello"));
    }


}
