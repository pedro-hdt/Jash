package hack.tests.diff;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("PMD")
public class DiffTest {

    @Test
    void test10() throws IOException {
        SystemTestRunner.run("diff/10");
    }
}
