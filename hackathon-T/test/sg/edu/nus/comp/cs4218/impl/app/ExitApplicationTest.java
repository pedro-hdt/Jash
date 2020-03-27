package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests for Exit Shell Command
 * <i>Only need to check if it successfully exits</i>
 */
public class ExitApplicationTest {
    
    private static ExitApplication exitApp;
    
    @BeforeEach
    public void setUp() {
        exitApp = new ExitApplication();
    }
    
    /**
     * Test for run() with null args
     */
    @Test
    public void testExitGracefullyWithNullArgs() {
        Exception exception = assertThrows(ExitException.class, () -> {
            exitApp.run(null, null, null);
        });
    
        assertTrue(exception.getMessage().contains("terminating execution"));
    
    }
    
    /**
     * Test for run() with non NullParams
     */
    @Test
    public void testExitGracefullyNonNullParams() {
        Exception exception = assertThrows(ExitException.class, () -> {
            exitApp.run(new String[0], System.in, System.out);
        });
    
        assertTrue(exception.getMessage().contains("terminating execution"));
    
    }
    
    /**
     * Test for terminateExecution() with null args
     */
    @Test
    public void testTerminateExecution() {
        Exception exception = assertThrows(ExitException.class, () -> {
            exitApp.terminateExecution();
        });
    
        assertTrue(exception.getMessage().contains("terminating execution"));
    
    }
    
    /**
     * Test with creating mock thread
     */
    @Test
    public void testExitGracefully() {
        Thread thread1 = new Thread(() -> {
            try {
                exitApp.run(null, null, null);
            } catch (ExitException e) {
            }
        });
    
        assertTrue(!thread1.isAlive());
    }
    
    
}
