package hackathon;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public class CpApplicationTest {

    private static final String FOLDER =    "test" + CHAR_FILE_SEP +
                                            "hackathon" + CHAR_FILE_SEP +
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
    @Test
    public void testCpToNotExistFolder() {
        assertThrows(Exception.class,
                () -> cpApplication.cpFilesToFolder("non_exist", TEMPT_FILE));
    }
}
