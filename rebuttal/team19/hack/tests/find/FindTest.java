package hack.tests.find;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FindTest {
    // find folder -name file-name
    // groupX
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("find/20");
    }

    //find folder file
    // Bad message
    // groupX
    @Test
    void test21() throws IOException {
        SystemTestRunner.run("find/21");
    }
}
