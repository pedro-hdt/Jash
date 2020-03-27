package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class PasteApplication implements PasteInterface {
    public String streamToString(InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        ArrayList<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while(line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin arguments. If only one Stdin
     * arg is specified, echo back the Stdin.
     *
     * @param stdin InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String mergeStdin(InputStream stdin) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        ArrayList<String> lines = new ArrayList<>();
        String line = reader.readLine();
        while(line != null) {
            lines.add(line);
            line = reader.readLine();
        }

        return String.join(STRING_NEWLINE, lines);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files. If only one file is
     * specified, echo back the file content.
     *
     * @param fileName Array of file names to be read and merged
     * @throws Exception
     */
    @Override
    public String mergeFile(String... fileName) throws Exception {
        if (fileName == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        ArrayList<String> outputRows =
                new ArrayList<>();

        ArrayList<ArrayList<String> > linesList =
                new ArrayList<>(fileName.length);
        ArrayList<String> filesAndDirectoriesNotFound = new ArrayList<>();
        for (String f : fileName) {
            try {
                ArrayList<String> fLines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(resolveFilePath(f).toFile()));
                String line = reader.readLine();
                while (line != null) {
                    fLines.add(line);
                    line = reader.readLine();
                }
                reader.close();
                linesList.add(fLines);
            } catch (FileNotFoundException e) {
                if (!new File(f).exists()) {
                    filesAndDirectoriesNotFound.add(f + " " + ERR_FILE_NOT_FOUND);
                } else {
                    filesAndDirectoriesNotFound.add(f + " " + ERR_IS_DIR);
                }
            } catch (IOException e) {
                throw new Exception(e.getMessage());
            }
        }
        if (filesAndDirectoriesNotFound.size() > 0) {
            String errorMessage = String.join(System.lineSeparator(), filesAndDirectoriesNotFound);
            throw new Exception(errorMessage);
        }

        boolean isLinesListEmpty = false;
        while (!isLinesListEmpty) {
            ArrayList<String> currRow = new ArrayList<>();
            isLinesListEmpty = true;
            for (int i = 0; i < linesList.size(); i++) {
                ArrayList<String> currLines = linesList.get(i);
                if (currLines.size() > 0) {
                    isLinesListEmpty = false;
                    currRow.add(currLines.remove(0));
                } else {
                    currRow.add("");
                }
            }
            if (currRow.size() > 0 && !isLinesListEmpty) {
                outputRows.add(String.join(Character.toString(CHAR_TAB), currRow));
            }
        }

        return String.join(STRING_NEWLINE, outputRows);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) Stdin input streams. If only one stdin is
     * specified, echo back the Stdin.
     *
     * @param stdins Array of Stdin input streams to be read and merged
     * @throws Exception
     */
    public String mergeStdins(InputStream... stdins) throws Exception {
        if (stdins == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        ArrayList<String> outputRows =
                new ArrayList<>();

        ArrayList<ArrayList<String> > linesList =
                new ArrayList<>(stdins.length);
        for (InputStream s : stdins) {
            try {
                ArrayList<String> sLines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(s));
                String line = reader.readLine();
                while (line != null) {
                    sLines.add(line);
                    line = reader.readLine();
                }
                reader.close();
                linesList.add(sLines);
            } catch (IOException e) {
                throw new Exception(e.getMessage());
            }
        }

        boolean isLinesListEmpty = false;
        while (!isLinesListEmpty) {
            ArrayList<String> currRow = new ArrayList<>();
            isLinesListEmpty = true;
            for (int i = 0; i < linesList.size(); i++) {
                ArrayList<String> currLines = linesList.get(i);
                if (currLines.size() > 0) {
                    isLinesListEmpty = false;
                    currRow.add(currLines.remove(0));
                } else {
                    currRow.add("");
                }
            }
            if (currRow.size() > 0 && !isLinesListEmpty) {
                outputRows.add(String.join(Character.toString(CHAR_TAB), currRow));
            }
        }

        return String.join(STRING_NEWLINE, outputRows);
    }

    /**
     * Returns string of line-wise concatenated (tab-separated) files and Stdin arguments.
     *
     * @param stdin    InputStream containing arguments from Stdin
     * @param fileName Array of file names to be read and merged
     * @throws Exception
     */
    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws Exception {
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        if (fileName == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        ArrayList<String> result = new ArrayList<>();

        String resultForFileNames = mergeFile(fileName);
        ArrayList<String> resultForFileNamesLines = new ArrayList<>(Arrays.asList(resultForFileNames.split("\\r?\\n", -1)));

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        ArrayList<String> stdinLines = new ArrayList<>();
        while(reader.ready()) {
            String line = reader.readLine();
            stdinLines.add(line);
        }

        for (String stdLine : stdinLines) {
            String resultForFileNamesLine = resultForFileNamesLines.remove(0);
            String row = "";
            if (resultForFileNamesLine != null) {
                row = resultForFileNamesLine + CHAR_TAB + stdLine;
            } else {
                row = CHAR_TAB + stdLine;
            }
            result.add(row);
        }

        for (String resultForFileNamesLine : resultForFileNamesLines) {
            result.add(resultForFileNamesLine);
        }

        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Runs the paste application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws SortException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        if (stdout == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }
        if (stdin == null) {
            throw new PasteException(ERR_NULL_STREAMS);
        }

        // Parse args
        PasteArgsParser parser = new PasteArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage());
        }

        // Get outputString
        String outputString = "";
        try {
            List<String> fileNames = parser.getFiles();
            if (fileNames.size() == 0) {
                outputString = mergeStdin(stdin);
            } else if (fileNames.contains("-")) {
                ArrayList<String> filesAndDirectoriesNotFound = new ArrayList<>();
                ArrayList<InputStream> fStreams = new ArrayList<>();
                String contentFromStdin = streamToString(stdin);
                for (String f : fileNames) {
                    try {
                        InputStream fStream;
                        if (f.equals("-")) {
                            fStream = new ByteArrayInputStream(contentFromStdin.getBytes());
                        } else {
                            fStream = new FileInputStream(f);
                        }
                        fStreams.add(fStream);
                    } catch (FileNotFoundException e) {
                        if (!new File(f).exists()) {
                            filesAndDirectoriesNotFound.add(f + ": " + ERR_FILE_NOT_FOUND);
                        } else {
                            filesAndDirectoriesNotFound.add(f + ": " + ERR_IS_DIR);
                        }
                    }
                }
                if (filesAndDirectoriesNotFound.size() > 0) {
                    String errorMessage = String.join(System.lineSeparator(), filesAndDirectoriesNotFound);
                    throw new PasteException(errorMessage);
                } else {
                    outputString = mergeStdins(fStreams.toArray(new InputStream[0]));
                }
            } else {
                outputString = mergeFile(fileNames.toArray(new String[0]));
            }
        } catch (Exception e) {
            throw new PasteException(e.getMessage());
        }

        // Write outputString to stdout
        try {
            stdout.write(outputString.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new PasteException(ERR_WRITE_STREAM);
        }
    }
}
