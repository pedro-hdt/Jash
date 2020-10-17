package hack.tests.wc;

import hack.util.SystemTestRunner;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.fail;

public class WcTest {

    // echo abc | wc
    // groupQ
    @Test
    void test20() throws IOException {
        SystemTestRunner.run("wc/20");
    }

//    @Test
//    @Disabled("Invalid duplicate")
//    void test01() throws IOException {
//        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
//                    SystemTestRunner.run("wc/01");
//                }
//        );
//    }
}
