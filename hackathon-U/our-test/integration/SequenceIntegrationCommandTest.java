package integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.ShellImpl;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class SequenceIntegrationCommandTest {
    
    public static final String INTG_FOLDER = "IntegrationTestFolder";
    public static final String SEQUENCE_FOLDER = "SequencingFolder";
    public static final String TEMPORARY_FILE = "temporaryFile.txt";
    public static final String EMPTY_FILE = "emptyFile.txt";
    public static final String TEXT_FILE = "textFile.txt";
    
    
    public static final String ORIGINAL_DIR = Environment.currentDirectory;
    public static final String DUMMY_TEST_FOLDER = "dummyTestFolder";
    
    ShellImpl shell = new ShellImpl();
    OutputStream stdout = new ByteArrayOutputStream();
    
    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + DUMMY_TEST_FOLDER
          + StringUtils.fileSeparator() + INTG_FOLDER);
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
          + StringUtils.fileSeparator() + DUMMY_TEST_FOLDER
          + StringUtils.fileSeparator() + INTG_FOLDER);
        
        stdout.flush();
    }

    /**
     * When exception thrown shows as error message and no Shell Exception
     */
    @Test
    public void testErrorOneOfSequenceException() {
        try {
            shell.parseAndEvaluate("ls ; mv empty ", stdout);
            assertTrue(stdout.toString().contains("No files provided"));
        } catch (Exception e) {
            fail();
        }

    }
    
    /**
     * Tests integration of a command that depends on action of first one
     */
    @Test
    @DisplayName("cd .. ; ls")
    public void testCdAndThenLs() throws ShellException, AbstractApplicationException {
        shell.parseAndEvaluate("cd .. ; ls", stdout);
    
        assertTrue(stdout.toString().contains(INTG_FOLDER));
    }
    
    /**
     * Tests integration where first command fails but second command passes
     */
    @Test
    @DisplayName("cd directory find folder -name file")
    public void testIncorrectCDAndFindCommand() throws ShellException, AbstractApplicationException {
        shell.parseAndEvaluate("cd NotPresentFolder ; find SequencingFolder -name emptyFile.txt", stdout);
        assertTrue(stdout.toString().contains("cd: NotPresentFolder: No such file or directory"));
        assertTrue(stdout.toString().contains("SequencingFolder/emptyFile.txt"));
    }
    
    /**
     * Tests integration of cd with rm
     */
    @Test
    @DisplayName("cd directory ; rm file")
    public void testCDAndThenRm() throws ShellException, AbstractApplicationException, IOException {
        Path path = Files.createFile(Paths.get(Environment.currentDirectory, SEQUENCE_FOLDER, TEMPORARY_FILE));
        shell.parseAndEvaluate("cd SequencingFolder ; rm temporaryFile.txt", stdout);
        assertFalse(Files.exists(path));
    }
    
    /**
     * Tests integration of cp with cd
     */
    @Test
    @DisplayName("cp file folder ; cd directory")
    public void testCPAndThenCD() throws IOException, ShellException, AbstractApplicationException {
        Path path = Files.createFile(Paths.get(Environment.currentDirectory, TEMPORARY_FILE));
        shell.parseAndEvaluate("cp temporaryFile.txt SequencingFolder; cd SequencingFolder", stdout);
        assertTrue(Files.exists(path));
    
        Files.delete(Paths.get(Environment.currentDirectory, TEMPORARY_FILE));
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + DUMMY_TEST_FOLDER
          + StringUtils.fileSeparator() + "IntegrationTestFolder");
        Files.delete(Paths.get(Environment.currentDirectory, TEMPORARY_FILE));
    }
    
    /**
     * Tests sequencing of two echo commands
     */
    @Test
    @DisplayName("echo args ; echo args")
    public void testEchoAndEcho() throws ShellException, AbstractApplicationException {
        shell.parseAndEvaluate("echo abc aaa ; echo bcg ola", stdout);
        assertEquals("abc aaa" + StringUtils.STRING_NEWLINE + "bcg ola" + StringUtils.STRING_NEWLINE,
          stdout.toString());
    }
    
    /**
     * Tests sequencing of wc and paste
     */
    @Test
    public void testWCAndPaste() throws ShellException, AbstractApplicationException {
    
        shell.parseAndEvaluate("cd SequencingFolder; wc -c textFile.txt; paste textFile.txt", stdout);
        assertEquals("      90 textFile.txt" + StringUtils.STRING_NEWLINE
          + "Sometimes tests need to be written for marks. You are a lucky person to take this module." + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    /**
     * Tests sequencing with exit in the end
     */
    @Test
    public void testSequencingWithExitAtEnd() {
        assertThrows(ExitException.class, () -> shell.parseAndEvaluate("echo hi; exit", stdout));
        assertEquals("hi" + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    /**
     * Tests sequencing with exit in the middle
     */
    @Test
    public void testSequencingWithExitInMiddle() {
        assertThrows(ExitException.class, () -> shell.parseAndEvaluate("echo hi; exit; echo bye", stdout));
        assertEquals("hi" + StringUtils.STRING_NEWLINE + "bye" + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    /**
     * Tests sequencing with sort command
     */
    @Test
    public void testSequencingWithSortAndSort() throws ShellException, AbstractApplicationException {
        shell.parseAndEvaluate("cd SequencingFolder ; sort textFile.txt", stdout);
        assertEquals("Sometimes tests need to be written for marks. You are a lucky person to take this module."
          + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    /**
     * Tests integration of mv with cd and ls
     */
    @Test
    public void testMVAndThenLsCommand() throws ShellException, AbstractApplicationException, IOException {
        Path path = Files.createFile(Paths.get(Environment.currentDirectory, TEMPORARY_FILE));
        shell.parseAndEvaluate("mv temporaryFile.txt SequencingFolder; cd SequencingFolder; ls", stdout);
        assertTrue(stdout.toString().contains(TEMPORARY_FILE));
        assertFalse(Files.exists(path)); //file not in original place anymore
    
        Files.delete(Paths.get(Environment.currentDirectory, TEMPORARY_FILE));
    }
    
    /**
     * Tests integration where third command depends on first but second fails
     */
    @Test
    public void testFirstAndThirdCommandPass() throws ShellException, AbstractApplicationException {
        shell.parseAndEvaluate("cd SequencingFolder; rm CommandSubsFolder; grep -i sometimes textFile.txt", stdout);
        assertTrue(stdout.toString().contains("CommandSubsFolder skipped"));
        assertTrue(stdout.toString().contains("Sometimes tests need to be written for marks."));
    }
    
    /**
     * Tests integration where one command is empty causing Shell Exception
     */
    @Test
    public void testWithSecondCommandEmpty() {
        ShellException expectedException = assertThrows(ShellException.class,
          () -> shell.parseAndEvaluate("cd SequencingFolder; ls; ;grep -i sometimes textFile.txt", stdout));
        assertEquals(expectedException.getMessage(), "shell: Invalid syntax");
        assertEquals("", stdout.toString());
    }
    
    /**
     * Tests integration where last command causes an exception causing command specific Exception
     */
    @Test
    public void testWithLastCommandCausingException() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("cd SequencingFolder; ls; grep", stdout);
        assertEquals(EMPTY_FILE + StringUtils.STRING_NEWLINE + TEXT_FILE + StringUtils.STRING_NEWLINE
          + "grep: Invalid syntax" + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    /**
     * Tests integration where middle command causes an exception causing command specific Exception
     */
    @Test
    public void testWithMiddleCommandCausingException() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("cd SequencingFolder; grep -i ; ls", stdout);
        assertEquals("grep: Invalid syntax" + StringUtils.STRING_NEWLINE + EMPTY_FILE
          + StringUtils.STRING_NEWLINE + TEXT_FILE + StringUtils.STRING_NEWLINE, stdout.toString());
    }
    
    @Test
    public void testFindWithCd() throws AbstractApplicationException, ShellException {
        shell.parseAndEvaluate("cd SequencingFolder; find . -name empty*", stdout);
        assertEquals("./emptyFile.txt" + StringUtils.STRING_NEWLINE, stdout.toString());
        
        Environment.setCurrentDirectory(ORIGINAL_DIR
          + StringUtils.fileSeparator() + DUMMY_TEST_FOLDER
          + StringUtils.fileSeparator() + INTG_FOLDER);
    }
    
    @Test
    public void testChangeAfterSequence() {
        
        try {
            Path file1 = Files.createFile(Paths.get(Environment.currentDirectory, "temp"));
            shell.parseAndEvaluate("ls ; rm temp ; ls", stdout);
            
            assertTrue(stdout.toString().contains(INTG_FOLDER));
            assertFalse(Files.exists(file1));
            
        } catch (Exception e) {
            fail();
        }
        
        
    }
    
}
