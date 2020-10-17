package hack.tests.pipe;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("PMD")
public class PipeTest {
    @Test
    void test01() throws IOException {
        SystemTestRunner.run("pipe/01");
    }

}
