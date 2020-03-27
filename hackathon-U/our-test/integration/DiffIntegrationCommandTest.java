package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class DiffIntegrationCommandTest {
    
    public static final String ORIGINAL_DIR = Environment.currentDirectory;
    
    private OutputStream stdout;
    private ShellImpl shell;
    
    @BeforeAll
    static void setupAll() {
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "DiffIntegrationFolder");
    }
    
    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(ORIGINAL_DIR);
    }
    
    @BeforeEach
    public void setup() {
        shell = new ShellImpl();
        stdout = new ByteArrayOutputStream();
    }
    
    @AfterEach
    public void resetCurrentDirectory() throws IOException {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "DiffIntegrationFolder");
        stdout.flush();
    }
    
    @Test
    public void testEchoThenDiff() throws Exception {
        String expected = "Files [- difftest.txt] differ" + STRING_NEWLINE;
        String argument = "echo difftestdifftest | diff -q - difftest.txt";
        shell.parseAndEvaluate(argument, stdout);
        
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testEchoThenDiff2() throws Exception {
        String expected = "Files [- difftest.txt] are identical" + STRING_NEWLINE;
        String argument = "echo 'difftest!!!!!!!' | diff -s - difftest.txt";
        shell.parseAndEvaluate(argument, stdout);
        
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testPasteThenDiff() throws Exception {
        String cmdline = "paste difftest.txt | diff difftest.txt - ";
        String expected = "";
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testPasteThenDiff2() throws Exception {
        String cmdline = "paste difftest.txt | diff -s difftest.txt - ";
        String expected = "Files [difftest.txt -] are identical" + STRING_NEWLINE;
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testPasteThenDiff3() throws Exception {
        String cmdline = "paste difftest.txt | diff -q difftest.txt - ";
        String expected = "";
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
}
