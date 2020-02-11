package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplication implements PasteInterface {

    @Override
    public String mergeStdin(InputStream stdin) throws PasteException {

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));

        try {
            String line = reader.readLine();
            while (!line.isEmpty()) {
                sb.append(line);
                sb.append(CHAR_TAB);
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
        }

        return sb.toString();

    }

    @Override
    public String mergeFile(String... fileName) throws PasteException {

        // TODO this *could* (and maybe should) use a buffered reader, but for normal applications,
        // it's probably fine to just load all lines...
        // if we are supposed to build the string an only then return it then it means
        // we need space for it anyway, and the performance is probably irrelevant for this module

        StringBuilder sb = new StringBuilder();

        // obtain list of files, where a file is a list of its lines as strings
        List<List<String>> files = new ArrayList<>(fileName.length);
        for (String f : fileName) {
            try {
                files.add(Files.readAllLines(IOUtils.resolveFilePath(f)));
            } catch (IOException e) {
                throw new PasteException(ERR_IO_EXCEPTION);
            }
        }

        // get number of lines in longest file
        int largestFileLines = Collections.max(files.stream().map(List::size).collect(Collectors.toList()));
        int currLine = 0;
        while (currLine < largestFileLines) {
            for (List<String> file : files) {
                if (currLine < file.size()) {
                    sb.append(file.get(currLine));
                }
                sb.append(CHAR_TAB);
            }
            sb.append(STRING_NEWLINE);
            currLine++;
        }

        return sb.toString();

    }

    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws PasteException {

        StringBuilder sb = new StringBuilder();

        // obtain list of files, where a file is a list of its lines as strings
        List<List<String>> files = new ArrayList<>(fileName.length);
        for (String f : fileName) {
            if (!f.equals("-")) {
                try {
                    files.add(Files.readAllLines(IOUtils.resolveFilePath(f)));
                } catch (IOException e) {
                    throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
                }
            } else {
                files.add(new ArrayList<>());
            }
        }

        // TODO remove duplicated code

        int largestFileLines = Collections.max(files.stream().map(List::size).collect(Collectors.toList()));
        int currLine = 0;
        while (currLine < largestFileLines) {

            for (int i = 0; i < fileName.length; i++) {

                if (fileName[i].equals("-")) {
                    sb.append(mergeStdin(stdin));
                } else {
                    List<String> file = files.get(i);
                    if (currLine < file.size()) {
                        sb.append(file.get(currLine));
                    }
                }

                sb.append(CHAR_TAB);

            }
            sb.append(STRING_NEWLINE);
            currLine++;
        }

        return sb.toString();
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {

        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        if (args.length == 0) {
            throw new PasteException(ERR_NO_FILE_ARGS);
        }

        boolean hasStdin = Arrays.stream(args).anyMatch((x) -> x.equals("-"));
        boolean hasFiles = !Arrays.stream(args).allMatch((x) -> x.equals("-"));

        String result;
        if (!hasStdin) {
            result = mergeFile(args);
        } else if (!hasFiles) {

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));

            try {
                String line = reader.readLine();
                while (!line.isEmpty()) {
                    for (int i = 0; i < args.length && !line.isEmpty(); i++) {
                        sb.append(line);
                        sb.append(CHAR_TAB);
                        line = reader.readLine();
                    }
                    sb.append(STRING_NEWLINE);
                }
            } catch (IOException e) {
                throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
            }

            result = sb.toString();

        } else {
            result = mergeFileAndStdin(stdin, args);
        }

        try {
            stdout.write(result.getBytes());
            // stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
        }

    }
}
