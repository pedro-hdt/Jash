/*
 * This file was automatically generated by EvoSuite
 * Thu Mar 19 14:26:58 GMT 2020
 */

package automated.sg.edu.nus.comp.cs4218.impl.util;


import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("PMD")
public class CommandBuilder_ESTest {
    
    @Test
    public void test0() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand(";\"Y^^P~", applicationRunner0);
            fail("Expecting exception: Exception");
            
        } catch (Exception e) {
            //
            // shell: Invalid syntax
            //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
            assertTrue(e instanceof ShellException);
        }
    }
    
    @Test
    public void test1() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand("|.", applicationRunner0);
            fail("Expecting exception: Exception");
            
        } catch (Exception e) {
            //
            // shell: Invalid syntax
            //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
            assertTrue(e instanceof ShellException);
            
        }
    }
    
    @Test
    public void test2() throws Throwable {
        try {
            CommandBuilder.parseCommand("IUG[*`A}Yr", (ApplicationRunner) null);
            fail("Expecting exception: Exception");
            
        } catch (Exception e) {
            //
            // shell: Invalid syntax
            //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
            assertTrue(e instanceof ShellException);
            
        }
    }
    
    @Test
    public void test3() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        Command command0 = CommandBuilder.parseCommand("0s;C@RmWu_vg<J]", applicationRunner0);
        assertNotNull(command0);
    }
    
    @Test
    public void test4() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand("z>{!Fug@B!\"", applicationRunner0);
            fail("Expecting exception: Exception");
            
        } catch (Exception e) {
            //
            // shell: Invalid syntax
            //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
            assertTrue(e instanceof ShellException);
            
        }
    }
    
    @Test
    public void test5() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        Command command0 = CommandBuilder.parseCommand("we3[zf\u0005|/KX@I<}", applicationRunner0);
        assertNotNull(command0);
    }
    
    @Test
    public void test6() throws Throwable {
        ApplicationRunner applicationRunner0 = new ApplicationRunner();
        try {
            CommandBuilder.parseCommand("", applicationRunner0);
            fail("Expecting exception: Exception");
            
        } catch (Exception e) {
            //
            // shell: Invalid syntax
            //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.CommandBuilder", e);
            assertTrue(e instanceof ShellException);
            
        }
    }
}
