package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.args.WcArguments;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class WcApplication implements WcInterface {

    private static final String NUMBER_FORMAT = " %7d";
    private static final int LINES_INDEX = 0;
    private static final int WORDS_INDEX = 1;
    private static final int BYTES_INDEX = 2;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws WcException {
        // Format: wc [-clw] [FILES]
        if (stdout == null) {
            throw new WcException(ERR_NO_OSTREAM);
        }
        if (args == null) {
            throw new WcException(ERR_NULL_ARGS);
        }

        WcArguments wcArgs = new WcArguments();
        wcArgs.parse(args);
        String result;

        if (wcArgs.getFiles().isEmpty()) {
            result = countFromStdin(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), stdin);
        } else {
            result = countFromFiles(wcArgs.isBytes(), wcArgs.isLines(), wcArgs.isWords(), wcArgs.getFiles().toArray(new String[0]));
        }

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (IOException e) {
            throw new WcException(ERR_WRITE_STREAM);//NOPMD
        }
    }

    /**
     * Returns string containing the number of lines, words, and bytes in input files
     *
     * @param isBytes  Boolean option to count the number of Bytes
     * @param isLines  Boolean option to count the number of lines
     * @param isWords  Boolean option to count the number of words
     * @param fileName Array of String of file names
     * @throws Exception
     */
    @Override
    public String countFromFiles(Boolean isBytes, Boolean isLines, Boolean isWords, //NOPMD
                                 String... fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_NO_FILE_ARGS);
        }
        List<String> result = new ArrayList<>();
        long totalBytes = 0, totalLines = 0, totalWords = 0;
        for (String file : fileName) {
            File node = IOUtils.resolveFilePath(file).toFile();
            if (!node.exists()) {
                result.add("wc: " + ERR_FILE_NOT_FOUND);
                continue;
            }
            if (node.isDirectory()) {
                result.add("wc: " + ERR_IS_DIR);
                continue;
            }
            if (!node.canRead()) {
                result.add("wc: " + ERR_NO_PERM);
                continue;
            }

            try {
                InputStream input = IOUtils.openInputStream(file);
                long[] count = getCountReport(input); // lines words bytes
                IOUtils.closeInputStream(input);
                // Update total count
                totalLines += count[0];
                totalWords += count[1];
                totalBytes += count[2];

                // Format all output: " %7d %7d %7d %s"
                // Output in the following order: lines words bytes filename
                StringBuilder sb = new StringBuilder(); //NOPMD
                if (isLines) {
                    sb.append(String.format(NUMBER_FORMAT, count[0]));
                }
                if (isWords) {
                    sb.append(String.format(NUMBER_FORMAT, count[1]));
                }
                if (isBytes) {
                    sb.append(String.format(NUMBER_FORMAT, count[2]));
                }
                sb.append(String.format(" %s", file));
                result.add(sb.toString());
            } catch (ShellException e) {
                throw new WcException(ERR_IO_EXCEPTION);
            }
        }

        // Print cumulative counts for all the files
        if (fileName.length > 1) {
            StringBuilder sb = new StringBuilder(); //NOPMD
            if (isLines) {
                sb.append(String.format(NUMBER_FORMAT, totalLines));
            }
            if (isWords) {
                sb.append(String.format(NUMBER_FORMAT, totalWords));
            }
            if (isBytes) {
                sb.append(String.format(NUMBER_FORMAT, totalBytes));
            }
            sb.append(" total");
            result.add(sb.toString());
        }
        return String.join(STRING_NEWLINE, result);
    }

    /**
     * Returns string containing the number of lines, words, and bytes in standard input
     *
     * @param isBytes Boolean option to count the number of Bytes
     * @param isLines Boolean option to count the number of lines
     * @param isWords Boolean option to count the number of words
     * @param stdin   InputStream containing arguments from Stdin
     * @throws Exception
     */
    @Override
    public String countFromStdin(Boolean isBytes, Boolean isLines, Boolean isWords,
                                 InputStream stdin) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }
        long[] count = getCountReport(stdin); // lines words bytes;

        StringBuilder sb = new StringBuilder(); //NOPMD
        if (isLines) {
            sb.append(String.format(NUMBER_FORMAT, count[0]));
        }
        if (isWords) {
            sb.append(String.format(NUMBER_FORMAT, count[1]));
        }
        if (isBytes) {
            sb.append(String.format(NUMBER_FORMAT, count[2]));
        }

        return sb.toString();
    }

    /**
     * Returns array containing the number of lines, words, and bytes based on data in InputStream.
     *
     * @param input An InputStream
     * @throws IOException
     */
    public long[] getCountReport(InputStream input) throws WcException {
        if (input == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }
        long[] result = new long[3]; // lines, words, bytes

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int inRead = 0;
        boolean inWord = false;
        try {
            while ((inRead = input.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < inRead; ++i) {
                    if (Character.isWhitespace(data[i])) {
                        // Use <newline> character here. (Ref: UNIX)
                        if (data[i] == '\n') {
                            ++result[LINES_INDEX];
                        }
                        if (inWord) {
                            ++result[WORDS_INDEX];
                        }

                        inWord = false;
                    } else {
                        inWord = true;
                    }
                }
                result[BYTES_INDEX] += inRead;
                buffer.write(data, 0, inRead);
            }
            buffer.flush();
            if (inWord) {
                ++result[WORDS_INDEX]; // To handle last word
            }
        } catch (IOException e) {
            throw new WcException(ERR_IO_EXCEPTION);
        }


        return result;
    }
}