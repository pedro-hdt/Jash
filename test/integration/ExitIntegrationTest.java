package integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;

@SuppressWarnings("PMD.AvoidInstanceofChecksInCatchClause")
public class ExitIntegrationTest {
    public static final String TERM_EXEC_MSG = "terminating execution";
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
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    @Test
    public void testExitWithSequence() {
        try {
            shell.parseAndEvaluate("exit ; ls", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    @Test
    public void testExitWithIoRedir() throws IOException {
        try {
            shell.parseAndEvaluate("exit > diff *.txt", stdout);
            Files.deleteIfExists(IOUtils.resolveFilePath("diff"));
        } catch (Exception e) {
            Files.deleteIfExists(IOUtils.resolveFilePath("diff"));
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    @Test
    public void testExitWithGlob() {
        try {
            shell.parseAndEvaluate("exit *", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    @Test
    public void testExitWithQuote() {
        try {
            shell.parseAndEvaluate("exit 'hi'", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    @Test
    public void testExitInCommandSubs() {
        try {
            shell.parseAndEvaluate("exit `ls`", stdout);
        } catch (Exception e) {
            assertTrue(e instanceof ExitException);
            assertMsgContains(e, TERM_EXEC_MSG);
        }
    }
    
    
}
