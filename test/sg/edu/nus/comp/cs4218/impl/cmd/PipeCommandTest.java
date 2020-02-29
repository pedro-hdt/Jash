package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

/**
 * Tests for Pipe operator used in commands
 */
public class PipeCommandTest {

    public PipeCommand pipeCommand;
    private static OutputStream stdout;


    private static final String ORIGINAL_DIR = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR + File.separator + "dummyTestFolder" + File.separator + "PipeTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        pipeCommand = null;
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    /**
     * Test to filter out only files which have filtered in their name
     */
    @Test
    public void testLsWithGrepUsingPipe() {
        try {
            CallCommand lsCommand = new CallCommand(new ArrayList<>(Collections.singleton("ls")), new ApplicationRunner(), new ArgumentResolver());
            CallCommand grepCommand = new CallCommand(new ArrayList<>(Arrays.asList("grep", "filtered")), new ApplicationRunner(), new ArgumentResolver());

            pipeCommand = new PipeCommand(new ArrayList<>(Arrays.asList(lsCommand, grepCommand)));
            pipeCommand.evaluate(System.in, stdout);

            assertTrue(stdout.toString().contains("filtered.txt"));
            assertFalse(stdout.toString().contains("random.txt"));

        } catch (Exception e) {
            fail("should not fail" + e);
        }
    }

}
