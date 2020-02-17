package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_REP_RULE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.SedException;

/**
 * SedApplicationTest used to test sed command
 *
 * Test Cases:
 *
 * Negative:
 * -    No arguments given
 * -    No output stream
 * -    No replacement text provided
 *
 *
 *
 * Positive:
 * -    Replace first with stdin input
 */
public class SedApplicationTest {
    private static SedApplication sed;
    private static InputStream stdin;
    private static OutputStream stdout;


    @BeforeEach
    public void setUp() {
        sed = new SedApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void tearDown() throws IOException {
        stdout.flush();
    }

    @Test
    public void testFailsWithNullArgsOrStream() {
        Exception expectedException = assertThrows(SedException.class, () -> sed.run(null, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_ARGS));

        expectedException = assertThrows(SedException.class, () -> sed.run(new String[1], null, null));
        assertTrue(expectedException.getMessage().contains(ERR_NULL_STREAMS));

    }

    @Test
    public void testFailsWithLessThanOneArgs() {
        Exception exception = assertThrows(SedException.class, () -> sed.run(new String[0], null, stdout));
        assertTrue(exception.getMessage().contains(ERR_NO_REP_RULE));

    }

    @Test
    public void testReplaceSubStringFromStdin() {
        String stdInString = "hello toBeReplaced";

        String[] args = new String[] {"s/toBeReplaced/replacedWord/"};
        stdin = new ByteArrayInputStream(stdInString.getBytes());

        try {
            sed.run(args, stdin, stdout);
            assertTrue(stdout.toString().contains("replacedWord"));
        } catch (SedException e) {
            fail("should not fail: " + e.getMessage());
        }
    }


}
