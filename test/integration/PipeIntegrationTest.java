package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PipeIntegrationTest {

    public static ApplicationRunner appRunner;
    public static ArgumentResolver argumentResolver;
    public static ByteArrayOutputStream out;

    @BeforeAll
    public static void setUp() {
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
        out = new ByteArrayOutputStream();
    }

    @BeforeEach
    public void init() {
        out.reset();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        out.close();
    }

    @Test
    public void testSimplePipe1() throws ShellException, AbstractApplicationException {

        String message = "hello world";

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList("paste", "-"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste));

        pipeCommand.evaluate(System.in, out);

        assertEquals(message + STRING_NEWLINE, out.toString());

    }

    @Test
    public void testSimplePipe2() throws ShellException, AbstractApplicationException {

        String message = "this message is 6 words long";

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand wc = new CallCommand(Arrays.asList("wc", "-w"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, wc));

        pipeCommand.evaluate(System.in, out);

        assertEquals(String.format(" %7d", 6) + STRING_NEWLINE, out.toString());

    }

    @Test
    public void testSimplePipe3() throws ShellException, AbstractApplicationException {

        String message = "not yet cut";

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand cut = new CallCommand(Arrays.asList("cut", "-c", "9-11"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, cut));

        pipeCommand.evaluate(System.in, out);

        assertEquals("cut" + STRING_NEWLINE, out.toString());

    }

    @Test
    public void testSimplePipe4() throws ShellException, AbstractApplicationException {

        String message = "5\n3\n23\n1\n8\n4";

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand sort = new CallCommand(Arrays.asList("sort", "-n"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, sort));

        pipeCommand.evaluate(System.in, out);

        assertEquals("1\n3\n4\n5\n8\n23" + STRING_NEWLINE, out.toString());

    }

    @Test
    public void testSimplePipe5() throws ShellException, AbstractApplicationException {

        String message = "this is greppable";

        InputStream in = new ByteArrayInputStream(message.getBytes());

        CallCommand paste = new CallCommand(Arrays.asList("paste", "-"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList("grep", "grep"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(paste, grep));

        pipeCommand.evaluate(in, out);

        assertEquals(message + STRING_NEWLINE, out.toString());

    }


    @Test
    public void testSimplePipe6() throws ShellException, AbstractApplicationException {

        CallCommand ls = new CallCommand(Arrays.asList("ls", "./dummyTestFolder"), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList("paste", "-"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(ls, paste));

        pipeCommand.evaluate(System.in, out);

        ByteArrayOutputStream lsOut = new ByteArrayOutputStream();
        ls.evaluate(System.in, lsOut);
        assertEquals(lsOut.toString(), out.toString());

    }


    @Test
    public void testSimplePipe7() throws ShellException, AbstractApplicationException {

        CallCommand wc = new CallCommand(Arrays.asList("wc", "-c", "./dummyTestFolder/WcTestFolder/wc2.txt"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList("grep", "42"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(wc, grep));

        pipeCommand.evaluate(System.in, out);

        assertEquals("", out.toString());

    }


    @Test
    public void testSimplePipe8() throws ShellException {

        CallCommand ls = new CallCommand(Arrays.asList("ls"), appRunner, argumentResolver);
        CallCommand exit = new CallCommand(Arrays.asList("exit"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(ls, exit));

        ExitException exitException = assertThrows(ExitException.class, () -> pipeCommand.evaluate(System.in, out));
        assertMsgContains(exitException, "terminating execution");

    }


    @Test
    public void testSimplePipeFirstCommandFails() throws ShellException, AbstractApplicationException {

        CallCommand paste = new CallCommand(Arrays.asList("paste", "nonExistentFile.txt"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList("grep", "grep"), appRunner, argumentResolver);

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

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList("paste", "-"), appRunner, argumentResolver);
        CallCommand sed = new CallCommand(Arrays.asList("sed", "s/hello/goodbye/"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste, sed));

        pipeCommand.evaluate(System.in, out);

        assertEquals("goodbye world" + STRING_NEWLINE, out.toString());

    }

    @Test
    public void testTriplePipe() throws ShellException, AbstractApplicationException {

        String message = "hello world";

        CallCommand echo = new CallCommand(Arrays.asList("echo", message), appRunner, argumentResolver);
        CallCommand paste = new CallCommand(Arrays.asList("paste", "-"), appRunner, argumentResolver);
        CallCommand sed = new CallCommand(Arrays.asList("sed", "s/hello/goodbye/"), appRunner, argumentResolver);
        CallCommand grep = new CallCommand(Arrays.asList("grep", "-i", "WORLD"), appRunner, argumentResolver);

        PipeCommand pipeCommand = new PipeCommand(Arrays.asList(echo, paste, sed, grep));

        pipeCommand.evaluate(System.in, out);

        assertEquals("goodbye world" + STRING_NEWLINE, out.toString());

    }

}
