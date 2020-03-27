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

public class GrepIntegrationCommandTest {
    
    public static final String ORIGINAL_DIR = Environment.getCurrentDirectory();
    
    private OutputStream stdout;
    private ShellImpl shell;
    private String cmdLine = "";
    private String expected = "";
    
    @BeforeAll
    static void setupAll() {
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + "dummyTestFolder"
          + StringUtils.fileSeparator() + "IntegrationTestFolder"
          + StringUtils.fileSeparator() + "GrepIntegrationTestFolder");
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
          + StringUtils.fileSeparator() + "GrepIntegrationTestFolder");
        stdout.flush();
    }
    
    @Test
    public void testGrepThenSort() throws AbstractApplicationException, ShellException {
        cmdLine = "ls; grep sometimes textFile.txt";
        expected = "textFile.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testGrepTheSortReverse() throws AbstractApplicationException, ShellException {
        cmdLine = "grep -i sometimes textFile.txt; sort -r textFile.txt";
        expected = "Sometimes tests need to be written for marks. You are a lucky person to take this module." //NOPMD
          + StringUtils.STRING_NEWLINE + "That is why take CS4218, and learn software testing."
          + StringUtils.STRING_NEWLINE
          + "Sometimes tests need to be written for marks. You are a lucky person to take this module."
          + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testGrpThenWc() throws AbstractApplicationException, ShellException {
        cmdLine = "grep -c sometimes textFile.txt; wc -l textFile.txt";
        expected = "0" + StringUtils.STRING_NEWLINE
          + "       1 textFile.txt" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testInvalidGrepThenValidCommand() throws AbstractApplicationException, ShellException {
        cmdLine = "grep -z sometimes textFile.txt; paste textFile.txt";
        expected = "grep: Invalid syntax" + StringUtils.STRING_NEWLINE
          + "Sometimes tests need to be written for marks. You are a lucky person to take this module."
          + StringUtils.STRING_NEWLINE
          + "That is why take CS4218, and learn software testing." + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
    
    @Test
    public void testValidGrepThenInvalidCommand() throws AbstractApplicationException, ShellException {
        cmdLine = "grep -i sometimes textFile.txt; paste textsfile.txt";
        expected =
          "Sometimes tests need to be written for marks. You are a lucky person to take this module." + StringUtils.STRING_NEWLINE
            + "paste: textsfile.txt: No such file or directory" + StringUtils.STRING_NEWLINE;
        shell.parseAndEvaluate(cmdLine, stdout);
        assertEquals(expected, stdout.toString());
    }
}
