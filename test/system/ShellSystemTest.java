package system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class ShellSystemTest {

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

    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();

    OutputStream stdout = new ByteArrayOutputStream();

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
    public void systemTest() {

        String commandsToRun = "ls" + StringUtils.STRING_NEWLINE
                + "echo hi" + StringUtils.STRING_NEWLINE
                + "cd .. ; ls" + StringUtils.STRING_NEWLINE
                + "cd SystemTestFolder" + StringUtils.STRING_NEWLINE
                + "echo yo > file1.txt" + StringUtils.STRING_NEWLINE
                + "sed abc" + StringUtils.STRING_NEWLINE
                + "exit" + StringUtils.STRING_NEWLINE;

        System.setIn(new ByteArrayInputStream(commandsToRun.getBytes()));

        try {
            ShellImpl.main();
            assertTrue(stdout.toString().contains("file1.txt"));
            assertTrue(stdout.toString().contains("hi"));
            assertTrue(stdout.toString().contains("sed: Invalid replacement rule"));
        } catch (ExitException e) {
            assertEquals("exit: terminating execution", e.getMessage());
        }

    }
}
