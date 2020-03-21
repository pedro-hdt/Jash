package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
// We argue that in this case having duplicate literals improve readability significantly
class CommandBuilderTest {

    /**
     * Tests parsing of the call command "echo hello world" and checks if the correct tokens
     * and structure was created
     *
     * @throws ShellException
     */
    @Test
    void testParseBasicCommand() throws ShellException {

        Command cmd = CommandBuilder.parseCommand("echo hello world", new ApplicationRunner());

        assertTrue(cmd instanceof CallCommand);
        CallCommand callCommand = (CallCommand) cmd;
        assertEquals(Arrays.asList("echo", "hello", "world"), callCommand.getArgsList());

    }


    /**
     * Tests parsing of the sequence command "echo hello ; echo world" and checks if the correct tokens
     * and structure was created
     *
     * @throws ShellException
     */
    @Test
    void testParseSequenceCommand() throws ShellException {

        Command cmd = CommandBuilder.parseCommand("echo hello ; echo world", new ApplicationRunner());

        assertTrue(cmd instanceof SequenceCommand);
        SequenceCommand sequenceCommand = (SequenceCommand) cmd;

        List<Command> cmdList = sequenceCommand.getCommands();

        // validate size
        assertEquals(2, cmdList.size());

        // validate args of first command
        assertTrue(cmdList.get(0) instanceof CallCommand);
        CallCommand callCommand0 = (CallCommand) cmdList.get(0);
        assertEquals(Arrays.asList("echo", "hello"), callCommand0.getArgsList());

        // validate args of second command
        assertTrue(cmdList.get(1) instanceof CallCommand);
        CallCommand callCommand1 = (CallCommand) cmdList.get(1);
        assertEquals(Arrays.asList("echo", "world"), callCommand1.getArgsList());

    }


    /**
     * Tests parsing of the pipe command "echo hello  world | grep world" and checks if the correct tokens
     * and structure was created
     *
     * @throws ShellException
     */
    @Test
    void testParsePipeCommand() throws ShellException {

        Command cmd = CommandBuilder.parseCommand("echo hello  world | grep world", new ApplicationRunner());

        assertTrue(cmd instanceof PipeCommand);
        PipeCommand pipeCommand = (PipeCommand) cmd;

        List<CallCommand> cmdList = pipeCommand.getCallCommands();

        // validate size
        assertEquals(2, cmdList.size());

        // validate args of first command
        assertEquals(Arrays.asList("echo", "hello", "world"), cmdList.get(0).getArgsList());

        // validate args of second command
        assertEquals(Arrays.asList("grep", "world"), cmdList.get(1).getArgsList());

    }


    /**
     * Tests parsing a complex command with pipes and sequences
     *
     * @throws ShellException
     */
    @Test
    void testParseComplexPipeSeqCommand() throws ShellException {

        Command cmd = CommandBuilder.parseCommand(
                "echo hello ; echo world | grep w ; echo done",
                new ApplicationRunner());

        assertTrue(cmd instanceof SequenceCommand);
        SequenceCommand sequenceCommand = (SequenceCommand) cmd;

        List<Command> cmdList = sequenceCommand.getCommands();

        // validate size
        assertEquals(3, cmdList.size());

        // validate args of first command
        assertTrue(cmdList.get(0) instanceof CallCommand);
        CallCommand callCommand0 = (CallCommand) cmdList.get(0);
        assertEquals(Arrays.asList("echo", "hello"), callCommand0.getArgsList());

        // validate args of second command
        assertTrue(cmdList.get(1) instanceof PipeCommand);
        PipeCommand pipeCommand = (PipeCommand) cmdList.get(1);
        CallCommand callCommand1 = pipeCommand.getCallCommands().get(0);
        assertEquals(Arrays.asList("echo", "world"), callCommand1.getArgsList());
        CallCommand callCommand2 = pipeCommand.getCallCommands().get(1);
        assertEquals(Arrays.asList("grep", "w"), callCommand2.getArgsList());

        // validate args of third command
        assertTrue(cmdList.get(0) instanceof CallCommand);
        CallCommand callCommand3 = (CallCommand) cmdList.get(2);
        assertEquals(Arrays.asList("echo", "done"), callCommand3.getArgsList());

    }


