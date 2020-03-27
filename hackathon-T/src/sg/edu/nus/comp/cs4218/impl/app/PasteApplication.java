package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.getLinesFromInputStream;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplication implements PasteInterface {
    @Override
    public String mergeStdin(InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        List<String> lines = getLinesFromInputStream(stdin);
        return String.join(STRING_NEWLINE, lines);
    }

    @Override
    public String mergeFile(String... fileName) throws Exception {
        List<String> result;
        List<InputStream> inputs = new ArrayList<>();
        List<BufferedReader> readers = new ArrayList<>();
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            checkFiles(node);

            InputStream input = IOUtils.openInputStream(file);
            inputs.add(input);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            readers.add(reader);
        }
        if (inputs.size() == 1) {
            result = getLinesFromInputStream(inputs.get(0));
        } else {
            result = merge(readers);
        }
        return String.join(STRING_NEWLINE, result);
    }

    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws Exception {
        List<String> result;
        List<BufferedReader> readers = new ArrayList<>();
        List<String> lines = getLinesFromInputStream(stdin);
        for (String file : fileName) {
            if ("-".equals(file)) {
                BufferedReader buffer =
                        new BufferedReader(new StringReader(String.join("\n", lines)));
                ;
                readers.add(buffer);
            } else {
                File node = IOUtils.resolveFilePath(file).toFile();
                checkFiles(node);

                InputStream input = IOUtils.openInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                readers.add(reader);
            }
        }
        result = merge(readers);
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Checks the file for whether it can be found, or whether it is a directory, or whether it can be read.
     *
     * @param node The file
     * @throws PasteException
     */
    public void checkFiles(File node) throws PasteException {
        if (!node.exists()) {
            throw new PasteException(ERR_FILE_NOT_FOUND);
        }
        if (node.isDirectory()) {
            throw new PasteException(ERR_IS_DIR);
        }
        if (!node.canRead()) {
            throw new PasteException(ERR_NO_PERM);
        }
    }

    /**
     * merges the files/stdin into one list of strings by reading them line by line, separated by tab characters.
     *
     * @param readers A list of BufferedReaders for each file or stdin
     * @return List of Strings
     * @throws Exception
     */
    private List<String> merge(List<BufferedReader> readers) throws Exception {
        List<String> result = new ArrayList<>();
        int numReachedEOF = 0;
        boolean[] filesReachedEOF = new boolean[readers.size()];
        while (numReachedEOF < readers.size()) {
            StringBuilder currentLine = new StringBuilder();
            for (int i = 0; i < readers.size(); i++) {
                BufferedReader reader = readers.get(i);
                if (i != 0) {
                    currentLine.append(CHAR_TAB);
                }
                if (!filesReachedEOF[i]) {
                    String line;
                    if ((line = reader.readLine()) == null) {
                        numReachedEOF++;
                        filesReachedEOF[i] = true;
                    } else {
                        currentLine.append(line);
                    }
                }
            }
            if (numReachedEOF < readers.size()) {
                result.add(currentLine.toString());
            }
        }
        return result;
    }


    /**
     * Runs the paste application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws PasteException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        String result;
        try {
            boolean hasFile = false;
            int numStdins = 0;
            for (String s : args) {
                if ("-".equals(s)) {
                    numStdins++;
                } else if (!"".equals(s)) {
                    hasFile = true;
                }
            }
            if (numStdins >= 1) {
                result = mergeFileAndStdin(stdin, args);
            } else if (numStdins == 1) {
                result = mergeStdin(stdin);
            } else { // no stdin
                if (hasFile) {
                    result = mergeFile(args);
                } else {
                    throw new PasteException(ERR_NO_INPUT);
                }
            }

        } catch (Exception e) {

            throw new PasteException(ERR_NO_INPUT);
        }
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new PasteException(ERR_WRITE_STREAM);
        }
    }
}
