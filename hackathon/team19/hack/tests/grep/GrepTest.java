package hack.tests.grep;

import hack.util.SystemTestRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class GrepTest {
//    @Test
//    void test01() throws IOException {
//        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
//                    SystemTestRunner.run("grep/01");
//                }
//        );
//    }

    @Test
    void test10() throws IOException {
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> {
                    SystemTestRunner.run("grep/10");
                }
        );
    }
}
