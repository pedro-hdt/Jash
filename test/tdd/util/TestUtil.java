package tdd.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("PMD")
public class TestUtil {

    public static Path resolveFilePath(String fileName) {
        Path currentDirectory = Paths.get(Environment.currentDirectory);
        return currentDirectory.resolve(fileName);
    }
}
