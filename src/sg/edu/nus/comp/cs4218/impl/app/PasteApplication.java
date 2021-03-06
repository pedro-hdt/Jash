package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplication implements PasteInterface {
    
    @Override
    public String mergeStdin(InputStream stdin) throws PasteException {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));//NOPMD
        StringBuilder sb = new StringBuilder();//NOPMD
        
        String line;
        try {
            line = reader.readLine();
            while (line != null && !line.isEmpty()) {
                sb.append(line);
                sb.append(STRING_NEWLINE);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
        }
        
        return sb.toString().trim();
        
    }
    
    @Override
    public String mergeFile(String... fileName) throws PasteException {
        
        return mergeFileAndStdin(System.in, fileName);
        
    }
    
    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws PasteException {
        
        List<BufferedReader> fileReaders = buildReadersList(stdin, fileName);
        StringBuilder sb = new StringBuilder(); //NOPMD
        
        try {
            boolean notDone;
            do {
                notDone = false;
                for (BufferedReader reader : fileReaders) { //NOPMD
                    String line = reader.readLine();
                    if (line != null) {
                        notDone = true;
                        sb.append(line);
                    }
                    sb.append(CHAR_TAB);
                }
                sb.deleteCharAt(sb.length() - 1);
                if (notDone) {
                    sb.append(STRING_NEWLINE);
                }
            } while (notDone);
            if (sb.length() - fileReaders.size() >= 0) {
                sb.delete(sb.length() - fileReaders.size(), sb.length()); // remove extra tab chars
            }
            
            for (BufferedReader reader : fileReaders) {//NOPMD
                reader.close();
            }
        } catch (IOException e) {
            throw (PasteException) new IOException(ERR_IO_EXCEPTION).initCause(e);
        }
        
        return sb.toString();
        
    }
    
    
    private List<BufferedReader> buildReadersList(InputStream stdin, String... files) throws PasteException {
        
        List<BufferedReader> readers = new ArrayList<>();
        BufferedReader stdinBufReader = new BufferedReader(new InputStreamReader(stdin)); //NOPMD
        
        for (String f : files) {
            try {
                if ("-".equals(f)) {
                    readers.add(stdinBufReader);
                } else {
                    File file = IOUtils.resolveFilePath(f).toFile();
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            throw new PasteException(f + ": " + ERR_IS_DIR);
                        }
                    } else {
                        throw new PasteException(f + ": " + ERR_FILE_NOT_FOUND);
                    }
                    readers.add(new BufferedReader(new FileReader(file)));
                }
            } catch (IOException e) {
                throw (PasteException) new IOException(ERR_IO_EXCEPTION).initCause(e);
            }
        }
        
        return readers;
    }
    
    
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        
        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }
        
        if (stdin == null) {
            throw new PasteException(ERR_NO_ISTREAM);
        }
        
        if (stdout == null) {
            throw new PasteException(ERR_NO_OSTREAM);
        }
        
        if (args.length == 0) {
            throw new PasteException(ERR_NO_ARGS);
        }
        
        boolean hasStdin = Arrays.stream(args).anyMatch((x) -> "-".equals(x));
        boolean hasFiles = !Arrays.stream(args).allMatch((x) -> "-".equals(x));
        
        String result;
        if (hasStdin) {
            if (hasFiles) {
                result = mergeFileAndStdin(stdin, args);
            } else {
                String[] lines = mergeStdin(stdin).split(STRING_NEWLINE);
                StringBuilder sb = new StringBuilder();//NOPMD
                int columns = args.length;
                for (int i = 0; i < lines.length; i++) {
                    sb.append(lines[i]);
                    sb.append(CHAR_TAB);
                    if (i % columns == columns - 1) {
                        sb.deleteCharAt(sb.length() - 1);
                        sb.append(STRING_NEWLINE);
                    }
                }
                result = sb.toString().trim();
            }
        } else {
            result = mergeFile(args);
        }
        
        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw (PasteException) new PasteException(ERR_WRITE_STREAM).initCause(e);
        }
    }
}
