package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.PathUtils.convertToAbsolutePath;

public final class FileUtils {
    private FileUtils() {
    }

    public static File createFile(String path) throws Exception {
        File file = new File(convertToAbsolutePath(path));
        if (file.exists()) {
            throw new Exception("Path already exists");
        }

        try {
            file.createNewFile();
            assertTrue(file.exists());
            assertFalse(file.isDirectory());
        } catch (Exception e) {
            throw e;
        }
        return file;
    }

    public static File createFolder(String path) throws Exception {
        File folder = new File(convertToAbsolutePath(path));
        if (folder.exists()) {
            throw new Exception("Path already exists");
        }

        try {
            folder.mkdir();
            assertTrue(folder.exists());
            assertTrue(folder.isDirectory());
        } catch (Exception e) {
            throw e;
        }
        return folder;
    }

    public static void deleteFile(String path) throws Exception {
        try {
            deleteFile(Paths.get(convertToAbsolutePath(path)));
        } catch (Exception e) {
            throw e;
        }
    }

    public static void deleteFile(Path path) throws Exception {
        if (!Files.exists(path)) {
            return;
        }

        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

            assertFalse(Files.exists(path));
        } catch (Exception e) {
            throw e;
        }
    }
}