    /**
     * Tests parsing a complex command with pipes and sequences and IO redirection
     * ```
     * echo "hello" > outfile ; grep 'he' < outfile ; echo done | paste -
     * ```
     *
     * @throws ShellException
     */
    @Test
    void testParseComplexPipeSeqIORedirCommand() throws ShellException, IOException {

        Path outFile = Files.createTempFile("outfile", "");

        Command cmd = CommandBuilder.parseCommand(
                "echo \"hello\" > " + outFile.toString() + " ; grep 'he' < " + outFile.toString() + " ; echo done | paste -",
                new ApplicationRunner());

        assertTrue(cmd instanceof SequenceCommand);
        SequenceCommand sequenceCommand = (SequenceCommand) cmd;

        List<Command> cmdList = sequenceCommand.getCommands();

        // validate size
        assertEquals(3, cmdList.size());

        // validate args of first command
        assertTrue(cmdList.get(0) instanceof CallCommand);
        CallCommand callCommand0 = (CallCommand) cmdList.get(0);
        assertEquals(Arrays.asList("echo", "\"hello\"", ">", outFile.toString()), callCommand0.getArgsList());

        // validate args of second command
        assertTrue(cmdList.get(1) instanceof CallCommand);
        CallCommand callCommand1 = (CallCommand) cmdList.get(1);
        assertEquals(Arrays.asList("grep", "\'he\'", "<", outFile.toString()), callCommand1.getArgsList());

        // validate args of third command
        assertTrue(cmdList.get(2) instanceof PipeCommand);
        PipeCommand pipeCommand = (PipeCommand) cmdList.get(2);
        CallCommand callCommand2 = pipeCommand.getCallCommands().get(0);
        assertEquals(Arrays.asList("echo", "done"), callCommand2.getArgsList());
        CallCommand callCommand3 = pipeCommand.getCallCommands().get(1);
        assertEquals(Arrays.asList("paste", "-"), callCommand3.getArgsList());

    }


    /**
     * Attempt to parse a command with a new line. Should throw an exception
     *
     * @throws ShellException
     */
    @Test
    void testFailsParseCommandWithNewLine() {

        ShellException shellException = assertThrows(
                ShellException.class,
                () -> CommandBuilder.parseCommand(
                        "echo hello " + STRING_NEWLINE + "world",
                        new ApplicationRunner())
        );

        assertMsgContains(shellException, ERR_SYNTAX);

    }

    /**
     * Attempt to parse an empty command. Should throw an exception
     *
     * @throws ShellException
     */
    @Test
    void testFailsParseEmptyCommand() {

        ShellException shellException = assertThrows(
                ShellException.class,
                () -> CommandBuilder.parseCommand("", new ApplicationRunner())
        );

        assertMsgContains(shellException, ERR_SYNTAX);

    }

    @Test
    @DisplayName("Automated Testing Tool")
    public void test0()  throws Throwable  {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand(";\"Y^^P~", applicationRunner0);
            fail("Expecting exception: Exception");

        } catch(Exception e) {
            assertTrue(e instanceof ShellException);
        }
    }

    @Test
    @DisplayName("Automated Testing Tool")
    public void test1()  throws Throwable  {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand("|.", applicationRunner0);
            fail("Expecting exception: Exception");

        } catch(Exception e) {
            assertTrue(e instanceof ShellException);

        }
    }

    @Test
    @DisplayName("Automated Testing Tool")
    public void test2()  throws Throwable  {
        try {
            CommandBuilder.parseCommand("IUG[*`A}Yr", (ApplicationRunner) null);
            fail("Expecting exception: Exception");

        } catch(Exception e) {
            assertTrue(e instanceof ShellException);

        }
    }

    @Test
    @DisplayName("Automated Testing Tool")
    public void test4()  throws Throwable  {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand("z>{!Fug@B!\"", applicationRunner0);
            fail("Expecting exception: Exception");
        } catch(Exception e) {
            assertTrue(e instanceof ShellException);

        }
    }

    @Test
    @DisplayName("Automated Testing Tool")
    public void test5()  throws Throwable  {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        Command command0 = CommandBuilder.parseCommand("we3[zf\u0005|/KX@I<}", applicationRunner0);
        assertNotNull(command0);
    }

}