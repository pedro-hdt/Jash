package system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.security.Permission;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class ShellSystemTest {

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    OutputStream stdout = new ByteArrayOutputStream();

    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            // allow anything.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException("terminating execution");
        }
    }

    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "SystemTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    public void setup() {
        System.setSecurityManager(new NoExitSecurityManager());
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
                + StringUtils.fileSeparator() + "dummyTestFolder"
                + StringUtils.fileSeparator() + "SystemTestFolder");

        stdout.flush();
    }

    /**
     * Tests entire system
     */
    @Test
    @DisplayName("System Test")
    public void systemTest() throws IOException {

        String commandsToRun = "ls" + StringUtils.STRING_NEWLINE
                + "echo hi" + StringUtils.STRING_NEWLINE
                + "cd .. ; ls" + StringUtils.STRING_NEWLINE
                + "cd SystemTestFolder" + StringUtils.STRING_NEWLINE
                + "echo yo > file1.txt" + StringUtils.STRING_NEWLINE
                + "wc -c > newfile.txt" + StringUtils.STRING_NEWLINE
                + "rm newfile.txt" + StringUtils.STRING_NEWLINE
                + "sort < testCommands.txt > result.txt" + StringUtils.STRING_NEWLINE
                + "diff testCommands.txt result.txt" + StringUtils.STRING_NEWLINE
                + "cp result.txt result-dup.txt" + StringUtils.STRING_NEWLINE
                + "cut -c 1-2 result.txt" + StringUtils.STRING_NEWLINE
                + "sed abc" + StringUtils.STRING_NEWLINE
                + "exit" + StringUtils.STRING_NEWLINE;


        try {
            System.setIn(new ByteArrayInputStream(commandsToRun.getBytes()));
            System.setOut(new PrintStream(stdout));

            ShellImpl.main();

        } catch (ExitException e) {
            assertTrue(stdout.toString().contains("sed: Invalid replacement rule"));
            assertTrue(stdout.toString().contains("file1.txt"));
            assertTrue(stdout.toString().contains("hi"));
            assertTrue(stdout.toString().contains("SystemTestFolder"));
            assertTrue(stdout.toString().contains("sed: Invalid replacement rule"));
            assertFalse(Files.exists(IOUtils.resolveFilePath("newfile.txt")));


            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("result.txt")));
            String str2 = new String(Files.readAllBytes(IOUtils.resolveFilePath("result-dup.txt")));
            assertEquals(str1, str2);

            assertEquals(stdout.toString().chars().filter(ch -> ch =='$').count(), 13);

            Files.deleteIfExists(IOUtils.resolveFilePath("result.txt"));
            Files.deleteIfExists(IOUtils.resolveFilePath("result-dup.txt"));
            assertEquals("exit: terminating execution", e.getMessage());

        } catch (Exception e) {
            fail();
        }

    }
}
