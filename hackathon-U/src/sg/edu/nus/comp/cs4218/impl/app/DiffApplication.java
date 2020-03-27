package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.DiffInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.DiffException;
import sg.edu.nus.comp.cs4218.impl.app.args.DiffArguments;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.resolveFilePath;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class DiffApplication implements DiffInterface {
    /**
     * Returns a string of files diff. The diff report contains a list of lines unique to the first
     * file and lines unique to the second file. Begin the former with '<' for each line. Begin the
     * latter with '>' for each line. Returns and empty string if there are no difference.
     *
     * @param fileNameA  String of file name of the first file to be diff
     * @param fileNameB  String of file name of the second file to be diff
     * @param isShowSame Boolean option to print 'Files [file_names] identical' if the files are
     *                   identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Files [file_names] differ' if the files are
     *                   different
     * @throws Exception
     */
    @Override
    public String diffTwoFiles(String fileNameA, String fileNameB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException, IOException {
        if (fileNameA == null || fileNameB == null || isShowSame == null || isNoBlank == null || isSimple == null) {
            throw new DiffException(ERR_NULL_ARGS);
        }

        File fileA = resolveFilePath(fileNameA).toFile();
        File fileB = resolveFilePath(fileNameB).toFile();

        if (!fileA.exists()) {
            throw new DiffException(fileA + ": " + ERR_FILE_NOT_FOUND);
        } else if (!fileB.exists()) {
            throw new DiffException(fileB + ": " + ERR_FILE_NOT_FOUND);
        } else if (fileA.isDirectory()) {
            throw new DiffException(fileA + ": " + ERR_IS_DIR);
        } else if (fileB.isDirectory()) {
            throw new DiffException(fileB + ": " + ERR_IS_DIR);
        }

        ArrayList<String> result = new ArrayList<>();
        String l;
        ArrayList<String> linesA = new ArrayList<>();
        ArrayList<String> linesB = new ArrayList<>();
        ArrayList<String> intersection = new ArrayList<>();

        BufferedReader readerA = new BufferedReader(new FileReader(resolveFilePath(fileNameA).toFile()));
        l = readerA.readLine();
        while (l != null) {
            linesA.add(l);
            l = readerA.readLine();
        }
        BufferedReader readerB = new BufferedReader(new FileReader(resolveFilePath(fileNameB).toFile()));
        l = readerB.readLine();
        while (l != null) {
            linesB.add(l);
            l = readerB.readLine();
        }
        readerA.close();
        readerB.close();

        if (isNoBlank) {
            linesA.removeAll(Arrays.asList(""));
            linesB.removeAll(Arrays.asList(""));
        }
        for (String line : linesA) {
            if (linesB.contains(line)) {
                intersection.add(line);
            }
        }

        linesA.removeAll(intersection);
        linesB.removeAll(intersection);

        if (linesA.size() == 0 && linesB.size() == 0) {
            if (isShowSame) {
                result.add("Files " + fileNameA + " " + fileNameB + " are identical");
            }
        } else {
            if (isSimple) {
                result.add("Files " + fileNameA + " " + fileNameB + " differ");
            } else {
                for (String line : linesA) {
                    String row = CHAR_REDIR_INPUT + " " + line;
                    result.add(row);
                }
                for (String line : linesB) {
                    String row = CHAR_REDIR_OUTPUT + " " + line;
                    result.add(row);
                }
            }
        }

        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns a string of folder diff. Non-recursively enter each folders and perform diff on each
     * files alphabetically. Report which files differ, common directories, and which files are
     * unique to which folder. For the files in the directory, the report contains a list of lines
     * unique to the first file and lines unique to the second file. Begin the former with '<' for
     * each line. Begin the latter with '>' for each line. Returns and empty string if there are no
     * difference.
     *
     * @param folderA    of path to first directory to diff
     * @param folderB    of path to second directory to diff
     * @param isShowSame Boolean option to print 'Files [file_names] identical' if the files are
     *                   identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Files [file_names] differ' if the files are
     *                   different
     * @throws Exception
     */
    @Override
    public String diffTwoDir(String folderA, String folderB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws Exception {
        if (folderA == null || folderB == null || isShowSame == null || isNoBlank == null || isSimple == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        File dirA = resolveFilePath(folderA).toFile();
        File dirB = resolveFilePath(folderB).toFile();

        if (!dirA.exists()) {
            throw new Exception(dirA + ": " + ERR_FILE_NOT_FOUND);
        } else if (!dirB.exists()) {
            throw new Exception(dirB + ": " + ERR_FILE_NOT_FOUND);
        } else if (dirA.isFile()) {
            throw new Exception(dirA + ": " + ERR_IS_NOT_DIR);
        }

        ArrayList<String> result = new ArrayList<>();

        ArrayList<String> fileAndFolderListA = new ArrayList<>(Arrays.asList(resolveFilePath(folderA).toFile().list()));
        ArrayList<String> fileAndFolderListB = new ArrayList<>(Arrays.asList(resolveFilePath(folderB).toFile().list()));
        ArrayList<String> intersectionFiles = new ArrayList<>();
        ArrayList<String> intersectionDirs = new ArrayList<>();

        for (String f : fileAndFolderListA) {
            if (fileAndFolderListB.contains(f)) {
                if (f.contains(".")) {
                    intersectionFiles.add(f);
                } else {
                    intersectionDirs.add(f);
                }
            }
        }

        fileAndFolderListA.removeAll(intersectionFiles);
        fileAndFolderListB.removeAll(intersectionFiles);
        fileAndFolderListA.removeAll(intersectionDirs);
        fileAndFolderListB.removeAll(intersectionDirs);

        Collections.sort(fileAndFolderListA);
        Collections.sort(fileAndFolderListB);
        Collections.sort(intersectionDirs);
        Collections.sort(intersectionFiles);

        if (fileAndFolderListA.size() > 0) {
            result.add("Only in " + folderA + ": " + String.join(" ", fileAndFolderListA));
        }
        if (fileAndFolderListB.size() > 0) {
            result.add("Only in " + folderB + ": " + String.join(" ", fileAndFolderListB));
        }
        if (intersectionDirs.size() > 0) {
            result.add("Common directories: " + String.join(" ", intersectionDirs));
        }
        if (intersectionFiles.size() > 0) {
            result.add("Common files: " + String.join(" ", intersectionFiles));
            for (String f : intersectionFiles) {
                String fileNameA = folderA + "/" + f;
                String fileNameB = folderB + "/" + f;
                String diff = diffTwoFiles(fileNameA, fileNameB, isShowSame, isNoBlank, isSimple);
                if (!diff.equals("")) {
                    result.add(diff);
                }
            }
        }

        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns a string of file and Stdin diff. The diff report contains a list of lines unique to
     * the file and lines unique to the Stdin. Begin the former with '<' for each line. Begin the
     * latter with '>' for each line. Returns and empty string if there are no difference.
     *
     * @param fileName   String of file name of the file to be diff
     * @param stdin      InputStream of Stdin arg to diff
     * @param isShowSame Boolean option to print 'Inputs identical' if the files are identical
     * @param isNoBlank  Boolean option to ignore changes with blank lines
     * @param isSimple   Boolean option to only print 'Inputs differ' if the files are different
     * @throws Exception
     */
    @Override
    public String diffFileAndStdin(String fileName, InputStream stdin, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws Exception {
        if (fileName == null || isShowSame == null || isNoBlank == null || isSimple == null) {
            throw new Exception(ERR_NULL_ARGS);
        }
        if (stdin == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        File file = resolveFilePath(fileName).toFile();
        if (!file.exists()) {
            throw new Exception(file + ": " + ERR_FILE_NOT_FOUND);
        } else if (file.isDirectory()) {
            throw new Exception(file + ": " + ERR_IS_DIR);
        }

        ArrayList<String> result = new ArrayList<>();
        String l;
        ArrayList<String> linesFile = new ArrayList<>();
        ArrayList<String> linesStdin = new ArrayList<>();
        ArrayList<String> intersection = new ArrayList<>();

        BufferedReader readerFile = new BufferedReader(new FileReader(resolveFilePath(fileName).toFile()));
        l = readerFile.readLine();
        while (l != null) {
            linesFile.add(l);
            l = readerFile.readLine();
        }
        BufferedReader readerStdin = new BufferedReader(new InputStreamReader(stdin));
        while(readerStdin.ready()) {
            l = readerStdin.readLine();
            linesStdin.add(l);
        }
        readerFile.close();
        readerStdin.close();

        if (isNoBlank) {
            linesFile.removeAll(Arrays.asList(""));
            linesStdin.removeAll(Arrays.asList(""));
        }
        for (String line : linesFile) {
            if (linesStdin.contains(line)) {
                intersection.add(line);
            }
        }

        linesFile.removeAll(intersection);
        linesStdin.removeAll(intersection);

        if (linesFile.size() == 0 && linesStdin.size() == 0) {
            if (isShowSame) {
                result.add("Files " + fileName + " " + "stdin" + " are identical");
            }
        } else {
            if (isSimple) {
                result.add("Files " + fileName + " " + "stdin" + " differ");
            } else {
                for (String line : linesFile) {
                    String row = CHAR_REDIR_INPUT + " " + line;
                    result.add(row);
                }
                for (String line : linesStdin) {
                    String row = CHAR_REDIR_OUTPUT + " " + line;
                    result.add(row);
                }
            }
        }

        return String.join(STRING_NEWLINE, result);
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws DiffException {
        String result = "";
        DiffArguments parser = new DiffArguments();
        try {
            parser.parse(args);
        } catch (Exception e) {
            throw new DiffException(e.getMessage());
        }

        List<String> files = parser.getFiles();
        List<String> dirs = parser.getDirectories();

        try {
            if (files.size() + dirs.size() > 2) {
                throw new DiffException(ERR_EXTRA_OPERAND);
            } else if (files.size() == 2) {
                if (!files.get(0).equals("-") && !files.get(1).equals("-")) {
                    result = diffTwoFiles(files.get(0), files.get(1), parser.isOutputSameFilesMessage(), parser.isIgnoreBlankLines(), parser.isOutputDifferentFilesMessage());
                } else if (files.get(0).equals("-") && files.get(1).equals("-")) {
                    if (parser.isOutputSameFilesMessage()) {
                        result = "Stdin" + " " + "stdin" + " are identical";
                    } else {
                        result = "";
                    }
                } else if (files.get(0).equals("-")) {
                    result = diffFileAndStdin(files.get(1), stdin, parser.isOutputSameFilesMessage(), parser.isIgnoreBlankLines(), parser.isOutputDifferentFilesMessage());
                } else if (files.get(1).equals("-")) {
                    result = diffFileAndStdin(files.get(0), stdin, parser.isOutputSameFilesMessage(), parser.isIgnoreBlankLines(), parser.isOutputDifferentFilesMessage());
                }
            } else if (dirs.size() == 2) {
                result = diffTwoDir(dirs.get(0), dirs.get(1), parser.isOutputSameFilesMessage(), parser.isIgnoreBlankLines(), parser.isOutputDifferentFilesMessage());
            } else {
                throw new DiffException(ERR_MISSING_OPERAND);
            }
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new DiffException(e.getMessage());
        }
    }
}
