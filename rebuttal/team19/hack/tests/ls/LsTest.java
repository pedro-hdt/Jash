package hack.tests.ls;

// passed tests are commented

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class LsTest {
//    @Test
//    void test01() throws IOException {
//        SystemTestRunner.run("ls/01");
//    }
//
//    @Test
//    void test03() throws IOException {
//        SystemTestRunner.run("ls/03");
//    }
//
//    @Test
//    void test04() throws IOException {
//        SystemTestRunner.run("ls/04");
//    }

    @Test
    void test05() throws IOException {
        SystemTestRunner.run("ls/05");
    }

    // this test was tested in Windows
    // ls -R
    // groupX
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("ls/20");
    }

    // this test was tested in Windows
    // ls -d */
    // groupX
    @Test
    void test21() throws IOException {
        SystemTestRunner.run("ls/21");
    }
}
