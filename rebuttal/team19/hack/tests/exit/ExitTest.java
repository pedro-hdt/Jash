package hack.tests.exit;

import hack.util.SystemTestRunner;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ExitTest {
    @Test
    @Disabled("Invalid")
    void test01() throws IOException {
        SystemTestRunner.run("exit/01");
    }

    // exit <Extra-argument>
    // groupQ
    @Test
    @Disabled("Invalid")
    void test20() throws IOException {
        SystemTestRunner.run("exit/20");
    }
    @Test
    @Disabled("Invalid")
    void test10() throws IOException {
        SystemTestRunner.run("exit/10");
    }
}
