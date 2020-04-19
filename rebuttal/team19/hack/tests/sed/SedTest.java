package hack.tests.sed;

import hack.util.SystemTestRunner;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SedTest {
    @Test
    @Disabled("Invalid duplicate")
    void test10() throws IOException {
        SystemTestRunner.run("sed/10");
    }

    @Test
    @Disabled("Invalid")
    void test11() throws IOException {
        SystemTestRunner.run("sed/11");
    }

    @Test
    @Disabled("Invalid")
    void test01() throws IOException {
        SystemTestRunner.run("sed/01");
    }
}
