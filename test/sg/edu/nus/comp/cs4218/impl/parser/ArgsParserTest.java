package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.TestUtils.assertMsgContains;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

/**
 * Tests for ArgsParser
 * <p>
 * Positive test cases:
 * - empty argument array
 * - no flags
 * - basic command with only legal flags
 * <p>
 * Negative test cases:
 * - basic command with illegal flags
 */
class ArgsParserTest {

    @Test
    void testParseNoArgs() throws InvalidArgsException {

        ArgsParser argsParser = new ArgsParser();
        argsParser.legalFlags.addAll(Arrays.asList('a', 'b', 'c'));

        argsParser.parse(new String[0]);

        // assert non flag args are correct, there are no flags passed, and legal flags are as expcected
        assertEquals(Arrays.asList(), argsParser.nonFlagArgs);
        assertEquals(0, argsParser.flags.size());
        assertEquals(Arrays.asList('a', 'b', 'c'), new ArrayList<>(argsParser.legalFlags));

    }


    @Test
    void testParseNoFlags() {

        ArgsParser argsParser = new ArgsParser();

        String[] args = {"echo", "testing"};//NOPMD
        argsParser.parse(args);

        // assert non flag args are correct, there are no flags passed, and legal flags are as expcected
        assertEquals(Arrays.asList(args), argsParser.nonFlagArgs);
        assertEquals(0, argsParser.flags.size());

    }


    @Test
    void testParse() throws InvalidArgsException {

        List<Character> flagsPassed = Arrays.asList('a', 'b', 'c');

        ArgsParser argsParser = new ArgsParser();
        argsParser.legalFlags.addAll(flagsPassed);

        argsParser.parse(new String[]{"echo", "-a", "-b", "-c", "testing"});

        // assert non flag args are correct, all flags are legal, and legal flags are as expected
        assertEquals(Arrays.asList("echo", "testing"), argsParser.nonFlagArgs);
        assertEquals(argsParser.legalFlags, argsParser.flags);
        assertEquals(flagsPassed, new ArrayList<>(argsParser.legalFlags));


    }

    @Test
    void testFailsParseInvalidFlags() {

        ArgsParser argsParser = new ArgsParser();
        argsParser.legalFlags.addAll(Arrays.asList('a', 'b', 'c'));

        InvalidArgsException invalidArgsEx =
                assertThrows(
                        InvalidArgsException.class,
                        () -> argsParser.parse(new String[]{"echo", "-a", "-b", "-c", "-d", "testing"})
                );

        assertMsgContains(invalidArgsEx, ILLEGAL_FLAG_MSG);

    }
}