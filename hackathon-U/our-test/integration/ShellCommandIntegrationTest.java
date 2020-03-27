package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;

public class ShellCommandIntegrationTest {
    
    public static final String ORIGINAL_DIR = Environment.currentDirectory;
    private OutputStream stdout;
    
    private ShellImpl shell = new ShellImpl();
    
    
    @BeforeAll
    static void setupAll() {
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "ShellCommandFolder");
    }
    
    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }
    
    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "ShellCommandFolder");
        
        stdout.flush();
    }
    
    @Test
    @DisplayName("sequence with pipe")
    public void testSequenceWithPipe() {
        try {
            shell.parseAndEvaluate("ls | grep empty ; echo 'non-empty'", stdout);
            assertEquals("empty.txt" + StringUtils.STRING_NEWLINE + "non-empty" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("quotes with pipe")
    public void testQuotePipe() {
        try {
            shell.parseAndEvaluate("echo \"hello there\" | sed \"s/hello/hi/\"", stdout);
            assertEquals("hi there" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("quotes with two pipe")
    public void testMultiplePipesWithQuotes() {
        try {
            shell.parseAndEvaluate("echo \"hello there\" | sed \"s/hello/hi/\" | sed \"s/there/robots/\"", stdout);
            assertEquals("hi robots" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("quotes with two pipe")
    public void testPipeWithIoRedir() {
        try {
            shell.parseAndEvaluate("echo \"no changes\" > newone.txt | sed \"s/no/some/\"", stdout);
    
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("newone.txt")));
    
            assertEquals("no changes" + StringUtils.STRING_NEWLINE, str1);
            Path filePath1 = Paths.get(Environment.currentDirectory, "newone.txt");
            Files.delete(filePath1);
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("input and output ioredir")
    public void testMultipleIoRedir() {
        try {
            shell.parseAndEvaluate("wc < wc.txt > output.txt", stdout);
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("output.txt")));
    
            assertEquals("       1       2       8" + StringUtils.STRING_NEWLINE, str1);
            Path filePath1 = Paths.get(Environment.currentDirectory, "output.txt");
            Files.delete(filePath1);
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("ioredir cp")
    public void testIoRedirWithCp() {
        try {
            shell.parseAndEvaluate("cp a b > a", stdout);
    
            Path filePath1 = Paths.get(Environment.currentDirectory, "a");
            Path filePath2 = Paths.get(Environment.currentDirectory, "b");
            assertTrue(Files.exists(filePath1));
            assertTrue(Files.exists(filePath2));
    
            Files.delete(filePath1);
            Files.delete(filePath2);
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("ioredir pass data")
    public void testIoRedirPassing() throws IOException {
        try {
            shell.parseAndEvaluate("echo yo > z > x > y", stdout);
            fail();
    
        } catch (Exception e) {
            assertTrue(e instanceof ShellException); //NOPMD
            assertMsgContains(e, "shell: Multiple streams provided");
            Path filePath1 = Paths.get(Environment.currentDirectory, "z");
            Files.delete(filePath1);
        }
    }
    
    @Test
    @DisplayName("ioredir and sequence. shows separation of states")
    public void testSequenceAndIORedir() {
        try {
            shell.parseAndEvaluate("sort -n old.txt > new.txt ; mv new.txt old.txt", stdout);
            assertEquals("", stdout.toString());
    
            Path filePath1 = Paths.get(Environment.currentDirectory, "old.txt");
            String str1 = new String(Files.readAllBytes(filePath1));
            assertEquals("2" + StringUtils.STRING_NEWLINE + "15" + StringUtils.STRING_NEWLINE +
              "100" + StringUtils.STRING_NEWLINE, str1);
    
    
            FileOutputStream outputStream = new FileOutputStream(filePath1.toFile()); //NOPMD
            byte[] strToBytes = ("15" + StringUtils.STRING_NEWLINE +
              "2" + StringUtils.STRING_NEWLINE + "100").getBytes();
            outputStream.write(strToBytes);
            outputStream.close();
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("lots of spaces to ignore after tokenising")
    public void testTokenise() {
        try {
            shell.parseAndEvaluate("      echo        hi        ", stdout);
            assertEquals("hi" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("lots of spaces to ignore after tokenising but handle quotes")
    public void testTokeniseWithQuote() {
        try {
            shell.parseAndEvaluate("      echo     ''   hi   ' '     ", stdout);
            assertEquals(" hi  " + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("paste - < wc.txt | grep f > grepOut.txt ; echo `wc -c grepOut.txt` ; rm grepOut.txt")
    public void testComplexChain1() {
        try {
            shell.parseAndEvaluate(
              "paste - < wc.txt | grep f > grepOut.txt ; echo `wc -c grepOut.txt` ; rm grepOut.txt",
              stdout);
            assertEquals("9 grepOut.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("wc -c `ls > lsOut.txt ; sed s/empty/full/ < lsOut.txt | grep ls` | " +
      "grep [0-9]+ > grepOut.txt ; paste grepOut.txt ; mv grepOut.txt .. ; " +
      "cd .. ; rm grepOut.txt ; cd ShellCommandFolder ; ls | cut -c 1-9 ; rm lsOut.txt")
    public void testComplexChain2() {
        try {
            shell.parseAndEvaluate(
              "wc -c `ls > lsOut.txt ; sed s/empty/full/ < lsOut.txt | grep ls` | " +
                "grep [0-9]+ > grepOut.txt ; paste grepOut.txt ; mv grepOut.txt .. ; " +
                "cd .. ; rm grepOut.txt ; cd ShellCommandFolder ; ls | cut -c 1-9 ; rm lsOut.txt",
              stdout);
            assertEquals("      35 lsOut.txt" + StringUtils.STRING_NEWLINE + "empty.txt" + StringUtils.STRING_NEWLINE,
              stdout.toString());
            assertFalse(Files.exists(IOUtils.resolveFilePath("lsOut.txt")));
            assertFalse(Files.exists(IOUtils.resolveFilePath("grepOut.txt")));
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("find . -name `ls | sort | paste - | grep empt` > findOut.txt ; " +
      "echo `paste findOut.txt` > echoOut.txt ; diff echoOut.txt findOut.txt | " +
      "cut -c 1-10 ; rm *Out.txt ")
    public void testComplexChain3() {
        try {
            shell.parseAndEvaluate(
              "find . -name `ls | sort | paste - | grep empt` > findOut.txt ; " +
                "echo `paste findOut.txt` > echoOut.txt ; diff echoOut.txt findOut.txt | " +
                "cut -c 1-10 ; rm *Out.txt ",
              stdout);
            assertEquals(StringUtils.STRING_NEWLINE, stdout.toString());
            assertFalse(Files.exists(IOUtils.resolveFilePath("findOut.txt")));
            assertFalse(Files.exists(IOUtils.resolveFilePath("echoOut.txt")));
        } catch (Exception e) {
            fail();
        }
    }
    
    
    @Test
    @DisplayName("echo \"`echo hello`\" | grep a > grepOut.txt ; echo `paste grepOut.txt` ; " +
      "echo `rm \"grepOut.txt\"`")
    public void testComplexChain4() {
        try {
            shell.parseAndEvaluate(
              "echo \"`echo hello`\" | grep a > grepOut.txt ; echo `paste grepOut.txt` ; " +
                "echo `rm \"grepOut.txt\"`",
              stdout);
            assertEquals(StringUtils.STRING_NEWLINE + StringUtils.STRING_NEWLINE, stdout.toString());
            assertFalse(Files.exists(IOUtils.resolveFilePath("grepOut.txt")));
        } catch (Exception e) {
            fail();
        }
    }
}
