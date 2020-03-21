package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Invalid commands: 2
 *
 * Positive Single Subs:
 *
 * Positive Multiple subs:
 */
public class CommandSubsIntegrationTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private static ArgumentResolver argsResolver;

    @Mock
    private static ApplicationRunner appRunner;

    @Mock
    private static OutputStream mockOutStream;

    @Mock
    private static InputStream stdin;

    private OutputStream stdout;

    private ShellImpl shell = new ShellImpl();

    @BeforeAll
    static void setupAll() {

        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "CommandSubsFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setup() {
        argsResolver = new ArgumentResolver();
        MockitoAnnotations.initMocks(this);
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "IntegrationTestFolder"
                + StringUtils.fileSeparator() + "CommandSubsFolder");

        mockOutStream.flush();
        stdout.flush();
    }

    @Test
    @DisplayName("Invalid command inside the substitution")
    public void testInvalidCommand() {
        List<String> argsList = Arrays.asList("`invalid hi`");

        try {
            CallCommand invalidCmd = new CallCommand(argsList, appRunner, argsResolver);

            invalidCmd.evaluate(stdin, mockOutStream);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ShellException);
        }

    }

    /**
     * SubCommands with invalid nesting with unmatched back quotes
     */
    @Test
    public void testInvalidCommandSubs() {
        Exception exception = assertThrows(ShellException.class, ()
                -> shell.parseAndEvaluate("echo `ls echo `test1.txt`", stdout));

        assertMsgContains(exception, ERR_SYNTAX);
    }

    /**
     * SubCommands with sub cmd throwing exception
     */
    @Test
    public void testErrorFromSubsCmd() {
        Exception exception = assertThrows(CdException.class, ()
                -> shell.parseAndEvaluate("cd `ls *`", stdout));


        assertMsgContains(exception, ERR_TOO_MANY_ARGS);
    }

    @Test
    @DisplayName("Integration of single back quote with stub")
    public void testCommandSubsOfSingleCmd() throws ShellException {
        List<String> argsList = Arrays.asList("`echo hi`");
        CallCommand cmd = new CallCommand(argsList, appRunner, argsResolver);

        try {
            doAnswer(invocation -> {
                String[] parsedArgsList = invocation.getArgument(1);
                String actual = String.join(" ", parsedArgsList);

                assertEquals("", actual);
                return null;
            }).when(appRunner).runApp(any(), any(), any(), any());

            cmd.evaluate(stdin, mockOutStream);

        } catch (AbstractApplicationException e) {
            fail();
        }

    }


    @Test
    @DisplayName("Direct call to shell")
    public void testLsWithSort() {
        try {
            shell.parseAndEvaluate("ls `sort abc.txt`", stdout);

            assertEquals("def:" + StringUtils.STRING_NEWLINE + "empty.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @DisplayName("Direct call to shell with flag in command subs")
    public void testLsWithSortWithFlag() {
        try {
            shell.parseAndEvaluate("ls `sort abc.txt` -d", stdout);

            assertEquals("def:" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Integration of Ls and Cd. Action of Cd affects ls output.")
    public void testCdAndLs() {
        try{
            shell.parseAndEvaluate("ls `cd ../`", stdout);

            assertTrue(stdout.toString().contains("emptyFile.txt"));
            assertTrue(stdout.toString().contains("CommandSubsFolder"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Integration of rm and cp. Copies and then deletes")
    public void testRmAndCp(){
        try {
            shell.parseAndEvaluate("rm `cp copyFile.txt newFile.txt` newFile.txt", stdout);

            assertFalse(Files.exists(Paths.get(Environment.currentDirectory, "newFile.txt")));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Grep with Sort")
    public void testGrepWithSort() {
        try {

            shell.parseAndEvaluate("sort `grep hell findInFile.txt`", stdout);
            assertEquals("first" + StringUtils.STRING_NEWLINE + "second" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            fail();
        }
    }

    @Test
    @DisplayName("Paste with ls")
    public void testPasteWithLs() {
        try {
            shell.parseAndEvaluate("paste `ls x*.txt`", stdout);
            assertEquals("hi\tboy\n" +
                    "hello\tgirl" +  StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("exit in command subs")
    public void testExitWhenInCmdSubs() {
        try {
            shell.parseAndEvaluate("wc `exit`", stdout);
            fail();
        } catch (Exception e) {
            assertMsgContains(e, "exit: terminating execution");
        }
    }

    @Test
    @DisplayName("Sed with Wc. Sed changes content which wc should detect")
    public void testWcAfterSed() {
        try {
            shell.parseAndEvaluate("wc -w `sed \"s|abc|f|\" replaceFile.txt`", stdout);
            assertEquals("       5 f.txt" +  StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Find With Ls")
    public void testFindWithLs() {
        try {
            shell.parseAndEvaluate("ls `find dir -name file1*`", stdout);
            assertEquals("dir" + StringUtils.fileSeparator() + "file1.tad" +  StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Echo With Diff")
    public void testEchoWithDiff() {
        try {
            shell.parseAndEvaluate("echo `diff hella.txt hellz.txt`", stdout);
            assertEquals("< first > second" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Two commands in back quotes")
    public void testCutWithGrep() {
        try {

            shell.parseAndEvaluate("cut -c 1-3 `grep hella.txt findInFile.txt`", stdout);
            assertEquals("fir" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Nested back quotes same cmd with quotes")
    public void testNestedSameCmd() {
        try {

            shell.parseAndEvaluate("echo `echo \"'quote is not interpreted as special character'\"`", stdout);
            assertEquals("'quote is not interpreted as special character'" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Nested back quotes same cmd with quotes")
    public void testWithQuotes() {
        try {

            shell.parseAndEvaluate("echo '\"This is space `echo \" \"`\"'", stdout);
            assertEquals("\"This is space `echo \" \"`\"" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }


    @Test
    @DisplayName("Multiple commands in back quotes 1")
    public void testMultipleCommandsMvLsEcho() {
        try {

            Path path = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "toMove"));
            Path path2 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "newName"));


            shell.parseAndEvaluate("mv `ls toMove` `echo newName`", stdout);
            assertFalse(Files.exists(path));
            assertTrue(Files.exists(path2));

            path2.toFile().delete();

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Multiple commands in multiple back quotes")
    public void testCutWithDiffAndEcho() {
        try {

            shell.parseAndEvaluate("cut -c `echo 1-3` `grep hella.txt findInFile.txt`", stdout);
            assertEquals("fir" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Multiple commands in multiple back quotes")
    public void testMultCmds2() {
        try {

            shell.parseAndEvaluate("echo \"`wc -c hella.txt` - `wc -c hellz.txt`\"", stdout);
            assertEquals("       5 hella.txt -        6 hellz.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @DisplayName("Multiple commands in multiple back quotes")
    public void testMultCmds3() {
        try {

            shell.parseAndEvaluate("ls `cp toCopy.txt newToCopy.txt`", stdout);

            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, "newToCopy.txt")));

            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("newToCopy.txt")));
            String str2 = new String(Files.readAllBytes(IOUtils.resolveFilePath("toCopy.txt")));

            assertEquals(str1, str2);
            assertTrue(stdout.toString().contains("newToCopy.txt"));

            Files.delete(Paths.get(Environment.currentDirectory, "newToCopy.txt"));
        } catch (Exception e) {
            fail();
        }
    }



}
