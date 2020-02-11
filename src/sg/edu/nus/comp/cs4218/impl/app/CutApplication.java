package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplication implements CutInterface { // TODO implement me

    private Boolean isCutByCharPos = false;
    private Boolean isCutByBytePos = false;
    private Boolean isRange = false;
    private int startIdx = 0;
    private int endIdx = 0;
    private String result = null;

    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, String... fileName) throws Exception {
        StringBuilder output = new StringBuilder();

        for (String srcPath : fileName) {
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
            if (isCharPo) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(IOUtils.openInputStream(srcPath)));
                int charCount = 0;
                int r;
                while ((r = reader.read()) != -1) {
                    char ch = (char) r;
                    if (isRange) {
                        if (charCount >= startIdx && charCount <= endIdx) {
                            output.append(ch);
                        }
                    } else {
                        if (charCount == startIdx || charCount == endIdx) {
                            output.append(ch);
                        }
                    }
                    charCount++;
                }
            } else if (isBytePo) {
                // TODO: FIND OUT HOW TO HANDLE BYTES IN JAVA FILES
            }
        }

        return output.toString();
    }

    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, InputStream stdin) throws Exception {
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
        if (args == null) {
            throw new CutException(ERR_NULL_ARGS);
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
                // ASSUMPTION 1: Input can ONLY be a list of comma separated numbers, a range of numbers or a single number
                // ASSUMPTION 2: Input can ONLY allow up to 2 comma separated numbers as the interface's method can't accept multiple positions
                String[] rangeParams = list.split(",");
                startIdx = Integer.parseInt(rangeParams[0]);
                endIdx = Integer.parseInt(rangeParams[1]);
            }

            if (files.length == 0 || (files.length == 1 && files[0].contains("-"))) {
                result = cutFromStdin(isCutByCharPos, isCutByBytePos, isRange, startIdx, endIdx, stdin);
                stdout.write(result.getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            } else {
                result = cutFromFiles(isCutByCharPos, isCutByBytePos, isRange, startIdx, endIdx, files);
                stdout.write(result.getBytes());
                stdout.write(STRING_NEWLINE.getBytes());
            }
        } catch (Exception e) {
            throw (CutException) new CutException(ERR_FILE_NOT_FOUND).initCause(e);
        }
    }
}
