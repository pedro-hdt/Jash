import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

public class TeamUBugs {

    ShellImpl shell = new ShellImpl();
    OutputStream output = new ByteArrayOutputStream();

    @BeforeAll
    static void setupAll() {
    }

    @AfterAll
    static void reset() {
    }

    @BeforeEach
    public void setup() {
        shell = new ShellImpl();
        output = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        output.flush();
    }

    /**
     * Double quotes interprets back quotes
     * CommandSubs within Double quotes adds extra spaces
     * Should not print an extra space before the period
     */
    @Test
    @DisplayName("Bug Number #4")
    public void testDoubleQuotesWithBackQuote() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("echo \"This is random:`echo \"random\"`.\"", output);

        assertEquals(output.toString(), "This is random:random.");
    }
}
