package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.app.GrepApplication.EMPTY_PATTERN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;


public class GrepApplicationTest {

    private static GrepApplication grepApplication;
    private static OutputStream stdout;

    static final String originalDir = Environment.getCurrentDirectory();


    @BeforeAll
    static void setupAll() {
        Environment.setCurrentDirectory(originalDir + File.separator + "dummyTestFolder" + File.separator + "GrepTestFolder");
    }

    @AfterAll
    static void reset() {
        Environment.setCurrentDirectory(originalDir);
    }


    @BeforeEach
    void setUp() {
        grepApplication = new GrepApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
    }

    @Test
    public void testGrepWithNoInput(){
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String []{"-i"}, null, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_NO_INPUT));
    }

    @Test
    public void testGrepWithNullPattern(){
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String []{"-i"}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

    @Test
    public void testGrepsWithIncorrectOption() {
        Exception expectedException = assertThrows(GrepException.class, () -> grepApplication.run(new String[] {"-z"}, System.in, stdout));
        assertTrue(expectedException.getMessage().contains(ERR_SYNTAX));
    }

//    @Test
//    public void testGrepWithNoMatchingPattern(){
//        grepApplication.run(new String[] {"pattern "})
//
//    }

}
