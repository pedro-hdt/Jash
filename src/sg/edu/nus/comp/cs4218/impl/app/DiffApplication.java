package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.DiffInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.DiffException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.DiffArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.PreserveStackTrace")
public class DiffApplication implements DiffInterface {

    InputStream inputStream; //NOPMD

    /**
     * Checks if the source path exists in the current system.
     * @param srcPath Source path of the file or directory.
     * @throws DiffException
     */
    public void checkExists(String srcPath) throws DiffException {
        if (srcPath != null && srcPath.equals("-")) {
            return;
        }
        File node = IOUtils.resolveFilePath(srcPath).toFile();
        if (!node.exists()) {
            throw new DiffException(ERR_FILE_NOT_FOUND);
        }
        if (!node.canRead()) {
            throw new DiffException(ERR_NO_PERM);
        }
    }

    /**
     * Reads the file's content and returns a string version of the file's content.
     * @param file File to be read
     * @return string version of the file's content
     * @throws Exception
     */
    public String readFileContent(File file) throws Exception {
        InputStream inputStream;  //NOPMD
        int fileLength;
        byte[] strBuffer;
        String fileContent;

        inputStream = new FileInputStream(file);
        fileLength = (int) file.length();
        strBuffer = new byte[fileLength];
        inputStream.read(strBuffer, 0, fileLength);
        fileContent = new String(strBuffer);
        inputStream.close();

        return fileContent;
    }

    /**
     * Performs a diff of two files.
     *
     * @param fileNameA  String of file name of the first file to be diff
     * @param fileNameB  String of file name of the second file to be diff
     * @param isShowSame Boolean option to print 'Files [file_names] identical' if the files are
     *                   identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Files [file_names] differ' if the files are
     *                   different
     * @return The difference in content
     * @throws DiffException
     */
    @Override
    public String diffTwoFiles(String fileNameA, String fileNameB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        String fileAContent;
        String fileBContent;
        String[] fileALines;
        String[] fileBLines;

        boolean isFileADir = false;
        boolean isFileBDir = false;

        try {
            if (fileNameA != null && fileNameA.equals("-")) {
                fileALines = IOUtils.getLinesFromInputStream(inputStream).toArray(new String[0]);
            } else {
                checkExists(fileNameA);
                File fileA = IOUtils.resolveFilePath(fileNameA).toFile();
                if (fileA.isDirectory()) {
                    isFileADir = true;
                }
                fileAContent = readFileContent(fileA);
                fileALines = fileAContent.split("\n");
            }

            if (fileNameB != null && fileNameB.equals("-")) {
                fileBLines = IOUtils.getLinesFromInputStream(inputStream).toArray(new String[0]);
            } else {
                checkExists(fileNameB);
                File fileB = IOUtils.resolveFilePath(fileNameB).toFile();
                if (fileB.isDirectory()) {
                    isFileBDir = true;
                }
                fileBContent = readFileContent(fileB);
                fileBLines = fileBContent.split("\n");
            }

            if (isFileADir && isFileBDir) {
                return diffTwoDir(fileNameA, fileNameB, isShowSame, isNoBlank, isSimple);
            } else {
                boolean[] commonLinesA = new boolean[fileALines.length];
                boolean[] commonLinesB = new boolean[fileBLines.length];

                // Check common lines
                for (int i = 0; i < fileALines.length; i++) {
                    String currLineA = fileALines[i];
                    for (int j = 0; j < fileBLines.length; j++) {
                        String currLineB = fileBLines[j];
                        if (currLineA.equals(currLineB)) {
                            commonLinesA[i] = true;
                            commonLinesB[j] = true;
                        } else if (isSimple) {
                            return "Files [" + fileNameA + " " + fileNameB + "] differ";
                        }
                    }
                }

                StringBuilder output = new StringBuilder();
                for (int i = 0; i < commonLinesA.length; i++) {
                    if (!commonLinesA[i]) {
                        if (isNoBlank && fileALines[i].isEmpty()) {
                            continue;
                        }
                        output.append("< " + fileALines[i] + "\n");
                    }
                }
                for (int j = 0; j < commonLinesB.length; j++) {
                    if (!commonLinesB[j]) {
                        if (isNoBlank && fileBLines[j].isEmpty()) {
                            continue;
                        }
                        output.append("> " + fileBLines[j] + "\n");
                    }
                }
                if (output.length() == 0 && isShowSame) {
                    return "Files [" + fileNameA + " " + fileNameB + "] are identical";
                }

                return output.toString();
            }
        } catch (Exception e) {
            throw new DiffException(e.getMessage());
        }
    }

