package hack.tests.mv;

// passed tests are commented

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class MvTest {

    // mv CURRENT_FOLDER OTHER_DESTINATION; ls
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("mv/20");
    }

    // mv FOLDER FOLDER FOLDER
    // groupQ
//    @Test
//    void test21() throws IOException {
//        SystemTestRunner.run("mv/21");
//    }
}
