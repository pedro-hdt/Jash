package sg.edu.nus.comp.cs4218.impl.app.args;

import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.*;

import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class CutArguments {

    public static final char CHAR_BYTE = 'b';
    public static final char CHAR_CHARACTER = 'c';
    public static final char CHAR_LIST = ',';
    public static final char CHAR_RANGE = '-';
    private final List<String> files;
    protected Set<Character> flags;
    protected Set<Character> legalFlags;
    protected ArrayList<String> stdinArgment;
    private boolean rangeFlag, charPoFlag, bytePoFlag;
    private int startIdx, endIdx;
    private ArrayList<ArrayList<String>> argumentOrder;

    public CutArguments() {
        this.rangeFlag = false;
        this.charPoFlag = false;
        this.bytePoFlag = false;
        this.flags = new HashSet<>();
        legalFlags = new HashSet<>();
        this.files = new ArrayList<>();
        this.argumentOrder = new ArrayList<ArrayList<String>>();
        stdinArgment = new ArrayList<>(Collections.singletonList("-"));
    }

    /**
     * Handles argument list parsing for the `cut` application.
     *
     * @param args Array of arguments to parse
     * @throws Exception
     */
    public void parse(String... args) throws Exception { //NOPMD
        boolean parsingFlag = true, listCheck = true;
        ArrayList<String> currentFileList = new ArrayList<String>();
        for (String arg : args) {
            // Flag check
            if (arg.charAt(0) == CHAR_FLAG_PREFIX) {
                if (arg.length() == 1) {
                    // Read from stdin
                    if (!currentFileList.isEmpty()) {
                        argumentOrder.add(currentFileList);
                        currentFileList = new ArrayList<String>();
                    }
                    argumentOrder.add(stdinArgment);

                } else if (!parsingFlag) {
                    throw new CutException("Too many flags");
                } else {
                    String flags = arg.substring(1);
                    if (flags.length() != 1) { // Flags are multiple character
                        throw new InvalidArgsException(ILLEGAL_FLAG_MSG + flags);
                    }
                    char flag = flags.charAt(0);
                    if (flag == CHAR_BYTE) {
                        bytePoFlag = true;
                    } else if (flag == CHAR_CHARACTER) {
                        charPoFlag = true;
                    } else {
                        throw new InvalidArgsException(ILLEGAL_FLAG_MSG + flag);
                    }
                    parsingFlag = false;
                }
            } else if (Character.isDigit(arg.charAt(0))) { // check list or range
                // Check if already done it
                if (!listCheck) {
                    throw new CutException("Too many lists");
                }
                try {
                    String[] indexes;
                    if (arg.contains("" + CHAR_RANGE)) {
                        rangeFlag = true;
                        indexes = arg.split(String.valueOf(CHAR_RANGE));
                        startIdx = Integer.parseInt(indexes[0]);
                        endIdx = Integer.parseInt(indexes[1]);
                    } else if (arg.contains("" + CHAR_LIST)) {
                        rangeFlag = false;
                        indexes = arg.split(String.valueOf(CHAR_LIST));
                        startIdx = Integer.parseInt(indexes[0]);
                        endIdx = Integer.parseInt(indexes[1]);
                    } else {
                        startIdx = Integer.parseInt(arg);
                        endIdx = startIdx;
                    }
                    listCheck = false;
                } catch (Exception e) {
                    throw e;
                }
                if (startIdx <= 0) {
                    throw new CutException(ERR_OUT_RANGE);
                }
                if (endIdx < startIdx) {
                    throw new CutException(ERR_INVALID_RANGE);
                }

            } else { // Append files
                currentFileList.add(arg);
            }
        }
        if (listCheck) {
            throw new CutException(ERR_NO_ARGS);
        }
        if (!currentFileList.isEmpty()) {
            argumentOrder.add(currentFileList);
        }
        if (argumentOrder.isEmpty()) {
            argumentOrder.add(stdinArgment);
        }
    }

    public List<String> getFiles() {
        return files;
    }

    public ArrayList<ArrayList<String>> getFileArgs() {
        return argumentOrder;
    }

    public boolean isRange() {
        return rangeFlag;
    }

    public boolean isCharPo() {
        return charPoFlag;
    }

    public boolean isBytePo() {
        return bytePoFlag;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }
}
