package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
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

public class GlobbingIntegrationTest {
    
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    private OutputStream stdout;
    
    private ShellImpl shell = new ShellImpl();
    
    
    @BeforeAll
    static void setupAll() {
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "GlobbingFolder");
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
          + StringUtils.fileSeparator() + "GlobbingFolder");
        
        stdout.flush();
    }
    
    @Test
    @DisplayName("Globbing with ls")
    public void testGlobLs() {
        try {
            shell.parseAndEvaluate("ls dir/*", stdout);
            assertEquals("dir" + StringUtils.fileSeparator() + "empty.txt" + StringUtils.STRING_NEWLINE, stdout.toString()); //NOPMD
        } catch (Exception e) {
            fail();
        }
    }
    
    
    @Test
    @DisplayName("Globbing with mv")
    public void testGlobMv() {
        try {
    
            String mvDir = "mvDir";
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), mvDir));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "first.mv"));
            Path filePath2 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "second.mv"));
    
            shell.parseAndEvaluate("mv fir*.mv mvDir", stdout);
    
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, mvDir, "first.mv")));
            assertFalse(Files.exists(Paths.get(Environment.currentDirectory, mvDir, "second.mv")));
    
            Files.delete(filePath2);
            Files.delete(Paths.get(Environment.currentDirectory, mvDir, "first.mv"));
            Files.delete(dirPath);
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("Globbing with cp")
    public void testGlobCp() {
        try {
    
            String cpDir = "cpDir";
            Path dirPath = Files.createDirectory(Paths.get(Environment.getCurrentDirectory(), cpDir));
            Path filePath1 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "first.cp"));
            Path filePath2 = Files.createFile(Paths.get(Environment.getCurrentDirectory(), "second.cp"));
    
            shell.parseAndEvaluate("cp *.cp cpDir", stdout);
    
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, cpDir, "first.cp")));
            assertTrue(Files.exists(Paths.get(Environment.currentDirectory, cpDir, "second.cp")));
    
            Files.delete(filePath1);
            Files.delete(filePath2);
            Files.delete(Paths.get(Environment.currentDirectory, cpDir, "first.cp"));
            Files.delete(Paths.get(Environment.currentDirectory, cpDir, "second.cp"));
            Files.delete(dirPath);
    
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("echo with **")
    public void testEchoGlob() {
        try {
            shell.parseAndEvaluate("echo **", stdout);
            assertEquals("dir wc1.txt wc100.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("echo with * inside quotes")
    public void testEchoGlobQuote() {
        try {
            shell.parseAndEvaluate("echo \"*\"", stdout);
            assertEquals("*" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("quote with no result rm")
    public void testRmWithQuoteInvalid() {
        try {
            shell.parseAndEvaluate("rm *invalid*", stdout);
            fail();
    
        } catch (Exception e) {
            assertMsgContains(e, "*invalid* skipped: No such file or directory");
        }
    }
    
    @Test
    @DisplayName("wc with globs")
    public void testWcGlob() {
        try {
            shell.parseAndEvaluate("wc *.txt", stdout);
            assertEquals("       3       4      19 wc1.txt\n" +
              "       2       3      14 wc100.txt\n" +
              "       5       7      33 total" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("cut with globs")
    public void testCutGlob() {
        try {
            shell.parseAndEvaluate("cut -b 2-10 *.txt", stdout);
            assertEquals("irst1"
              + StringUtils.STRING_NEWLINE
              + "hel" + StringUtils.STRING_NEWLINE
              + "undred" + StringUtils.STRING_NEWLINE
              + "by" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("paste with globs")
    public void testPasteGlob() {
        try {
            shell.parseAndEvaluate("paste *.txt", stdout);
            assertEquals("first1\thundred" + StringUtils.STRING_NEWLINE +
              "hello\tbye" + StringUtils.STRING_NEWLINE +
              "yo\tno" + StringUtils.STRING_NEWLINE +
              "boy\t" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    
    @Test
    @DisplayName("invalid with cd")
    public void testInvalidCd() {
        try {
            shell.parseAndEvaluate("cd *", stdout);
            fail();
        } catch (Exception e) {
            assertEquals("cd: Too many arguments", e.getMessage());
        }
    }
    
    @Test
    @DisplayName("sort with globs")
    public void testSortGlobs() {
        try {
            shell.parseAndEvaluate("sort *.txt", stdout);
            assertEquals("boy"
              + StringUtils.STRING_NEWLINE + "bye"
              + StringUtils.STRING_NEWLINE + "first1"
              + StringUtils.STRING_NEWLINE + "hello"
              + StringUtils.STRING_NEWLINE + "hundred"
              + StringUtils.STRING_NEWLINE + "no"
              + StringUtils.STRING_NEWLINE + "yo" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    
    @Test
    @DisplayName("find with globs invalid")
    public void testFindGlobInvalid() {
        try {
            shell.parseAndEvaluate("find ./ -name *", stdout);
            fail();
        } catch (Exception e) {
            assertMsgContains(e, "find: Only one filename is allowed");
        }
    }
    
    @Test
    @DisplayName("find with globs as regex")
    public void testFindGlob() {
        try {
            shell.parseAndEvaluate("find ./ -name \"*\"", stdout);
            assertEquals("./dir"
              + StringUtils.STRING_NEWLINE + "./dir/empty.txt"
              + StringUtils.STRING_NEWLINE + "./wc1.txt"
              + StringUtils.STRING_NEWLINE + "./wc100.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("multiple globs as sequence")
    public void testMultipleGlobs1() {
        try {
            shell.parseAndEvaluate("ls * ; echo *", stdout);
            assertEquals("dir:"
              + StringUtils.STRING_NEWLINE + "empty.txt"
              + StringUtils.STRING_NEWLINE + ""
              + StringUtils.STRING_NEWLINE + "wc1.txt"
              + StringUtils.STRING_NEWLINE + "wc100.txt"
              + StringUtils.STRING_NEWLINE + "dir wc1.txt wc100.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("multiple globs as ioredir. not same as unix. assumption made")
    public void testMultipleGlobs2() {
        try {
    
            shell.parseAndEvaluate("paste *.t*t > random.mult2.txt", stdout);
            String str1 = new String(Files.readAllBytes(IOUtils.resolveFilePath("random.mult2.txt")));
            Path filePath1 = Paths.get(Environment.getCurrentDirectory(), "random.mult2.txt");
            Files.delete(filePath1);
    
            assertEquals("\tfirst1\thundred" + StringUtils.STRING_NEWLINE +
              "\thello\tbye" + StringUtils.STRING_NEWLINE +
              "\tyo\tno" + StringUtils.STRING_NEWLINE +
              "\tboy\t" + StringUtils.STRING_NEWLINE, str1);
        } catch (Exception e) {
            fail();
        }
    }
    
    
    @Test
    @DisplayName("multiple globs as pipe 1")
    public void testMultipleGlobs3() {
        try {
            shell.parseAndEvaluate("paste *.txt | wc", stdout);
    
            assertEquals("       4       7      36" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("multiple globs as pipe 2")
    public void testMultipleGlobs4() {
        try {
            shell.parseAndEvaluate("paste *.txt | wc *", stdout);
    
            assertEquals("wc: This is a directory"
              + StringUtils.STRING_NEWLINE + "       3       4      19 wc1.txt"
              + StringUtils.STRING_NEWLINE + "       2       3      14 wc100.txt"
              + StringUtils.STRING_NEWLINE + "       5       7      33 total" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("multiple globs as cmd subs")
    public void testMultipleGlobs5() {
        try {
            shell.parseAndEvaluate("ls `echo *` *", stdout);
    
            assertEquals("dir:"
              + StringUtils.STRING_NEWLINE + "empty.txt"
              + StringUtils.STRING_NEWLINE + ""
              + StringUtils.STRING_NEWLINE + "wc1.txt"
              + StringUtils.STRING_NEWLINE + "wc100.txt"
              + StringUtils.STRING_NEWLINE + "dir:"
              + StringUtils.STRING_NEWLINE + "empty.txt"
              + StringUtils.STRING_NEWLINE + ""
              + StringUtils.STRING_NEWLINE + "wc1.txt"
              + StringUtils.STRING_NEWLINE + "wc100.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
    @Test
    @DisplayName("multiple globs as withing find")
    public void testMultipleGlobs6() {
        try {
            shell.parseAndEvaluate("find di* -name emp*.*", stdout);
            assertEquals("dir" + StringUtils.fileSeparator() + "empty.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        } catch (Exception e) {
            fail();
        }
    }
    
}