    /**
     * Performs a diff of two directories.
     *
     * @param folderA    of path to first directory to diff
     * @param folderB    of path to second directory to diff
     * @param isShowSame Boolean option to print 'Files [file_names] identical' if the files are
     *                   identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Files [file_names] differ' if the files are
     *                   different
     * @return
     * @throws DiffException
     */
    @Override
    public String diffTwoDir(String folderA, String folderB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            checkExists(folderA);
            checkExists(folderB);

            File dirA = IOUtils.resolveFilePath(folderA).toFile();
            File dirB = IOUtils.resolveFilePath(folderB).toFile();
            String[] dirAFiles = dirA.list();
            String[] dirBFiles = dirB.list();
            List<String> listA = Arrays.asList(dirAFiles);
            List<String> listB = Arrays.asList(dirBFiles);

            String pathA = folderA + File.separator;
            String pathB = folderB + File.separator;
            boolean hasLines = false;

            for (int i = 0; i < dirAFiles.length; i++) {
                String message;
                if (listB.contains(dirAFiles[i])) {
                    String filePathA = pathA + dirAFiles[i];
                    String filePathB = pathB + dirAFiles[i];

                    message = diffTwoFiles(filePathA, filePathB, isShowSame, isNoBlank, isSimple);
                    if (!message.isEmpty()) {
                        if (hasLines) {
                            stringBuilder.append(STRING_NEWLINE);
                        }
                        hasLines = true;
                    }
                    stringBuilder.append(message);
                } else {
                    if (hasLines) {
                        stringBuilder.append(STRING_NEWLINE);
                    }
                    stringBuilder.append("Only in " + folderA + ": " + dirAFiles[i]);
                    hasLines = true;
                }
            }

            for (int i = 0; i < dirBFiles.length; i++) {
                String message;
                if (!listA.contains(dirBFiles[i])) {
                    if (hasLines) {
                        stringBuilder.append(STRING_NEWLINE);
                    }
                    message = "Only in " + folderB + ": " + dirBFiles[i];
                    stringBuilder.append(message);
                    hasLines = true;
                }
            }
            return new String(stringBuilder);
        } catch (Exception e) {
            throw new DiffException(e.getMessage());
        }
    }

    /**
     * Performs a diff of a file and stdin.
     *
     * @param fileName   String of file name of the file to be diff
     * @param stdin      InputStream of Stdin arg to diff
     * @param isShowSame Boolean option to print 'Inputs identical' if the files are identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Inputs differ' if the files are different
     * @return
     * @throws DiffException
     */
    @Override
    public String diffFileAndStdin(String fileName, InputStream stdin, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        try {
            inputStream = stdin;
            return diffTwoFiles(fileName, "-", isShowSame, isNoBlank, isSimple);
        } catch (Exception e) {
            throw new DiffException(e.getMessage());
        }
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdout == null || stdin == null) {
            throw new DiffException(ERR_NULL_STREAMS);
        }

        Boolean isShowSame;
        Boolean isNoBlank;
        Boolean isSimple;
        inputStream = stdin;

        DiffArgsParser parser = new DiffArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new DiffException(ERR_INVALID_FLAG);
        }

        try {
            isShowSame = parser.isShowSame();
            isNoBlank = parser.isNoBlank();
            isSimple = parser.isSimple();
            String[] files = parser.getFiles();

            if (files.length < 2) {
                throw new DiffException(ERR_NO_ARGS);
            }

            if (files.length > 2) {
                throw new DiffException(ERR_TOO_MANY_ARGS);
            }

            String source = files[0];
            String destination = files[1];

            String output = diffTwoFiles(source, destination, isShowSame, isNoBlank, isSimple);
            if (output.length() != 0) {
                output += STRING_NEWLINE;
            }
            stdout.write(output.getBytes());
        } catch (Exception e) {
            throw (DiffException) new DiffException(e.getMessage()).initCause(e);
        }
    }
}
