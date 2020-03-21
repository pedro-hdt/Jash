package sg.edu.nus.comp.cs4218.impl.util;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.LongVariable"})
public class ErrorConstants {

    // Streams related
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";
    public static final String ERR_NULL_STREAMS = "Null Pointer Exception";
    public static final String ERR_CLOSING_STREAMS = "Unable to close streams";
    public static final String ERR_MULTIPLE_STREAMS = "Multiple streams provided";
    public static final String ERR_NO_OSTREAM = "OutputStream not provided";
    public static final String ERR_NO_ISTREAM = "InputStream not provided";
    public static final String ERR_NO_INPUT = "No InputStream and no filenames";
    public static final String ERR_NO_FILE_ARGS = "No files provided";

    // Arguments related
    public static final String ERR_MISSING_ARG = "Missing Argument";
    public static final String ERR_NO_ARGS = "Insufficient arguments";
    public static final String ERR_NULL_ARGS = "Null arguments";
    public static final String ERR_TOO_MANY_ARGS = "Too many arguments";
    public static final String ERR_INVALID_FLAG = "Invalid flag option supplied";

    // Files and folders related
    public static final String ERR_FOLDER_NOT_FOUND = "No such folder or directory";
    public static final String ERR_FILE_NOT_FOUND = "No such file or directory";
    public static final String ERR_IS_DIR = "This is a directory";
    public static final String ERR_IS_NOT_DIR = "Not a directory";
    public static final String ERR_NO_PERM = "Permission denied";

    // `sed` related
    public static final String ERR_NO_REP_RULE = "No replacement rule supplied";
    public static final String ERR_INVALID_REP_RULE = "Invalid replacement rule";
    public static final String ERR_INVALID_REP_X = "X needs to be a number greater than 0";
    public static final String ERR_INVALID_REGEX = "Invalid regular expression supplied";
    public static final String ERR_EMPTY_REGEX = "Regular expression cannot be empty";

    // `mv` related
    public static final String ERR_CANNOT_OVERWRITE = "Cannot overwrite";

    // `cut` related
    public static final String ERR_INVALID_RANGE = "Invalid range";
    public static final String ERR_OUT_OF_RANGE = "Out of range";

    // General constants
    public static final String ERR_INVALID_APP = "Invalid app";
    public static final String ERR_SYNTAX = "Invalid syntax";
    public static final String ERR_GENERAL = "Exception Caught";
    public static final String ERR_IO_EXCEPTION = "IOException";

}
