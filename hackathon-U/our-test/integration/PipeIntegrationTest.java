package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PipeIntegrationTest {
    
    public static final String PASTE_CMD = "paste";
    public static final String ECHO_CMD = "echo";
    public static final String GREP_CMD = "grep";
    public static ApplicationRunner appRunner;
    public static ArgumentResolver argumentResolver;
    public static ByteArrayOutputStream out;
    public static ShellImpl shell = new ShellImpl();
    
    public static final String ORIGINAL_DIR = Environment.currentDirectory;
    
    
    @BeforeAll
    public static void setUp() {
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
        out = new ByteArrayOutputStream();
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "PipeIntegrationTestFolder");
    }
    
    @BeforeEach
    public void init() {
        out.reset();
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        out.close();
        Environment.currentDirectory = ORIGINAL_DIR;
    }
    
    @AfterEach
    public void tearAfterTest() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "PipeIntegrationTestFolder");
    }
    
    @Test
    public void testSimplePipe1() throws ShellException, AbstractApplicationException {
        
        String message = "hello world";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "-"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals(message + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testSimplePipe2() throws ShellException, AbstractApplicationException {
        
        String message = "this message is 6 words long";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand wcCmd = new CallCommand(Arrays.asList("wc", "-w"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, wcCmd));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals(String.format(" %7d", 6) + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testSimplePipe3() throws ShellException, AbstractApplicationException {
        
        String message = "not yet cut";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand cut = new CallCommand(Arrays.asList("cut", "-c", "9-11"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, cut));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals("cut" + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testSimplePipe4() throws ShellException, AbstractApplicationException {
        
        String message = "5" + StringUtils.STRING_NEWLINE + "3" + StringUtils.STRING_NEWLINE + "23"
                + StringUtils.STRING_NEWLINE + "1" + StringUtils.STRING_NEWLINE + "8" + StringUtils.STRING_NEWLINE + "4";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand sort = new CallCommand(Arrays.asList("sort", "-n"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, sort));
        
        pipeCommand.evaluate(System.in, out);
        String expected = "1" + StringUtils.STRING_NEWLINE + "3" + StringUtils.STRING_NEWLINE + "4"
                + StringUtils.STRING_NEWLINE + "5" + StringUtils.STRING_NEWLINE + "8" + StringUtils.STRING_NEWLINE + "23";

        assertEquals(expected + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testSimplePipe5() throws ShellException, AbstractApplicationException {
        
        String message = "this is greppable";
        
        InputStream stdin = new ByteArrayInputStream(message.getBytes());
        
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "-"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList(GREP_CMD, GREP_CMD), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(paste, grep));
        
        pipeCommand.evaluate(stdin, out);
        
        assertEquals(message + STRING_NEWLINE, out.toString());
        
    }
    
    
    @Test
    public void testSimplePipe6() throws ShellException, AbstractApplicationException {
        
        CallCommand lsCmd = new CallCommand(Arrays.asList("ls", "./dummyTestFolder"), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "-"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(lsCmd, paste));
        
        pipeCommand.evaluate(System.in, out);
        
        ByteArrayOutputStream lsOut = new ByteArrayOutputStream();
        lsCmd.evaluate(System.in, lsOut);
        assertEquals(lsOut.toString(), out.toString());
        
    }
    
    
    @Test
    public void testSimplePipe7() throws ShellException, AbstractApplicationException {
        
        CallCommand wcCmd = new CallCommand(Arrays.asList("wc", "-c", "./dummyTestFolder/WcTestFolder/wc2.txt"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList(GREP_CMD, "42"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(wcCmd, grep));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals("", out.toString());
        
    }
    
    
    @Test
    public void testSimplePipe8() throws ShellException {
        
        CallCommand lsCmd = new CallCommand(Arrays.asList("ls"), appRunner, argumentResolver);
        CallCommand exit = new CallCommand(Arrays.asList("exit"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(lsCmd, exit));
        
        ExitException exitException = assertThrows(ExitException.class, () -> pipeCommand.evaluate(System.in, out));
        assertMsgContains(exitException, "terminating execution");
        
    }
    
    
    @Test
    public void testSimplePipeFirstCommandFails() throws ShellException, AbstractApplicationException {
        
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "nonExistentFile.txt"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList(GREP_CMD, GREP_CMD), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(paste, grep));
        
        PasteException pasteException = assertThrows(PasteException.class, () -> pipeCommand.evaluate(System.in, out));
        assertMsgContains(pasteException, ERR_FILE_NOT_FOUND);
        
    }
    
    @Test
    public void testSimplePipeSecondCommandDoesNotExist() throws ShellException {
        
        CallCommand paste = new CallCommand(Arrays.asList("ls"), appRunner, argumentResolver);
        CallCommand fake = new CallCommand(Arrays.asList("fakeCommand", "arg"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(paste, fake));
        
        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(System.in, out));
        assertMsgContains(shellException, ERR_INVALID_APP);
        
        
    }
    
    @Test
    public void testDoublePipe() throws ShellException, AbstractApplicationException {
        
        String message = "hello world";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "-"), appRunner, argumentResolver);
        CallCommand sed = new CallCommand(Arrays.asList("sed", "s/hello/goodbye/"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste, sed));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals("goodbye world" + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testTriplePipe() throws ShellException, AbstractApplicationException {
        
        String message = "hello world";
        
        CallCommand echo = new CallCommand(Arrays.asList(ECHO_CMD, message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList(PASTE_CMD, "-"), appRunner, argumentResolver);
        CallCommand sed = new CallCommand(Arrays.asList("sed", "s/hello/goodbye/"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList(GREP_CMD, "-i", "WORLD"), appRunner, argumentResolver);
        
        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste, sed, grep));
        
        pipeCommand.evaluate(System.in, out);
        
        assertEquals("goodbye world" + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    public void testTriplePipes() {
        try {
            OutputStream stdout = new ByteArrayOutputStream();
    
            shell.parseAndEvaluate("ls | cut -c 1-3 | sort", stdout);
            assertEquals("dir" + StringUtils.STRING_NEWLINE, stdout.toString());
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testMultiplePipes() {
        try {
            OutputStream stdout = new ByteArrayOutputStream();
            shell.parseAndEvaluate("ls | grep .tat | grep match", stdout);
            assertEquals("match1.tat" + StringUtils.STRING_NEWLINE +
              "match2.tat" + StringUtils.STRING_NEWLINE, stdout.toString());
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testSimplePipe9() {
        try {
            OutputStream stdout = new ByteArrayOutputStream();
            shell.parseAndEvaluate("grep -i [\\d] < text.txt | wc -l", stdout);
            assertEquals("       4" + StringUtils.STRING_NEWLINE, stdout.toString());
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testSimplePipe10() {
        try {
            OutputStream stdout = new ByteArrayOutputStream();
            shell.parseAndEvaluate("diff match*.tat > output.txt", stdout);
    
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("output.txt")));
            assertEquals("< hi" + StringUtils.STRING_NEWLINE +
              "> hie" + StringUtils.STRING_NEWLINE +
              "" + StringUtils.STRING_NEWLINE, str1);
    
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "output.txt"));
    
    
        } catch (Exception e) {
            fail();
        }
    }
    
}
