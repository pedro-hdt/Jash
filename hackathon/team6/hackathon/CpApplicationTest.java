package hackathon;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public class CpApplicationTest {

    private static final String FOLDER =    "rebuttal" + CHAR_FILE_SEP +
                                            "team6" + CHAR_FILE_SEP +
            "hackathon/files" + CHAR_FILE_SEP +
                                            "cp" + CHAR_FILE_SEP;
    private static final String TEMPT_FILE = FOLDER + "tempt.txt";

    private CpInterface cpApplication = new CpApplication();

    /**
     * This is bug is due to invalid handling cp exist file to non-exist folder.
     * Ref. spec page 17, Section 9.10. No assumption mentioned.
     *
     * Taking an example command `cp *.txt foo` as an example, if foo is not exist, your CpApplication would treat foo
     * as a file rather than a folder, which does meet the specification `cp [FILES] DIRECTORY.
     */
    // Invalid: if you call cp files to folder obviously we will assume a folder. when cp is called with run fucntion
    // this does not happen
    @Test
    public void testCpToNotExistFolder() throws IOException, AbstractApplicationException {

        Environment.setCurrentDirectory(FOLDER);
        Path f1 = Files.createTempFile(IOUtils.resolveFilePath(Environment.getCurrentDirectory()), "", ".txt");
        Path f2 = Files.createTempFile(IOUtils.resolveFilePath(Environment.getCurrentDirectory()), "", ".txt");

        cpApplication.run(new String[] {f1.toString(), f2.toString(), "not_exist"}, System.in, System.out);

        assertTrue(Files.exists(f1));
        assertTrue(Files.exists(f2));
        Path copy1 = Paths.get(Environment.getCurrentDirectory(), "not_exist", f1.getFileName().toString());
        Path copy2 = Paths.get(Environment.getCurrentDirectory(), "not_exist", f2.getFileName().toString());
        assertTrue(Files.exists(copy1));
        assertTrue(Files.exists(copy2));

        Files.deleteIfExists(f1);
        Files.deleteIfExists(f2);
        Files.deleteIfExists(copy1);
        Files.deleteIfExists(copy2);
        Files.deleteIfExists(copy1.getParent());
    }
}
