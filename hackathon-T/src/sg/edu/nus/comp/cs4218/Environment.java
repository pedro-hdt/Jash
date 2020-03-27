package sg.edu.nus.comp.cs4218;

import java.nio.file.Files;

import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.LongVariable"})
public final class Environment {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");

    /**
     * Sets the current directory of Shell and returns it.
     *
     * @param path has to be a valid absolute path
     */
    public static void setCurrentDirectory(String path) {
        if (Files.isDirectory(IOUtils.resolveFilePath(path))) {
            currentDirectory = IOUtils.resolveFilePath(path).toString();
        }
    }

    /**
     * @return currentDirectory
     */
    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    public static final String ORIGINAL_DIRECTORY = currentDirectory;
    public static final String LS_TEST_DIRECTORY = String.format("%s/testFiles/ls", currentDirectory);
    public static final String LS_UNIT_DIRECTORY = String.format("%s/testFiles/ls/unit", currentDirectory);
    public static final String SORT_TEST_DIRECTORY = String.format("%s/testFiles/sort", currentDirectory);
    public static final String SORT_TEST_DIRECTORY2 = String.format("%s/test/tdd/util/dummyTestFolder/SortTestFolder", currentDirectory);
    public static final String FIND_TEST_DIRECTORY = String.format("%s/testFiles/find/root", currentDirectory);
    public static final String FIND_RESULT_DIRECTORY = String.format("%s/testFiles/find", currentDirectory);
    public static final String MV_TEST_DIRECTORY = String.format("%s/testFiles/mv/root", currentDirectory);
    public static final String PASTE_TEST_DIRECTORY = String.format("%s/testFiles/paste/test", currentDirectory);
    public static final String PASTE_RESULT_DIRECTORY = String.format("%s/testFiles/paste/test/result", currentDirectory);
    public static final String CUT_TEST_DIRECTORY = String.format("%s/testFiles/cut", currentDirectory);
    public static final String RM_TEST_DIRECTORY = String.format("%s/testFiles/rm", currentDirectory);
    public static final String SED_TEST_DIRECTORY = String.format("%s/testFiles/sed", currentDirectory);
    public static final String REGEX_TEST_DIRECTORY = String.format("%s/testFiles/regex", currentDirectory);
    public static final String CD_TEST_DIRECTORY = String.format("%s/testFiles/cd", currentDirectory);
    public static final String CP_TEST_DIRECTORY = String.format("%s/testFiles/cp", currentDirectory);
    public static final String GREP_TEST_DIRECTORY = String.format("%s/testFiles/grep", currentDirectory);
    public static final String WC_TEST_DIRECTORY = String.format("%s/testFiles/wc", currentDirectory);

    public static final String CALL_TEST_DIRECTORY = String.format("%s/testFiles/call/test", currentDirectory);
    public static final String CALL_RESULT_DIRECTORY = String.format("%s/testFiles/call/result", currentDirectory);
    public static final String PIPE_TEST_DIRECTORY = String.format("%s/testFiles/pipe/test", currentDirectory);
    public static final String PIPE_RESULT_DIRECTORY = String.format("%s/testFiles/pipe/result", currentDirectory);
    public static final String SEQUENCE_TEST_DIRECTORY = String.format("%s/testFiles/sequence/test", currentDirectory);
    public static final String SEQUENCE_RESULT_DIRECTORY = String.format("%s/testFiles/sequence/result", currentDirectory);
    public static final String IOREDIRECT_TEST_DIRECTORY = String.format("%s/testFiles/ioredirect", currentDirectory);
    public static final String DIFF_TEST_DIRECTORY = String.format("%s/testFiles/diff", currentDirectory);

    public static final String INTEGRATION_LS_DIRECTORY = String.format("%s/testFiles/integration/ls", currentDirectory);

    private Environment() {
    }
}
