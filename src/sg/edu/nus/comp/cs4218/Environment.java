package sg.edu.nus.comp.cs4218;

import java.nio.file.Files;

import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public final class Environment {//NOPMD

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");

    /**
     * Sets the current directory of Shell and returns it.
     * @param path has to be a valid absolute path
     */
    public static void setCurrentDirectory(String path) {
        if (Files.isDirectory(IOUtils.resolveFilePath(path))) {
            currentDirectory = IOUtils.resolveFilePath(path).toString();
        }
    }

    /**
     *
     * @return currentDirectory
     */
    public static String getCurrentDirectory() {
        return currentDirectory;
    }

    private Environment() {
    }

}
