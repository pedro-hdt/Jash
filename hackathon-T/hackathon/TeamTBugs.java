import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sun.tools.doclint.Env;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class TeamTBugs {

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
     * Double backquote nesting results in wrong execution of commands in CommandSubs
     * For multiple command subs in same command, the combination should be as expected
     */
    @Test
    @DisplayName("Bug Number #2")
    public void testNestedQuotesMultiple() {
        try {

            shell.parseAndEvaluate("echo abc `echo 1 2 3`xyz`echo 4 5 6`", output);
            assertEquals("abc 1 2 3xyz4 5 6" + StringUtils.STRING_NEWLINE, output.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
