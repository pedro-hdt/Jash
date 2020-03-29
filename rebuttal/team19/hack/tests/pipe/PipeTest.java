package hack.tests.pipe;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class PipeTest {
    @Test
    void test01() throws IOException {
        SystemTestRunner.run("pipe/01");
    }

}
