package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;

public class LsApplication implements LsInterface {
    
    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;
    
    @Override
    public String listFolderContent(Boolean isFoldersOnly, Boolean isRecursive,
                                    String... folderName) throws LsException {
        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isFoldersOnly);
        }
        
        List<Path> paths;
        if (folderName.length == 0 && isRecursive) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderName);
        }
        
        return buildResult(paths, isFoldersOnly, isRecursive);
    }
    
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
      throws LsException {
        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }
        
        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }
        
        LsArgsParser parser = new LsArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw (LsException) new LsException(e.getMessage()).initCause(e);
        }
        
        Boolean foldersOnly = parser.isFoldersOnly();
        Boolean recursive = parser.isRecursive();
        String[] directories = parser.getDirectories()
          .toArray(new String[0]);
        String result = listFolderContent(foldersOnly, recursive, directories);
        
        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw (LsException) new LsException(ERR_WRITE_STREAM).initCause(e);
        }
    }
    
    /**
     * Lists only the current directory's content and RETURNS. This does not account for recursive
     * mode in cwd.
     *
     * @param isFoldersOnly
     * @return
     */
    private String listCwdContent(Boolean isFoldersOnly) throws LsException {
        String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd), isFoldersOnly));
        } catch (InvalidDirectoryException e) {
            throw (LsException) new LsException("Unexpected error occurred!").initCause(e);
        }
    }
    
    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths         - list of java.nio.Path objects to list
     * @param isFoldersOnly - only list the folder contents
     * @param isRecursive   - recursive mode, repeatedly ls the child directories
     * @return String to be written to output stream.
     */
    private String buildResult(List<Path> paths, Boolean isFoldersOnly, Boolean isRecursive) {
        StringBuilder result = new StringBuilder();
        for (Path path : paths) {
            try {
                List<Path> contents = getContents(path, isFoldersOnly);
                String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
    
                if (Files.isDirectory(path)) {
                    String formatted = formatContents(contents);
                    result.append(':').append(System.lineSeparator());
                    result.append(formatted);
        
                    if (!formatted.isEmpty()) {
                        // Empty directories should not have an additional new line
                        result.append(StringUtils.STRING_NEWLINE);
                    }
                }
    
                result.append(StringUtils.STRING_NEWLINE);
    
                // RECURSE!
                if (isRecursive && Files.isDirectory(path)) {
                    result.append(buildResult(contents.stream()
                      .filter(p -> Files.isDirectory(p))
                      .collect(Collectors.toList()), isFoldersOnly, isRecursive));
                }
            } catch (InvalidDirectoryException e) {
                // NOTE: This is pretty hackish IMO - we should find a way to change this
                // If the user is in recursive mode, and if we resolve a file that isn't a directory
                // we should not spew the error message.
                //
                // However the user might have written a command like `ls invalid1 valid1 -R`, what
                // do we do then?
                // NOTE: Effort has been put into solving the above issue and now works well however certain weird bugs
                // may still remain due to earlier design decisions
                result.append(e.getMessage());
                result.append(System.lineSeparator());
            }
        }
    
        return result.toString().trim();
    }
    
    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents - list of items in a directory
     * @return
     */
    private String formatContents(List<Path> contents) {
        List<String> fileNames = new ArrayList<>();
        for (Path path : contents) {
            fileNames.add(path.getFileName().toString());
        }
    
        StringBuilder result = new StringBuilder();
        for (String fileName : fileNames) {
            result.append(fileName);
            result.append(System.lineSeparator());
        }
    
        return result.toString().trim();
    }
    
    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory
     * @return List of files + directories in the passed directory.
     */
    private List<Path> getContents(Path directory, Boolean isFoldersOnly)
      throws InvalidDirectoryException {
        if (!Files.exists(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }
        
        if (!isFoldersOnly && !Files.isDirectory(directory)) {
            return new ArrayList<>(Collections.singleton(directory));
        }
        
        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        
        for (File f : Objects.requireNonNull(pwd.listFiles())) {
            if (isFoldersOnly && !f.isDirectory()) {
                continue;
            }
            
            if (!f.isHidden()) {
                result.add(f.toPath());
            }
        }
        
        Collections.sort(result);
        
        return result;
    }
    
    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories
     * @return List of java.nio.Path objects
     */
    private List<Path> resolvePaths(String... directories) throws LsException {
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < directories.length; i++) {
            paths.add(resolvePath(directories[i]));
        }
    
        return paths;
    }
    
    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory
     * @return
     */
    private Path resolvePath(String directory) throws LsException {
        if (directory.isEmpty()) {
            throw new LsException("No such file or directory");
        }
        //Is this a bug since it checks only for Unix based and not Windows etc?
        if (Paths.get(directory).isAbsolute()) {
            // This is an absolute path
            return Paths.get(directory).normalize();
        }
    
        return Paths.get(Environment.currentDirectory, directory).normalize();
    }
    
    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path
     * @return
     */
    private Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }
    
    private static class InvalidDirectoryException extends Exception {
        InvalidDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory));
        }
        
    }
}
