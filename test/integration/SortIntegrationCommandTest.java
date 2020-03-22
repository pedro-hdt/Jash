package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SortIntegrationCommandTest {
    
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    
    private OutputStream stdout;
    private ShellImpl shell;
    private String cmdline = "";
    private String expected = "";
    
    @BeforeAll
    static void setupAll() {
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "SortIntegrationFolder");
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
          + StringUtils.fileSeparator() + "SortIntegrationFolder");
        stdout.flush();
    }
    
    @Test
    public void testSortThenEcho() throws ShellException, AbstractApplicationException {
        expected = "G" + STRING_NEWLINE +
          "f" + STRING_NEWLINE +
          "E" + STRING_NEWLINE +
          "d" + STRING_NEWLINE +
          "C" + STRING_NEWLINE +
          "b" + STRING_NEWLINE +
          "A" + STRING_NEWLINE +
          "8" + STRING_NEWLINE +
          "6" + STRING_NEWLINE +
          "4" + STRING_NEWLINE +
          "1" + STRING_NEWLINE +
          "*" + STRING_NEWLINE +
          "&" + STRING_NEWLINE +
          "$" + STRING_NEWLINE +
          "#" + STRING_NEWLINE +
          "sorttest.txt" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt; echo sorttest.txt";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenEchoNegative() throws ShellException, AbstractApplicationException {
        expected = "sort: No such file or directory" + STRING_NEWLINE +
          "sorttest.txt" + STRING_NEWLINE;
        cmdline = "sort -nrfe sorttest.txt; echo sorttest.txt";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenGrep() throws ShellException, AbstractApplicationException {
        expected = "8" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt | grep 8";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenGrepNegative() throws ShellException, AbstractApplicationException {
        expected = "";
        cmdline = "sort -nrf sorttest.txt | grep ABC";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenCut() throws ShellException, AbstractApplicationException {
        expected = "G" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt | cut -b 1 -";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenCutNegative() throws ShellException, AbstractApplicationException {
        expected = "" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt | cut -b 2 -";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenFind() throws ShellException, AbstractApplicationException {
        expected = "G" + STRING_NEWLINE +
          "f" + STRING_NEWLINE +
          "E" + STRING_NEWLINE +
          "d" + STRING_NEWLINE +
          "C" + STRING_NEWLINE +
          "b" + STRING_NEWLINE +
          "A" + STRING_NEWLINE +
          "8" + STRING_NEWLINE +
          "6" + STRING_NEWLINE +
          "4" + STRING_NEWLINE +
          "1" + STRING_NEWLINE +
          "*" + STRING_NEWLINE +
          "&" + STRING_NEWLINE +
          "$" + STRING_NEWLINE +
          "#" + STRING_NEWLINE +
          "../WcIntegrationFolder/wctest.txt" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt; find ../ -name wctest.txt";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testSortThenFindNegative() throws ShellException, AbstractApplicationException {
        expected = "G" + STRING_NEWLINE +
          "f" + STRING_NEWLINE +
          "E" + STRING_NEWLINE +
          "d" + STRING_NEWLINE +
          "C" + STRING_NEWLINE +
          "b" + STRING_NEWLINE +
          "A" + STRING_NEWLINE +
          "8" + STRING_NEWLINE +
          "6" + STRING_NEWLINE +
          "4" + STRING_NEWLINE +
          "1" + STRING_NEWLINE +
          "*" + STRING_NEWLINE +
          "&" + STRING_NEWLINE +
          "$" + STRING_NEWLINE +
          "#" + STRING_NEWLINE;
        cmdline = "sort -nrf sorttest.txt; find ../ -name wcteste.txt";
        
        shell.parseAndEvaluate(cmdline, stdout);
        assertEquals(expected, stdout.toString());
    }
}
