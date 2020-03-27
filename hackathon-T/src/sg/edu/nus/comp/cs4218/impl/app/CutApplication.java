package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.app.args.CutArguments;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplication implements CutInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (args == null) {
            throw new CutException(ERR_NULL_ARGS);
        }

        CutArguments parser = new CutArguments();
        try {
            parser.parse(args);
        } catch (CutException e) {
            throw e;
        } catch (InvalidArgsException iae) {
            throw new CutException(ERR_INVALID_FLAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (parser.getFileArgs().isEmpty()) {
//
//        }
        String result = "";
        int startIdx = parser.getStartIdx();
        int endIdx = parser.getEndIdx();
        try {
            for (ArrayList<String> currentArgs : parser.getFileArgs()) {
                if (currentArgs.size() == 1 && currentArgs.get(0).equals("-")) {
                    // stdin
                    result += cutFromStdin(parser.isCharPo(), parser.isBytePo(), parser.isRange(),
                            startIdx, endIdx, stdin);
                } else {
                    String[] stockArr = new String[currentArgs.size()];
                    result += cutFromFiles(parser.isCharPo(), parser.isBytePo(), parser.isRange(),
                            startIdx, endIdx, currentArgs.toArray(stockArr));

                }
                result = result + STRING_NEWLINE;
            }
        } catch (CutException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CutException(e.getMessage()); // NOPMD
        }

        try {
//            System.out.println(result);
            stdout.write(result.getBytes());
        } catch (IOException ioe) {
            throw new CutException(ERR_WRITE_STREAM);
        } catch (NullPointerException npe) {
            throw new CutException(ERR_NULL_STREAMS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, String... fileName) throws Exception {
        if (fileName == null) {
            throw new CutException(ERR_NULL_ARGS);
        }
        StringBuilder result = new StringBuilder();
        BufferedReader reader = null; //NOPMD

        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
//                System.err.println(ERR_FILE_NOT_FOUND + ": " + file + "\n");
                throw new CutException(ERR_FILE_NOT_FOUND);
//                continue;
            }
            if (node.isDirectory()) {
                throw new CutException(ERR_IS_DIR);
            }
            reader = new BufferedReader(new FileReader(node));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(cutSingleLine(isBytePo, isRange, startIdx, endIdx, line)).append(STRING_NEWLINE);
            }
            reader.close();
        }
        return result.toString().trim();
    }

    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        StringBuilder result = new StringBuilder();
        List<String> inputs = IOUtils.getLinesFromInputStream(stdin);
        for (String input : inputs) {
            String trimmedString = input.trim();
            result.append(cutSingleLine(isBytePo, isRange, startIdx, endIdx, trimmedString)).append(STRING_NEWLINE);
        }

        return result.toString();
    }

    private String cutSingleLine(Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, String trimmedString) {
        StringBuilder result = new StringBuilder();
        ;
        if (isRange) {
            result.append(cutRange(trimmedString, startIdx, endIdx, isBytePo));
        } else {
            if (startIdx == endIdx) {
                result.append(cutChar(trimmedString, startIdx, isBytePo));
            } else {
                result.append(cutChar(trimmedString, startIdx, isBytePo)).append(cutChar(trimmedString, endIdx, isBytePo));
            }
        }
        return result.toString();
    }

    private String cutChar(String originalString, int index, boolean isByte) {
        if (index > originalString.length() || index <= 0) {
            return "";
        }
        if (isByte) {
            return new String(new byte[]{originalString.getBytes()[index - 1]});
        } else {
            return String.valueOf(originalString.charAt(index - 1));
        }
    }

    private String cutRange(String originalString, int startIdx, int endIdx, boolean isByte) {
        int finalEndIdx = endIdx;
        if (startIdx >= originalString.length()) {
            return "";
        }
        if (endIdx > originalString.length()) {
            finalEndIdx = originalString.length();
        }
        String result = "";
        if (isByte) {
            byte[] byteArr = Arrays.copyOfRange(originalString.getBytes(), startIdx - 1, finalEndIdx);
            result = new String(byteArr);
        } else {
            result = originalString.substring(startIdx - 1, finalEndIdx);
        }
        return result;
    }
}
