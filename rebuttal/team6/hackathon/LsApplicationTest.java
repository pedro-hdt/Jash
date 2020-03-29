package hackathon;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class LsApplicationTest {
    private final LsInterface lsApplication = new LsApplication();
    private OutputStream outputStream;

    @BeforeEach
    void setCurrentDirectory() {
        outputStream = new ByteArrayOutputStream();
    }


    @AfterEach
    void resetCurrentDirectory() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This bug is due to invalid to distinguishing relative path and absolute path in Windows OS.
     * No spec needed.
     */
    @Test
    void runWithValidArgs() {
        String[] args = {"-R", "-d"};
        assertDoesNotThrow(() -> {
            lsApplication.run(args, System.in, outputStream);
        });
    }
}
