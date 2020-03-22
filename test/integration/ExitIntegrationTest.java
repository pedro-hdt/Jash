package integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

public class ExitIntegrationTest {
    private static ShellImpl shell;
    OutputStream stdout = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        shell = new ShellImpl();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {

        stdout.flush();
    }

    @Test
    public void testExitWithPipe() {
        try {
            shell.parseAndEvaluate("exit | ls", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    public void testExitWithSequence() {
        try {
            shell.parseAndEvaluate("exit ; ls", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    public void testExitWithIoRedir() {
        try {
            shell.parseAndEvaluate("exit > diff *.txt", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    public void testExitWithGlob() {
        try {
            shell.parseAndEvaluate("exit *", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    public void testExitWithQuote() {
        try {
            shell.parseAndEvaluate("exit 'hi'", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }

    @Test
    public void testExitInCommandSubs() {
        try {
            shell.parseAndEvaluate("exit `ls`", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, "terminating execution");
        }
    }


}
