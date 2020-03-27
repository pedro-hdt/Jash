import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeamUBugs {
    
    ShellImpl shell = new ShellImpl();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayInputStream input;
    static String ORIGINAL_DIR;
    
    @BeforeAll
    static void setupAll() {
        ORIGINAL_DIR = Environment.currentDirectory;
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
        output.reset();
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
