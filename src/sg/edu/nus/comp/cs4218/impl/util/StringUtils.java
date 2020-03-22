package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;

public final class StringUtils {
    public static final String STRING_NEWLINE = System.lineSeparator();
    public static final String STRING_CURR_DIR = ".";
    public static final String STRING_PARENT_DIR = "..";
    public static final char CHAR_FILE_SEP = File.separatorChar;
    public static final char CHAR_TAB = '\t';
    public static final char CHAR_SPACE = ' ';
    public static final char CHAR_DOUBLE_QUOTE = '"';
    public static final char CHAR_SINGLE_QUOTE = '\'';
    public static final char CHAR_BACK_QUOTE = '`';
    public static final char CHAR_REDIR_INPUT = '<';
    public static final char CHAR_REDIR_OUTPUT = '>';
    public static final char CHAR_PIPE = '|';
    public static final char CHAR_SEMICOLON = ';';
    public static final char CHAR_ASTERISK = '*';
    public static final char CHAR_FLAG_PREFIX = '-';
    
    private StringUtils() {
    }
    
    /**
     * Returns the file separator defined for a particular system.
     * Used for RegexArgument parsing only.
     *
     * @return String of file separator
     */
    public static String fileSeparator() {
        // We need to escape \ in Windows...
        if (System.getProperty("os.name").toLowerCase().contains("win")) {//NOPMD
            return '\\' + File.separator;
        }
        return File.separator;
    }
    
    /**
     * Check if string contains only whitespace
     *
     * @param str String to be checked
     * @return true under any one of the 3 conditions:
     * 1. string is null
     * 2. string is empty
     * 3. string contains only whitespace
     */
    public static boolean isBlank(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
    
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
    
        return true;
    }
    
    /**
     * Tokenize a string delimited by whitespace
     *
     * @param str String to be tokenized
     * @return String array containing the tokens
     */
    public static String[] tokenize(String str) {
        if (isBlank(str)) {
            return new String[0];
        }
        return str.trim().split("\\s+");
    }
}
