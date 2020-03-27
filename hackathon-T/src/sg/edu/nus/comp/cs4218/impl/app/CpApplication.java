package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CpApplication implements CpInterface {
    @Override
    public String cpSrcFileToDestFile(String srcFile, String destFile) throws CpException {
        File src = IOUtils.resolveFilePath(srcFile).toFile();
        File dest = IOUtils.resolveFilePath(destFile).toFile();
        byte[] srcContents;
        if (!src.exists()) {
            throw new CpException(ERR_FILE_NOT_FOUND);
        }
        if (src.isDirectory()) {
            throw new CpException(ERR_IS_DIR);
        }
        if (!src.canRead()) {
            throw new CpException(ERR_NO_PERM);
        }

        if (!dest.canWrite()) {
            throw new CpException(ERR_NO_PERM);
        }

        try {
            srcContents = Files.readAllBytes(Paths.get(srcFile));
            Files.write(Paths.get(destFile), srcContents);
        } catch (IOException e) {
            throw new CpException(ERR_IO_EXCEPTION);
        }

        return "";
    }

    @Override
    public String cpFilesToFolder(String destFolder, String... fileName) throws CpException {

        for (String name : fileName) {
            File src = IOUtils.resolveFilePath(name).toFile();
            if (!src.exists()) {
                throw new CpException(ERR_FILE_NOT_FOUND);
            }
            if (src.isDirectory()) {
                throw new CpException(ERR_IS_DIR);
            }
            if (!src.canRead()) {
                throw new CpException(ERR_NO_PERM);
            }
            String[] fileNameChunks = name.split(File.separator);
            String exactName = fileNameChunks[fileNameChunks.length - 1];
            String dest = destFolder + File.separator + exactName;
            File destFile = IOUtils.resolveFilePath(dest).toFile();
            if (!destFile.exists()) {
                try {
                    destFile.createNewFile();
                } catch (IOException e) {
                    throw new CpException(ERR_IO_EXCEPTION);
                }
            }

            try {
                name = IOUtils.resolveFilePath(name).toString();
                byte[] srcContents = Files.readAllBytes(Paths.get(name));
                Files.write(Paths.get(dest), srcContents);
            } catch (IOException e) {
                throw new CpException(ERR_IO_EXCEPTION);
            }

        }

        return "";
    }

    /**
     * Runs the cp application with the specified arguments.
     * Assumption: The application must take in at least two args.
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CpException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (args == null) {
            throw new CpException(ERR_NULL_ARGS);
        }
        if (args.length < 2) {
            throw new CpException(ERR_MISSING_ARG);
        }
        String destination = args[args.length - 1];
        File dest = IOUtils.resolveFilePath(destination).toFile();
        if (!dest.exists()) {
            // if it is a file specified in a non-existant directory, error. else, create it as a file.
            if (dest.getParentFile().exists()) {
                try {
                    dest.createNewFile();
                } catch (IOException e) {
                    throw new CpException(ERR_IO_EXCEPTION);
                }
            } else {
                throw new CpException(ERR_FILE_NOT_FOUND);
            }
        }
        if (dest.isFile()) {
            if (args.length > 2) {
                throw new CpException(ERR_TOO_MANY_ARGS);
            }
            File source = IOUtils.resolveFilePath(args[0]).toFile();
            cpSrcFileToDestFile(source.getPath(), dest.getPath());
        } else {
            String[] srcFiles = Arrays.copyOfRange(args, 0, args.length - 1);
            cpFilesToFolder(dest.getPath(), srcFiles);
        }
    }
}
