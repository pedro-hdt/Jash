package hack.tests.cut;

// passed tests are commented

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("PMD")
public class CutTest {
    @Test
    void test10() throws IOException {
        SystemTestRunner.run("cut/10");
    }

    @Test
    void test11() throws IOException {
        SystemTestRunner.run("cut/11");
    }

    @Test
    void test12() throws IOException {
        SystemTestRunner.run("cut/12");
    }

//    @Test
//    void test13() throws IOException {
//        SystemTestRunner.run("cut/13");
//    }
}
