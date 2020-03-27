package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CutInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.parser.CutArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;

public class CutApplication implements CutInterface {
    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @param fileName Array of String of file names
     * @return
     * @throws Exception
     */
    @Override
    public String cutFromFiles(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, String... fileName) throws Exception {
        if (isCharPo == null || isBytePo == null || isRange == null || fileName == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        ArrayList<String> result = new ArrayList<>();
        try {
            for (String f : fileName) {
                File file = resolveFilePath(f).toFile();
                if (file.exists() && file.isFile()) {
                    InputStream fStream = new FileInputStream(file);
                    result.add(cutFromStdin(isCharPo, isBytePo, isRange, startIdx, endIdx, fStream));
                } else if (file.exists() && file.isDirectory()) {
                    throw new Exception(f + ": " + ERR_IS_DIR);
                } else {
                    throw new Exception(f + ": " + ERR_FILE_NOT_FOUND);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return String.join(System.lineSeparator(), result);
    }

    /**
     * Cuts out selected portions of each line
     *
     * @param isCharPo Boolean option to cut by character position
     * @param isBytePo Boolean option to cut by byte position
     * @param isRange  Boolean option to perform range-based cut
     * @param startIdx index to begin cut
     * @param endIdx   index to end cut
     * @param stdin    InputStream containing arguments from Stdin
     * @return
     * @throws Exception
     */
    @Override
    public String cutFromStdin(Boolean isCharPo, Boolean isBytePo, Boolean isRange, int startIdx, int endIdx, InputStream stdin) throws Exception {
        if (isCharPo == null || isBytePo == null || isRange == null || stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        ArrayList<String> result = new ArrayList<>();
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(stdin);
            ArrayList<Integer> indices = new ArrayList<>();

            if (isRange) {
                for (int i = startIdx; i <= endIdx; i++) {
                    indices.add(i);
                }
            } else {
                indices.add(startIdx);
                if (endIdx != startIdx) {
                    indices.add(endIdx);
                }
            }

            for (String l : lines) {
                String cutLine = "";
                for (int i : indices) {
                    if (isCharPo) {
                        cutLine = cutLine + l.charAt(i - 1);
                    } else if (isBytePo) {
                        byte[] bytes = {l.getBytes()[i - 1]};
                        cutLine = cutLine + new String(bytes);
                    } else {
                        throw new Exception("Need -c or -b");
                    }
                }
                result.add(cutLine);
            }
        } catch (StringIndexOutOfBoundsException e) {
            throw new Exception(ERR_OUT_RANGE);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Exception(ERR_OUT_RANGE);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        return String.join(System.lineSeparator(), result);
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CutException {
        if (args == null) {
            throw new CutException(ERR_NULL_ARGS);
        }
        if (stdin == null || stdout == null) {
            throw new CutException(ERR_NULL_STREAMS);
        }
        CutArgsParser parser = new CutArgsParser();

        try {
            ArrayList<String> result = new ArrayList<>();

            parser.parse(args);
            boolean isCharPo = parser.isCutByCharPos();
            boolean isBytePo = parser.isCutByBytePos();
            boolean isRange = parser.isRange();
            int startIdx = Integer.parseInt(parser.getStartAndEndIndex().get(0));
            int endIdx = Integer.parseInt(parser.getStartAndEndIndex().get(1));
            String[] files = parser.getFiles().toArray(new String[0]);

            if (files.length == 0) {
                result.add(cutFromStdin(isCharPo, isBytePo, isRange, startIdx, endIdx, stdin));
            } else if (Arrays.asList(files).contains("-")) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].equals("-")) {
                        result.add(cutFromStdin(isCharPo, isBytePo, isRange, startIdx, endIdx, stdin));
                    } else {
                        InputStream fStream = new FileInputStream(resolveFilePath(files[i]).toFile());
                        result.add(cutFromStdin(isCharPo, isBytePo, isRange, startIdx, endIdx, fStream));
                    }
                }
            } else {
                result.add(cutFromFiles(isCharPo, isBytePo, isRange, startIdx, endIdx, files));
            }

            stdout.write(String.join(System.lineSeparator(), result).getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            if (e.getMessage().equals("Stream Closed")) {
                throw new CutException(ERR_WRITE_STREAM);
            } else {
                throw new CutException(e.getMessage());
            }
        } catch (Exception e) {
            throw new CutException(e.getMessage());
        }
    }
}
