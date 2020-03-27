package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.DiffInterface;
import sg.edu.nus.comp.cs4218.exception.DiffException;
import sg.edu.nus.comp.cs4218.impl.app.args.DiffArguments;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.*;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class DiffApplication implements DiffInterface {
    private static final String STDIN_STRING = "-";
    private static final String ARE_IDENTICAL = "Files %s and %s are identical";
    private static final String DIFFER = "Files %s and %s differ";
    private static final String BINARYDIFFER = "Binary files %s and %s differ";
    private static final String COMMON_SUBDIR = "Common subdirectories: ";
    private static final String ONLY_IN = "Only in %s: ";

    private File[] listFilesAlphabetOrder(String dir_str) {
        File directory = IOUtils.resolveFilePath(dir_str).toFile();
        File[] sortedFileArray = {};

        if (directory.list().length == 0) {
            return sortedFileArray;
        }

        String[] fileList = directory.list();
        Arrays.sort(fileList);
        sortedFileArray = new File[fileList.length];
        for (int i = 0; i < fileList.length; i++) {
            sortedFileArray[i] = IOUtils.resolveFilePath(dir_str + "/" + fileList[i]).toFile();
        }
        return sortedFileArray;
    }

    private Boolean areFilesEqual(File fileA, File fileB) {
        if (fileA.getName() == fileB.getName()) {
            if ((fileA.isDirectory() && fileB.isDirectory()) || (fileA.isFile() && fileB.isFile())) {
                return true;
            }
        }
        return false;
    }

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
    public String diffTwoFiles(String fileNameA, String fileNameB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        String result = "";
        Boolean isBinary = false;
        if (!fileNameA.endsWith(".txt") || !fileNameB.endsWith(".txt")) {
            // binary files, set to isSimple
            isSimple = true;
            isBinary = true;
        }
        File fileA = IOUtils.resolveFilePath(fileNameA).toFile();
        File fileB = IOUtils.resolveFilePath(fileNameB).toFile();
        HashSet<String> setA = new HashSet<String>();
        HashSet<String> setB = new HashSet<String>();
        HashSet<String> copySetA = new HashSet<String>();
        BufferedReader readerA = null;
        BufferedReader readerB = null;
        try {
            readerA = new BufferedReader(new FileReader(fileA));
            readerB = new BufferedReader(new FileReader(fileB));
        } catch (FileNotFoundException e) {
            throw new DiffException(ERR_FILE_NOT_FOUND);
        }

        String line;
        try {
            while ((line = readerA.readLine()) != null) {
                if (!line.equals("") || !isNoBlank) {
                    setA.add(line);
                    copySetA.add(line);
                }
            }
            while ((line = readerB.readLine()) != null) {
                if (!line.equals("") || !isNoBlank) {
                    setB.add(line);
                }
            }
        } catch (IOException e) {
            throw new DiffException(ERR_IO_EXCEPTION);
        }

        for (String s : setA) {
            if (setB.contains(s)) {
                copySetA.remove(s);
                setB.remove(s);
            }
        }
        // files are identical
        if (copySetA.isEmpty() && setB.isEmpty()) {
            if (isShowSame) {
                result = String.format(ARE_IDENTICAL, fileNameA, fileNameB);
            }
            return result + "\n";
        }
        // files are different
        if (isSimple) {
            if (isBinary) {
                result = String.format(BINARYDIFFER, fileNameA, fileNameB);
            } else {
                result = String.format(DIFFER, fileNameA, fileNameB);
            }
            return result + "\n";
        }
        for (String s : copySetA) {
            result = result + "<" + s + "\n";
        }
        for (String s : setB) {
            result = result + ">" + s + "\n";
        }


        try {
            readerA.close();
            readerB.close();
        } catch (IOException e) {
            throw new DiffException(ERR_IO_EXCEPTION);
        }

        return result;
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
    public String diffTwoDir(String folderA, String folderB, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        String result = "";

        File[] folderAContents = listFilesAlphabetOrder(folderA);
        File[] folderBContents = listFilesAlphabetOrder(folderB);

        HashMap<String, String> folderAHashMap = new HashMap<>();
        HashMap<String, String> folderBHashMap = new HashMap<>();
        HashMap<String, String> folderAHashMapCopy = new HashMap<>();
        HashSet<String> commonSubDirectories = new HashSet<>();
        HashSet<String> commonFiles = new HashSet<>();

        for (File file : folderAContents) {
            if (file.isDirectory()) {
                folderAHashMap.put(file.getName(), "Directory");
                folderAHashMapCopy.put(file.getName(), "Directory");
            }
            if (file.isFile()) {
                folderAHashMap.put(file.getName(), "File");
                folderAHashMapCopy.put(file.getName(), "File");
            }
        }

        for (File file : folderBContents) {
            if (file.isDirectory()) {
                folderBHashMap.put(file.getName(), "Directory");
            }
            if (file.isFile()) {
                folderBHashMap.put(file.getName(), "File");
            }
        }

        for (Map.Entry<String, String> entry : folderAHashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (folderBHashMap.get(key) != null && folderBHashMap.get(key) == value) {
                folderBHashMap.remove(key, value);
                folderAHashMapCopy.remove(key, value);
                if (value.equals("Directory")) {
                    commonSubDirectories.add(key);
                }
                if (value.equals("File")) {
                    commonFiles.add(key);
                }
            }
        }

        String commonFilesDiff = "";
        // For common files, compare content.
        for (String key : commonFiles) {
            commonFilesDiff += diffTwoFiles(folderA + "/" + key, folderB + "/" + key, false, false, true);
            commonFilesDiff += "\n";
        }

        String commonSubdirMsg = COMMON_SUBDIR;
        for (String key : commonSubDirectories) {
            commonSubdirMsg = commonSubdirMsg + folderA + "/" + key + " and " + folderB + "/" + key + " ";
        }

        String onlyInFirstMsg = String.format(ONLY_IN, folderA);
        for (String key : folderAHashMapCopy.keySet()) {
            onlyInFirstMsg = onlyInFirstMsg + key + " ";
        }

        String onlyInSecondMsg = String.format(ONLY_IN, folderB);
        for (String key : folderBHashMap.keySet()) {
            onlyInSecondMsg = onlyInSecondMsg + key + " ";
        }

        result = commonFilesDiff + commonSubdirMsg + "\n" + onlyInFirstMsg + "\n" + onlyInSecondMsg + "\n";
        return result;
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
    public String diffFileAndStdin(String fileName, InputStream stdin, Boolean isShowSame, Boolean isNoBlank, Boolean isSimple) throws DiffException {
        String result = "";
        // check file exists / is directory / can read
        File file = IOUtils.resolveFilePath(fileName).toFile();
        if (!file.exists()) {
            throw new DiffException(ERR_FILE_NOT_FOUND);
        }
        if (file.isDirectory()) {
            throw new DiffException(ERR_IS_DIR);
        }
        if (!file.canRead()) {
            throw new DiffException(ERR_NO_PERM);
        }
        List<String> stdinLines;
        try {
            stdinLines = IOUtils.getLinesFromInputStream(stdin);
        } catch (Exception e) {
            throw new DiffException(ERR_IO_EXCEPTION);
        }

        HashSet<String> setFile = new HashSet<String>();
        HashSet<String> setStdin = new HashSet<String>();
        HashSet<String> copySetFile = new HashSet<String>();
        BufferedReader readerFile = null;
        try {
            readerFile = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new DiffException(ERR_FILE_NOT_FOUND);
        }

        String line;
        try {
            while ((line = readerFile.readLine()) != null) {
                if (!line.equals("") || !isNoBlank) {
                    setFile.add(line);
                    copySetFile.add(line);
                }
            }
            for (String s : stdinLines) {
                if (!s.equals("") || !isNoBlank) {
                    setStdin.add(s);
                }
            }
        } catch (IOException e) {
            throw new DiffException(ERR_IO_EXCEPTION);
        }

        for (String s : setFile) {
            if (setStdin.contains(s)) {
                copySetFile.remove(s);
                setStdin.remove(s);
            }
        }
        // files are identical
        if (copySetFile.isEmpty() && setStdin.isEmpty()) {
            if (isShowSame) {
                result = String.format(ARE_IDENTICAL, fileName, stdin);
            }
            return result + "\n";
        }
        // files are different
        if (isSimple) {
            result = String.format(DIFFER, fileName, stdin);
            return result + "\n";
        }
        for (String s : copySetFile) {
            result = result + "<" + s + "\n";
        }
        for (String s : setStdin) {
            result = result + ">" + s + "\n";
        }

        return result;
    }

    /**
     * Runs application with specified input data and specified output stream.
     *
     * @param args
     * @param stdin
     * @param stdout
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws DiffException {
        if (stdout == null) {
            throw new DiffException(ERR_NO_OSTREAM);
        }

        if (stdin == null) {
            throw new DiffException(ERR_NO_ISTREAM);
        }

        DiffArguments diffArguments = new DiffArguments();

        try {
            diffArguments.parse(args);
        } catch (DiffException e) {
            throw new DiffException(ERR_INVALID_ARG);
        }

        List<String> filesToCompare = diffArguments.getFiles();
        String[] filesToCompareArr = new String[filesToCompare.size()];
        filesToCompare.toArray(filesToCompareArr);

        String firstFileName = filesToCompareArr[0];
        String secondFileName = filesToCompareArr[1];

        Boolean isSimple = diffArguments.checkIfSimple();
        Boolean isNoBlank = diffArguments.checkIfBlank();
        Boolean isShowSame = diffArguments.checkIfShowSame();

        // First file is stdin
        if (firstFileName.length() == 1 && firstFileName.compareTo(STDIN_STRING) == 0) {
            try {
                String result = diffFileAndStdin(secondFileName, stdin, isShowSame, isNoBlank, isSimple);
                stdout.write(result.getBytes());
                return;
            } catch (Exception e) {
                throw new DiffException(e.getMessage());
            }
        }

        // Second file is stdin
        if (secondFileName.length() == 1 && secondFileName.compareTo(STDIN_STRING) == 0) {
            try {
                String result = diffFileAndStdin(firstFileName, stdin, isShowSame, isNoBlank, isSimple);
                stdout.write(result.getBytes());
                return;
            } catch (Exception e) {
                throw new DiffException(e.getMessage());
            }
        }

        // Both are stdin
        if (firstFileName.length() == 1 && firstFileName.compareTo(STDIN_STRING) == 0 &&
                secondFileName.length() == 1 && secondFileName.compareTo(STDIN_STRING) == 0) {
            throw new DiffException(ERR_TWO_STDIN);
        }

        File firstFile = IOUtils.resolveFilePath(firstFileName).toFile();
        File secondFile = IOUtils.resolveFilePath(secondFileName).toFile();

        if (!firstFile.exists()) {
            throw new DiffException(ERR_FILE_NOT_FOUND + ": " + firstFileName);
        }

        if (!secondFile.exists()) {
            throw new DiffException(ERR_FILE_NOT_FOUND + ": " + secondFileName);
        }

        if (firstFile.isDirectory() && secondFile.isDirectory()) {
            try {
                String result = diffTwoDir(firstFileName, secondFileName, isShowSame, isNoBlank, isSimple);
                stdout.write(result.getBytes());

                return;
            } catch (IOException e) {
                throw new DiffException(ERR_IO_EXCEPTION);
            } catch (Exception e) {
                throw new DiffException(e.getMessage());
            }
        } else if (firstFile.isFile() && secondFile.isFile()) {
            try {
                String result = diffTwoFiles(firstFileName, secondFileName, isShowSame, isNoBlank, isSimple);
                stdout.write(result.getBytes());
                return;
            } catch (Exception e) {
                throw new DiffException(e.getMessage());
            }
        } else {
            // Cannot compare file and directory
            throw new DiffException(ERR_FILE_AND_DIR);
        }
    }
}
