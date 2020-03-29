package hack.tests.exit;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExitTest {
    @Test
    void test01() throws IOException {
        SystemTestRunner.run("exit/01");
    }

    // exit <Extra-argument>
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("exit/20");
    }
    @Test
    void test10() throws IOException {
        SystemTestRunner.run("exit/10");
    }
}
