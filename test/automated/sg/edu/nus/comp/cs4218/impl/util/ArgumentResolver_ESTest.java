/*
 * This file was automatically generated by EvoSuite
 * Thu Mar 19 15:09:10 GMT 2020
 */

package automated.sg.edu.nus.comp.cs4218.impl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.RegexArgument;

@SuppressWarnings("PMD")
public class ArgumentResolver_ESTest {

  @Test 
  public void test00()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      List<String> list0 = argumentResolver0.resolveOneArgument("");
      assertFalse(list0.contains(""));
  }

  @Test 
  public void test01()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      LinkedList<String> linkedList0 = new LinkedList<String>();
      List<String> list0 = argumentResolver0.parseArguments(linkedList0);
      assertTrue(list0.isEmpty());
  }

  @Test 
  public void test02()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      // Undeclared exception!
      try { 
        argumentResolver0.resolveOneArgument((String) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver", e);
         assertTrue(e instanceof NullPointerException);
      }
  }

  @Test 
  public void test03()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      RegexArgument regexArgument0 = new RegexArgument("([^'\"`|<>;s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+");
      List<String> list0 = regexArgument0.globFiles();
      try { 
        argumentResolver0.parseArguments(list0);
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: ]*: Invalid app
         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner", e);
          assertTrue(e instanceof ShellException);

      }
  }

  @Test 
  public void test04()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      // Undeclared exception!
      try { 
        argumentResolver0.parseArguments((List<String>) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver", e);
          assertTrue(e instanceof NullPointerException);

      }
  }

  @Test 
  public void test05()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      // Undeclared exception!
      try { 
        argumentResolver0.makeRegexArgument((String) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
      }
  }

  @Test 
  public void test06()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      List<String> list0 = argumentResolver0.resolveOneArgument("cp");
      assertTrue(list0.contains("cp"));
  }

  @Test 
  public void test07()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      List<String> list0 = argumentResolver0.resolveOneArgument("Dt`N;-`");
      assertEquals(1, list0.size());
      assertFalse(list0.contains("Dt`N;-`"));
  }

  @Test 
  public void test08()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      RegexArgument regexArgument0 = argumentResolver0.makeRegexArgument();
      assertEquals("", regexArgument0.toString());
  }

  @Test 
  public void test09()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      ApplicationRunner applicationRunner0 = argumentResolver0.getAppRunner();
      assertNotNull(applicationRunner0);
  }

  @Test 
  public void test10()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      RegexArgument regexArgument0 = argumentResolver0.makeRegexArgument("([^'\"`|<>;s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+");
      assertEquals("([^'\"`|<>;s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+", regexArgument0.toString());
  }

  @Test 
  public void test11()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      List<String> list0 = argumentResolver0.resolveOneArgument("W{r`>x'l#cv*pF }");
      assertEquals(1, list0.size());
  }

  @Test 
  public void test12()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      try { 
        argumentResolver0.resolveOneArgument("([^'\"`|<>;s]+|'[^']*'|\"([^\"``*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+");
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: .*?: Invalid app
         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner", e);
          assertTrue(e instanceof ShellException);

      }
  }

  @Test 
  public void test13()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      try { 
        argumentResolver0.resolveOneArgument("([^'\"`|<>;s]+|'[^']*'|\"([^```.*?`[^$`]*)+!|\"[^\"]*\"`[^`]*`)+");
        fail("Expecting exception: Exception");
      
      } catch(Exception e) {
         //
         // shell: .*?: Invalid app
         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner", e);
          assertTrue(e instanceof ShellException);

      }
  }

  @Test 
  public void test14()  throws Throwable  {
      ArgumentResolver argumentResolver0 = new ArgumentResolver();
      LinkedList<String> linkedList0 = new LinkedList<String>();
      linkedList0.add("Dt`N;-`");
      List<String> list0 = argumentResolver0.parseArguments(linkedList0);
      assertFalse(list0.contains("Dt`N;-`"));
      assertEquals(1, list0.size());
  }
}
