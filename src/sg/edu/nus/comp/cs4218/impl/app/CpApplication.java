package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD.PreserveStackTrace")
public class CpApplication implements CpInterface {
    
    @Override
    public String cpSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path source = IOUtils.resolveFilePath(srcFile);
        if (!Files.exists(source)) {
            throw new CpException("'" + srcFile + "': " + ERR_FILE_NOT_FOUND);
        }
        IOUtils.resolveFilePath(destFile).toFile().getParentFile().mkdirs(); // create all needed parent dirs
        Files.copy(source, source.resolveSibling(destFile), StandardCopyOption.REPLACE_EXISTING);
        
        return null;
    }
    
    @Override
    public String cpFilesToFolder(String destFolder, String... fileName) throws Exception { //NOPMD
        
        List<String> invalidFiles = new ArrayList<>();
        boolean hasOtherErrOccurred = false; //NOPMD
        
        for (String srcPath : fileName) {
            
            Path src = IOUtils.resolveFilePath(srcPath);
            
            if (!Files.exists(src)
              || src.getParent().equals(IOUtils.resolveFilePath(destFolder))
              || src.equals(IOUtils.resolveFilePath(destFolder))) {
                invalidFiles.add(srcPath); // signal this file does not exist to report it later then skip it
                continue;
            }
            
            // Can avoid this with assumption that target operand is always a directory
            if (Files.exists(IOUtils.resolveFilePath(destFolder))) {
                if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length == 1) {
                    FileOutputStream outputStream = new FileOutputStream(IOUtils.resolveFilePath(destFolder).toFile());//NOPMD
                    byte[] strToBytes = Files.readAllBytes(IOUtils.resolveFilePath(srcPath));
                    outputStream.write(strToBytes);
                    outputStream.close();

                    return null;
                } else if (!Files.isDirectory(IOUtils.resolveFilePath(destFolder)) && fileName.length > 1) {
                    throw new Exception("'" + destFolder + "' is not a directory.");
                }
            }
            
            try {
                Path dest = Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                        IOUtils.resolveFilePath(srcPath).getFileName().toString());
                dest.toFile().getParentFile().mkdirs();
                Files.copy(IOUtils.resolveFilePath(srcPath),
                  Paths.get(IOUtils.resolveFilePath(destFolder).toString(),
                    IOUtils.resolveFilePath(srcPath).getFileName().toString()),
                  StandardCopyOption.REPLACE_EXISTING);
            } catch (DirectoryNotEmptyException dnee) {
                throw new Exception(ERR_CANNOT_OVERWRITE + " non-empty directory: " + destFolder);
            } catch (FileSystemException fse) {
                hasOtherErrOccurred = true;
            }
            
        }
        
        if (!invalidFiles.isEmpty()) {
            StringBuilder sb = new StringBuilder();//NOPMD
            for (String f : invalidFiles) {
                sb.append(f);
                sb.append(" skipped: ");
                if (Files.exists(IOUtils.resolveFilePath(f))) {
                    sb.append(String.format("'%s' and '%s' are the same file", f, f));
                } else {
                    sb.append(ERR_FILE_NOT_FOUND);
                }
                sb.append(STRING_NEWLINE);
            }
            throw new Exception(STRING_NEWLINE + sb.toString().trim());
        }
        
        if (hasOtherErrOccurred) {
            throw new Exception("file system error while copying file");
        }
        
        return null;
    }
    
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {
        
        if (args == null) {
            throw new CpException(ERR_NULL_ARGS);
        }
        
        if (stdout == null) {
            throw new CpException(ERR_NO_OSTREAM);
        }
        
        // Assumption: Will assume multiple args passed when regex is used with only first arg passed
        if (args.length < 2) {
            throw new CpException(ERR_NO_ARGS);
        }
        
        List<String> sourceOperands = Arrays.asList(args).subList(0, args.length - 1);
        String targetOperand = args[args.length - 1];
        
        try {
            if (Files.exists(IOUtils.resolveFilePath(targetOperand))
                    || Files.isDirectory(IOUtils.resolveFilePath(targetOperand))
                    || sourceOperands.size() > 1) {
                cpFilesToFolder(targetOperand, sourceOperands.toArray(new String[0]));
            } else {
                cpSrcFileToDestFile(sourceOperands.get(0), targetOperand);
            }
        } catch (Exception e) {
            throw (CpException) new CpException(e.getMessage()).initCause(e);
        }
    }
}