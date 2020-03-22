/*
 * This file was automatically generated by EvoSuite
 * Thu Mar 19 15:58:30 GMT 2020
 */

package automated.sg.edu.nus.comp.cs4218.impl.util;


import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD")
public class IORedirectionHandler_ESTest {
    
    @Test
    public void test0() throws Throwable {
        LinkedList<String> linkedList0 = new LinkedList<String>();
        PipedInputStream pipedInputStream0 = new PipedInputStream();
        ArgumentResolver argumentResolver0 = new ArgumentResolver();
        IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(linkedList0, pipedInputStream0, (OutputStream) null, argumentResolver0);
        OutputStream outputStream0 = iORedirectionHandler0.getOutputStream();
        assertNull(outputStream0);
    }

//  @Test
//  public void test1()  throws Throwable  {
//      LinkedList<Locale.LanguageRange> linkedList0 = new LinkedList<Locale.LanguageRange>();
//      Locale locale0 = Locale.ENGLISH;
//      Set<String> set0 = locale0.getUnicodeLocaleKeys();
//      Locale.FilteringMode locale_FilteringMode0 = Locale.FilteringMode.AUTOSELECT_FILTERING;
//      List<String> list0 = Locale.filterTags((List<Locale.LanguageRange>) linkedList0, (Collection<String>) set0, locale_FilteringMode0);
//      ByteArrayOutputStream byteArrayOutputStream0 = new ByteArrayOutputStream();
//      DataOutputStream dataOutputStream0 = new DataOutputStream(byteArrayOutputStream0);
//      MockPrintStream mockPrintStream0 = new MockPrintStream(dataOutputStream0);
//      ArgumentResolver argumentResolver0 = new ArgumentResolver();
//      IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, (InputStream) null, mockPrintStream0, argumentResolver0);
//      InputStream inputStream0 = iORedirectionHandler0.getInputStream();
//      assertNull(inputStream0);
//  }
    
    @Test
    public void test2() throws Throwable {
        LinkedList<String> linkedList0 = new LinkedList<String>();
        byte[] byteArray0 = new byte[6];
        linkedList0.offerLast((String) null);
        ByteArrayInputStream byteArrayInputStream0 = new ByteArrayInputStream(byteArray0, (byte) (-48), (-2441));
        PipedOutputStream pipedOutputStream0 = new PipedOutputStream();
        ArgumentResolver argumentResolver0 = new ArgumentResolver();
        IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(linkedList0, byteArrayInputStream0, pipedOutputStream0, argumentResolver0);
        // Undeclared exception!
        try {
            iORedirectionHandler0.extractRedirOptions();
            fail("Expecting exception: NullPointerException");
            
        } catch (NullPointerException e) {
        }
    }
    
    @Test
    public void test3() throws Throwable {
        ArgumentResolver argumentResolver0 = new ArgumentResolver();
        List<String> list0 = argumentResolver0.resolveOneArgument("(B|5^.M#_!?");
        byte[] byteArray0 = new byte[0];
        ByteArrayInputStream byteArrayInputStream0 = new ByteArrayInputStream(byteArray0);
        PipedInputStream pipedInputStream0 = new PipedInputStream();
        PipedOutputStream pipedOutputStream0 = new PipedOutputStream(pipedInputStream0);
        IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, byteArrayInputStream0, pipedOutputStream0, argumentResolver0);
        iORedirectionHandler0.extractRedirOptions();
        List<String> list1 = iORedirectionHandler0.getNoRedirArgsList();
        assertTrue(list1.contains("(B|5^.M#_!?"));
    }
    
    @Test
    public void test4() throws Throwable {
        ArgumentResolver argumentResolver0 = new ArgumentResolver();
        List<String> list0 = argumentResolver0.resolveOneArgument("(B|5^.M#_!?");
        byte[] byteArray0 = new byte[0];
        ByteArrayInputStream byteArrayInputStream0 = new ByteArrayInputStream(byteArray0);
        PipedInputStream pipedInputStream0 = new PipedInputStream();
        PipedOutputStream pipedOutputStream0 = new PipedOutputStream(pipedInputStream0);
        IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, byteArrayInputStream0, pipedOutputStream0, argumentResolver0);
        InputStream inputStream0 = iORedirectionHandler0.getInputStream();
        assertEquals(0, inputStream0.available());
    }
    
    @Test
    public void test5() throws Throwable {
        ArgumentResolver argumentResolver0 = new ArgumentResolver();
        List<String> list0 = argumentResolver0.resolveOneArgument("(B|5^.M#_!?");
        byte[] byteArray0 = new byte[0];
        ByteArrayInputStream byteArrayInputStream0 = new ByteArrayInputStream(byteArray0);
        PipedInputStream pipedInputStream0 = new PipedInputStream();
        PipedOutputStream pipedOutputStream0 = new PipedOutputStream(pipedInputStream0);
        IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, byteArrayInputStream0, pipedOutputStream0, argumentResolver0);
        OutputStream outputStream0 = iORedirectionHandler0.getOutputStream();
        assertSame(pipedOutputStream0, outputStream0);
    }

