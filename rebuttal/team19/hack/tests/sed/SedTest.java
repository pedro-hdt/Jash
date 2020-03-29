package hack.tests.sed;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SedTest {
    @Test
    void test10() throws IOException {
        SystemTestRunner.run("sed/10");
    }

    @Test
    void test11() throws IOException {
        SystemTestRunner.run("sed/11");
    }

    @Test
    void test01() throws IOException {
        SystemTestRunner.run("sed/01");
    }
}
