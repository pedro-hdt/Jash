package hack.tests.rm;

// passed tests are commented

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class RmTest {

    // rm
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("rm/20");
    }

    // rm -r .
    // groupQ
    // careful, if you run this test, likely the whole project will be delete
    @Test
    void test21() throws IOException {
        fail();
        // SystemTestRunner.run("rm/21");
    }

    // __mkdir folder
    // __touch folder/file
    // cd folder
    // rm file
    // groupQ
//    @Test
//    void test22() throws IOException {
//        SystemTestRunner.run("rm/22");
//    }

    // __mkdir folder
    // cd folder
    // rm -d ../folder
    // ls
    // groupQ
    // groupX
    @Test
    void test23() throws IOException {
        SystemTestRunner.run("rm/23");
    }
}
