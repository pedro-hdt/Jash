import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class PasteApplicationTest {

    private static PasteApplication paste = new PasteApplication();

    @Test // TODO temporary
    public void pasteOutputTest() throws IOException, PasteException {

        Path f1 = Files.createTempFile("paste", "");
        Path f2 = Files.createTempFile("paste", "");

        List<String> lines1 = Arrays.asList("1", "2", "3", "4");
        List<String> lines2 = Arrays.asList("A", "B", "C", "D", "E", "F");

        Files.write(f1, lines1);
        Files.write(f2, lines2);

        String expected = "1\tA\t\n2\tB\t\n3\tC\t\n4\tD\t\n\tE\t\n\tF\t\n";

        String actual = paste.mergeFile(f1.toString(), f2.toString());
        System.out.println(actual);

        Assertions.assertEquals(expected, actual);

    }
}
