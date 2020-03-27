package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;

public final class PathUtils {
    private PathUtils() {
    }

    /**
     * Converts folderName to absolute path, if initially was relative path
     *
     * @param folderName supplied by user
     * @return a String of the absolute path of the folderName
     */
    public static String convertToAbsolutePath(String folderName) {
        String home = System.getProperty("user.home").trim();
        String currentDir = Environment.currentDirectory.trim();
        String convertedPath = convertPathToSystemPath(folderName);

        String newPath;
        if (convertedPath.length() >= home.length() && convertedPath.substring(0, home.length()).trim().equals(home)) {
            newPath = convertedPath;
        } else {
            newPath = currentDir + CHAR_FILE_SEP + convertedPath;
        }
        return newPath;
    }

    /**
     * Converts path provided by user into path recognised by the system
     *
     * @param path supplied by user
     * @return a String of the converted path
     */
    public static String convertPathToSystemPath(String path) {
        String convertedPath = path;
        String pathIdentifier = "\\" + CHAR_FILE_SEP;
        convertedPath = convertedPath.replaceAll("(\\\\)+", pathIdentifier);
        convertedPath = convertedPath.replaceAll("/+", pathIdentifier);

        if (convertedPath.length() != 0 && convertedPath.charAt(convertedPath.length() - 1) == CHAR_FILE_SEP) {
            convertedPath = convertedPath.substring(0, convertedPath.length() - 1);
        }

        return convertedPath;
    }
}
