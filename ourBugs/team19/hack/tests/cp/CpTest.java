package hack.tests.cp;

// passed tests are commented

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@SuppressWarnings("PMD")
public class CpTest {
    // cp non-empty-folder non-existing-folder
    // groupX
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("cp/20");
    }

    // cp *.txt folder
    // ls folder
    // groupQ
//    @Test
//    void test21() throws IOException {
//        SystemTestRunner.run("cp/21");
//    }

//    @Test
//    void test01() throws IOException {
//        SystemTestRunner.run("cp/01");
//    }
//
//    @Test
//    void test02() throws IOException {
//        SystemTestRunner.run("cp/02");
//    }
}