//  @Test
//  public void test6()  throws Throwable  {
//      ArgumentResolver argumentResolver0 = new ArgumentResolver();
//      List<String> list0 = argumentResolver0.resolveOneArgument("<");
//      FileDescriptor fileDescriptor0 = new FileDescriptor();
//      MockFileInputStream mockFileInputStream0 = new MockFileInputStream(fileDescriptor0);
//      SequenceInputStream sequenceInputStream0 = new SequenceInputStream(mockFileInputStream0, mockFileInputStream0);
//      BufferedInputStream bufferedInputStream0 = new BufferedInputStream(sequenceInputStream0, 60);
//      MockFile mockFile0 = new MockFile("<");
//      MockFileOutputStream mockFileOutputStream0 = new MockFileOutputStream(mockFile0, false);
//      IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, bufferedInputStream0, mockFileOutputStream0, argumentResolver0);
//      try {
//        iORedirectionHandler0.extractRedirOptions();
//        fail("Expecting exception: Exception");
//
//      } catch(Exception e) {
//         //
//         // shell: Invalid syntax
//         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler", e);
//      }
//  }

//  @Test
//  public void test7()  throws Throwable  {
//      ArgumentResolver argumentResolver0 = new ArgumentResolver();
//      List<String> list0 = argumentResolver0.resolveOneArgument("");
//      Enumeration<InputStream> enumeration0 = (Enumeration<InputStream>) mock(Enumeration.class, new ViolatedAssumptionAnswer());
//      doReturn(false).when(enumeration0).hasMoreElements();
//      SequenceInputStream sequenceInputStream0 = new SequenceInputStream(enumeration0);
//      IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, sequenceInputStream0, (OutputStream) null, argumentResolver0);
//      try {
//        iORedirectionHandler0.extractRedirOptions();
//        fail("Expecting exception: Exception");
//
//      } catch(Exception e) {
//         //
//         // shell: Invalid syntax
//         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler", e);
//      }
//  }

//  @Test
//  public void test8()  throws Throwable  {
//      FileDescriptor fileDescriptor0 = new FileDescriptor();
//      MockFileInputStream mockFileInputStream0 = new MockFileInputStream(fileDescriptor0);
//      MockPrintStream mockPrintStream0 = new MockPrintStream("{B}E`):UJ");
//      ArgumentResolver argumentResolver0 = new ArgumentResolver();
//      IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler((List<String>) null, mockFileInputStream0, mockPrintStream0, argumentResolver0);
//      try {
//        iORedirectionHandler0.extractRedirOptions();
//        fail("Expecting exception: Exception");
//
//      } catch(Exception e) {
//         //
//         // shell: Invalid syntax
//         //
//         verifyException("sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler", e);
//      }
//  }
//
//  @Test
//  public void test9()  throws Throwable  {
//      ArgumentResolver argumentResolver0 = new ArgumentResolver();
//      List<String> list0 = argumentResolver0.resolveOneArgument("3");
//      Enumeration<InputStream> enumeration0 = (Enumeration<InputStream>) mock(Enumeration.class, new ViolatedAssumptionAnswer());
//      doReturn(false).when(enumeration0).hasMoreElements();
//      SequenceInputStream sequenceInputStream0 = new SequenceInputStream(enumeration0);
//      MockPrintStream mockPrintStream0 = new MockPrintStream("W/2l)~h}?kRvyy`");
//      IORedirectionHandler iORedirectionHandler0 = new IORedirectionHandler(list0, sequenceInputStream0, mockPrintStream0, argumentResolver0);
//      List<String> list1 = iORedirectionHandler0.getNoRedirArgsList();
//      assertNull(list1);
//  }
}
