package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class IORedirectionIntegrationTest {
    
    private static ShellImpl shell;
    private static ByteArrayOutputStream out;
    private static final String FILE1 = "intermediateFile1.txt";
    private static final String FILE2 = "intermediateFile2.txt";
    
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    
    
    @BeforeAll
    public static void setUp() throws IOException {
        shell = new ShellImpl();
        out = new ByteArrayOutputStream();
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "IoRedirIntegrationTestFolder");
        
        Files.createFile(IOUtils.resolveFilePath(FILE1));
        Files.createFile(IOUtils.resolveFilePath(FILE2));
    }
    
    @AfterEach
    public void finalize() {
        out.reset();
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "IoRedirIntegrationTestFolder");
    }
    
    @AfterAll
    public static void tearDown() throws IOException {
        Files.delete(IOUtils.resolveFilePath(FILE1));
        Files.delete(IOUtils.resolveFilePath(FILE2));
        
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    @Test
    @DisplayName("echo \"hello\" > file ; paste - < file")
    public void testSimpleIORedir1() throws AbstractApplicationException, ShellException {
        
        shell.parseAndEvaluate(String.format("echo \"hello\" > %s ; paste - < %1$s", FILE1), out);
        assertEquals("hello" + STRING_NEWLINE, out.toString());
        
    }
    
    @Test
    @DisplayName("ioredir pass data")
    public void testIoRedirPassing() throws IOException {
        try {
            shell.parseAndEvaluate("echo yo > first > second > third", out);
            fail();
    
        } catch (Exception e) {
            assertTrue(e instanceof ShellException); //NOPMD
            assertMsgContains(e, "shell: Multiple streams provided");
            Path filePath1 = Paths.get(Environment.getCurrentDirectory(), "first");
            Files.delete(filePath1);
        }
    }
    
    @Test
    @DisplayName("ioredir pass data output")
    public void testInvalidWhenFileDoesntExist() {
        try {
            shell.parseAndEvaluate("echo one < non-existent", out);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof ShellException); //NOPMD
            assertMsgContains(e, "shell: No such file or directory");
        }
    }
    
    @Test
    @DisplayName("ioredir pass data output")
    public void testIoRedirPassingOutput() {
        try {
            shell.parseAndEvaluate("echo one < yo", out);
            assertEquals("one" + StringUtils.STRING_NEWLINE, out.toString());
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("ls > file ; paste - < file")
    public void testSimpleIORedir2() throws AbstractApplicationException, ShellException, IOException {
        
        shell.parseAndEvaluate(String.format("ls > %s ; paste - < %1$s", FILE1), out);
        assertTrue(out.toString().contains("textfile.txt"));
        
    }
    
    @Test
    @DisplayName("echo \"hello\" > file1 ; sed s/hello/goodby/ < file1 > file2 ; grep good < file2")
    public void testDoubleIORedir1() throws AbstractApplicationException, ShellException {
        
        shell.parseAndEvaluate(
          String.format("echo \"hello\" > %s ; sed s/hello/goodbye/ < %1$s > %s ; grep good < %2$s", FILE1, FILE2), out);
        assertTrue(out.toString().contains("goodbye"));
        
    }
    
    @Test
    @DisplayName("echo \"hello\" > file1 ; paste - < file1 > file2 ; wc -c < file2")
    public void testDoubleIORedir2() throws AbstractApplicationException, ShellException {
        
        shell.parseAndEvaluate(
          String.format("echo \"hello\" > %s ; paste - < %1$s > %s ; wc -c < %2$s", FILE1, FILE2), out);
        assertTrue(out.toString().contains("6"));
        
    }
    
    @Test
    @DisplayName("echo \"hello\" > file ; invalid < file")
    public void testRedirInputToInvalidApp() throws AbstractApplicationException, ShellException {
        
        shell.parseAndEvaluate(String.format("echo \"hello\" > %s ; invalid < %1$s", FILE1), out);
        assertTrue(out.toString().contains(ERR_INVALID_APP));
        
    }
    
    @Test
    @DisplayName("invalid > file")
    public void testRedirOutputOfInvalidApp() {
        
        ShellException shellException = assertThrows(ShellException.class,
          () -> shell.parseAndEvaluate(String.format("invalid > %s", FILE1), out));
        assertMsgContains(shellException, ERR_INVALID_APP);
        
    }
    
    @Test
    @DisplayName("IOredir after piping")
    public void testIoRedirWithPipe() {
        try {
            shell.parseAndEvaluate("ls | grep pipetest.* | wc > output.txt", out);
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("output.txt")));
            assertEquals("       1       1      13" + StringUtils.STRING_NEWLINE, str1);
    
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "output.txt"));
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testMultipleIoRedir() {
        try {
            shell.parseAndEvaluate("sort < sortingFile.txt > sortedFile.txt", out);
            
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("sortedFile.txt")));
            assertEquals("ab" + StringUtils.STRING_NEWLINE +
              "mm" + StringUtils.STRING_NEWLINE +
              "za" + StringUtils.STRING_NEWLINE +
              "zc" + StringUtils.STRING_NEWLINE, str1);
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "sortedFile.txt"));
            
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testIoRedirWithWc() {
        try {
            shell.parseAndEvaluate("wc  -l < wcf.txt", out);
            
            assertEquals("       0" + StringUtils.STRING_NEWLINE, out.toString());
            
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testIoRedirWithRm() {
        try {
            Files.createFile(Paths.get(Environment.currentDirectory, "toDelete"));
            shell.parseAndEvaluate("rm toDelete > res.txt", out);
            
            assertFalse(Files.exists(Paths.get(Environment.currentDirectory, "toDelete")));
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "res.txt"));
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testChainIoRedir() {
        try {
            shell.parseAndEvaluate("grep -ci match < grepFile.txt > matched.txt", out);
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("matched.txt")));
            assertEquals("4" + StringUtils.STRING_NEWLINE, str1);
    
            Files.deleteIfExists(Paths.get(Environment.currentDirectory, "matched.txt"));
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    public void testIORedirFileAsStdInSed() {
        try {
            shell.parseAndEvaluate("sed 's|abc|def|2' < sedFile.txt", out);
            assertEquals("abc def abc abc" + StringUtils.STRING_NEWLINE +
              "def abf abc def" + StringUtils.STRING_NEWLINE +
              "hi nothing abhere" + StringUtils.STRING_NEWLINE, out.toString());
    
        } catch (Exception e) {
            fail();
        }
    }
    
    
}
