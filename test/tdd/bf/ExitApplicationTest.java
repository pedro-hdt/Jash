package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Permission;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

import java.security.Permission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ExitApplicationTest {
    private ExitApplication exitApp;

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
    static void setUp() {
        System.out.println("Starting Exit App Test");
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @BeforeEach
    public void initialisation() {
        exitApp = new ExitApplication();
    }

    @Test
    public void testExit() {
        try {
            exitApp.terminateExecution();
        } catch (ExitException e) {
            assertEquals("exit: terminating execution", e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        System.setSecurityManager(null);
    }
}