package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;

/**
 * The cut command Cuts out selected portions of each line (as specified by list) from each file and writes them to the standard output.
 * If no file arguments are specified, cut from the standard input. Column numbering starts from 1.
 *
 * <p>
 * <b>Command format:</b> <code>cut [Option] [LIST] FILES...</code>
 * </p>
 */
public class CutApplication implements CutInterface {

    /**
     * Builds the output read from the current data processed.
     *
     * @param data     Data read from the input stream
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @param count    Current char/byte position
     * @return
     */
    public String buildOutput(int data, Boolean isRange, int startIdx, int endIdx, int count) {
        StringBuilder output = new StringBuilder();
        char currData = (char) data;

        if (isRange) {
            if (count >= startIdx && count <= endIdx) {
                output.append(currData);
            }
        } else {
            if (count == startIdx || count == endIdx) {
                output.append(currData);
            }
        }

        return output.toString();
    }

    /**
     * Processes the input stream and return a string.
     *
     * Process byte: http://tutorials.jenkov.com/java-io/inputstream.html
     * Process chars: https://stackoverflow.com/questions/811851/how-do-i-read-input-character-by-character-in-java
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param stdin    InputStream containing arguments from Stdin
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @return
     * @throws Exception If an I/O exception occurs.
     */
    public String processInput(Boolean isCharPo, Boolean isBytePo, InputStream stdin, Boolean isRange, int startIdx, int endIdx) throws Exception {
        StringBuilder output = new StringBuilder();

        int data;
        if (isBytePo) {
            int byteCount = 1; // to determine byte position

            // Read 1 byte at a time from the input stream
            while ((data = stdin.read()) != -1) {
                output.append(buildOutput(data, isRange, startIdx, endIdx, byteCount));
                byteCount++; // keeps track of the number of bytes that have been read
            }
        } else if (isCharPo) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            int charCount = 1;

            // Read 1 char at a time from the input stream
            while ((data = reader.read()) != -1) {
                output.append(buildOutput(data, isRange, startIdx, endIdx, charCount));
                charCount++; // keeps track of the number of chars that have been read
            }
        }

        stdin.close();
        return output.toString();
    }

    /**
     * Cut selected portions from each file and returns a string as output.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @param fileName Array of String of file names
     * @return
     * @throws Exception If an I/O exception occurs.
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, String... fileName) throws Exception {
        StringBuilder output = new StringBuilder();

        for (String srcPath : fileName) {
            // If not the first file, add carriage return
            if (output.length() != 0) {
                output.append("\n");
            }
            File node = IOUtils.resolveFilePath(srcPath).toFile();
            if (!node.exists()) {
                throw new Exception(ERR_FILE_NOT_FOUND);
            }
            if (node.isDirectory()) {
                throw new Exception(ERR_IS_DIR);
            }
            if (!node.canRead()) {
                throw new Exception(ERR_NO_PERM);
            }
            output.append(processInput(isCharPo, isBytePo, IOUtils.openInputStream(srcPath), isRange, startIdx, endIdx));
        }

        return output.toString();
    }

    /**
     * Cut selected portions from stdin and return a string as output.
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @param stdin    InputStream containing arguments from Stdin
     * @return
     * @throws Exception If an I/O exception occurs.
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, InputStream stdin) throws Exception {
        return processInput(isCharPo, isBytePo, stdin, isRange, startIdx, endIdx);
    }

    /**
     * Runs the cut application with the specified arguments.
     *
     * ASSUMPTION 1: Input can ONLY be a list of comma separated numbers, a range of numbers or a single number
     * ASSUMPTION 2: Input can ONLY allow up to 2 comma separated numbers as the interface's method can't accept multiple positions
     * ASSUMPTION 3: '0' should not be supplied as a single number, a range of numbers or comma separated numbers. The official command throws this error `cut: [-cf] list: values may not include zero`
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream. Elements of args will be output to stdout, separated by a
     *               space character.
     * @throws CutException If an I/O exception occurs.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
         if (stdout == null) {
            throw new CutException(ERR_NULL_STREAMS);
         }

         Boolean isCutByCharPos;
         Boolean isCutByBytePos;
         Boolean isRange;
         int startIdx;
         int endIdx;
         String result;

        if (args == null) {
            throw new CutException(ERR_NULL_ARGS);
        }

        if (args.length < 1) {
            throw new CutException(ERR_NO_ARGS);
        }

        CutArgsParser parser = new CutArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw (CutException) new CutException(e.getMessage()).initCause(e);
        }

        try {
            isCutByCharPos = parser.isCutByCharPos();
            isCutByBytePos = parser.isCutByBytePos();
            String[] files = parser.getFiles();

            String list = parser.getList();
            isRange = list.contains("-");
            if (isRange) {
                String[] rangeParams = list.split("-");
                startIdx = Integer.parseInt(rangeParams[0]);
                endIdx = Integer.parseInt(rangeParams[1]);
            } else if (list.length() == 1) {
                startIdx = Integer.parseInt(list);
                endIdx = Integer.parseInt(list);
            } else {
                String[] rangeParams = list.split(",");
                startIdx = Integer.parseInt(rangeParams[0]);
                endIdx = Integer.parseInt(rangeParams[1]);
            }

            // Read from stdin
            if (files.length == 0 || (files.length == 1 && files[0].contains("-"))) {
                if (stdin == null) {
                    throw new Exception(ERR_NULL_STREAMS);
                }
                result = cutFromStdin(isCutByCharPos, isCutByBytePos, isRange, startIdx, endIdx, stdin).trim();
                stdout.write(result.getBytes());
            } else { // Read from files
                result = cutFromFiles(isCutByCharPos, isCutByBytePos, isRange, startIdx, endIdx, files).trim();
                stdout.write(result.getBytes());
            }
        } catch (Exception e) {
            throw new CutException(e.getMessage());
        }
    }
}
