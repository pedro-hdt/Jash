package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplication implements PasteInterface {

    @Override
    public String mergeStdin(InputStream stdin) throws PasteException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
        StringBuilder sb = new StringBuilder();

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

        List<BufferedReader> readers = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (String f : fileName) {
            try {
                readers.add(new BufferedReader(new FileReader(IOUtils.resolveFilePath(f).toString())));
            } catch (IOException e) {
                throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
            }
        }

        try {

            boolean done = false;

            while (!done) {
                done = true;
                for (BufferedReader reader : readers) {
                    String line = reader.readLine();
                    if (line != null) {
                        done = false;
                        sb.append(line);
                    }
                    sb.append(CHAR_TAB);
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(STRING_NEWLINE);
            }

            for (BufferedReader reader : readers) {
                reader.close();
            }

        } catch (IOException e) {
            throw (PasteException) new IOException(ERR_IO_EXCEPTION).initCause(e);
        }

        return sb.toString().trim();

    }

    @Override
    public String mergeFileAndStdin(InputStream stdin, String... fileName) throws PasteException {

        List<BufferedReader> fileReaders = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (String f : fileName) {
            try {
                if ("-".equals(f)) {
                    fileReaders.add(null);
                } else {
                    fileReaders.add(new BufferedReader(new FileReader(IOUtils.resolveFilePath(f).toString())));
                }
            } catch (IOException e) {
                throw (PasteException) new IOException(ERR_IO_EXCEPTION).initCause(e);
            }
        }

        try {
            ListIterator<String> stdinLines = Arrays.asList(mergeStdin(stdin).split(STRING_NEWLINE)).listIterator();
            int columns = fileName.length;
            int i = 0;
            String line = "";
            while (stdinLines.hasNext() || line != null) {

                if (fileName[i % columns].equals("-")) {
                    if (stdinLines.hasNext()) {
                        sb.append(stdinLines.next());
                    }
                } else if ((line = fileReaders.get(i % columns).readLine()) != null) {
                    sb.append(line);
                }
                sb.append(CHAR_TAB);

                if (i % columns == columns - 1) {
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(STRING_NEWLINE);
                }

                i++;
            }
        } catch (IOException e) {
            throw (PasteException) new IOException(ERR_IO_EXCEPTION).initCause(e);
        }

        return sb.toString().trim();

    }


    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {

        if (args == null) {
            throw new PasteException(ERR_NULL_ARGS);
        }

        if (args.length == 0) {
            throw new PasteException(ERR_NO_ARGS);
        }

        if (stdout == null) {
            throw new PasteException(ERR_NO_OSTREAM);
        }

        boolean hasStdin = Arrays.stream(args).anyMatch((x) -> "-".equals(x));
        boolean hasFiles = !Arrays.stream(args).allMatch((x) -> "-".equals(x));

        String result;
        if (hasStdin) {
            if (hasFiles) {
                result = mergeFileAndStdin(stdin, args);
            } else {
                String[] lines = mergeStdin(stdin).split("\n");
                StringBuilder sb = new StringBuilder();
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
            throw (PasteException) new PasteException(ERR_IO_EXCEPTION).initCause(e);
        }
    }
}
