/*
 * This file was automatically generated by EvoSuite
 * Thu Mar 19 17:02:08 GMT 2020
 */

package automated.sg.edu.nus.comp.cs4218.impl.parser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;

public class CutArgsParser_ESTest {

//  @Test
//  public void test00()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      LinkedList<String> linkedList0 = new LinkedList<String>();
//      cutArgsParser0.nonFlagArgs = (List<String>) linkedList0;
//      linkedList0.add((String) null);
//      String string0 = cutArgsParser0.getList();
//      assertNull(string0);
//  }

  @Test 
  public void test01()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      String[] stringArray0 = new String[4];
      stringArray0[0] = "illegal option -- ";
      stringArray0[1] = "illegal option -- ";
      stringArray0[2] = "illegal option -- ";
      stringArray0[3] = "illegal option -- ";
      cutArgsParser0.parse(stringArray0);
      String string0 = cutArgsParser0.getList();
      assertEquals("illegal option -- ", string0);
  }

  @Test 
  public void test02()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      String[] stringArray0 = new String[5];
      stringArray0[0] = "";
      stringArray0[1] = "illegal option -- ";
      stringArray0[2] = "illegal option -- ";
      stringArray0[3] = "illegal option -- ";
      stringArray0[4] = "illegal option -- ";
      cutArgsParser0.parse(stringArray0);
      String string0 = cutArgsParser0.getList();
      assertEquals("", string0);
  }

  @Test 
  public void test03()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      String[] stringArray0 = new String[4];
      stringArray0[0] = "illegal option -- ";
      stringArray0[1] = "illegal option -- ";
      stringArray0[2] = "illegal option -- ";
      stringArray0[3] = "illegal option -- ";
      cutArgsParser0.parse(stringArray0);
      String[] stringArray1 = cutArgsParser0.getFiles();
      assertEquals(3, stringArray1.length);
  }

  @Test 
  public void test04()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      assertFalse(cutArgsParser0.isCutByCharPos());
      assertFalse(cutArgsParser0.isCutByBytePos());
      
      String[] stringArray0 = new String[1];
      stringArray0[0] = "illegal option -- ";
      cutArgsParser0.parse(stringArray0);
      String[] stringArray1 = cutArgsParser0.getFiles();
      assertEquals(0, stringArray1.length);
  }

//  @Test
//  public void test05()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      cutArgsParser0.flags = null;
//      // Undeclared exception!
//      try {
//        cutArgsParser0.isCutByCharPos();
//        fail("Expecting exception: NullPointerException");
//
//      } catch(NullPointerException e) {
//      }
//  }
//
//  @Test
//  public void test06()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      cutArgsParser0.legalFlags = null;
//      cutArgsParser0.flags = cutArgsParser0.legalFlags;
//      // Undeclared exception!
//      try {
//        cutArgsParser0.isCutByBytePos();
//        fail("Expecting exception: NullPointerException");
//
//      } catch(NullPointerException e) {
//      }
//  }
//
//  @Test
//  public void test07()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      cutArgsParser0.nonFlagArgs = null;
//      // Undeclared exception!
//      try {
//        cutArgsParser0.getList();
//        fail("Expecting exception: NullPointerException");
//
//      } catch(NullPointerException e) {
//         //
//         // no message in exception (getMessage() returned null)
//         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser", e);
//      }
//  }
//
//  @Test
//  public void test08()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      cutArgsParser0.nonFlagArgs = null;
//      // Undeclared exception!
//      try {
//        cutArgsParser0.getFiles();
//        fail("Expecting exception: NullPointerException");
//
//      } catch(NullPointerException e) {
//         //
//         // no message in exception (getMessage() returned null)
//         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser", e);
//      }
//  }
//
//  @Test
//  public void test09()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      Set<Character> set0 = cutArgsParser0.legalFlags;
//      cutArgsParser0.flags = set0;
//      cutArgsParser0.isCutByBytePos();
//      assertTrue(cutArgsParser0.isCutByCharPos());
//  }

  @Test 
  public void test10()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      Boolean boolean0 = cutArgsParser0.isCutByBytePos();
      assertFalse(cutArgsParser0.isCutByCharPos());
      assertFalse(boolean0);
  }

//  @Test
//  public void test11()  throws Throwable  {
//      CutArgsParser cutArgsParser0 = new CutArgsParser();
//      Set<Character> set0 = cutArgsParser0.legalFlags;
//      cutArgsParser0.flags = set0;
//      cutArgsParser0.isCutByCharPos();
//      assertTrue(cutArgsParser0.isCutByBytePos());
//  }

  @Test 
  public void test12()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      Boolean boolean0 = cutArgsParser0.isCutByCharPos();
      assertFalse(boolean0);
      assertFalse(cutArgsParser0.isCutByBytePos());
  }

  @Test 
  public void test13()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      // Undeclared exception!
      try { 
        cutArgsParser0.getList();
        fail("Expecting exception: IndexOutOfBoundsException");
      
      } catch(IndexOutOfBoundsException e) {
         //
         // Index: 0, Size: 0
         //
      }
  }

  @Test 
  public void test14()  throws Throwable  {
      CutArgsParser cutArgsParser0 = new CutArgsParser();
      // Undeclared exception!
      try { 
        cutArgsParser0.getFiles();
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // fromIndex(1) > toIndex(0)
         //
      }
  }
}
