package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.InputStream;
import java.io.OutputStream;

public class MvApplication implements MvInterface { // TODO implement me
    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolder, String... fileName) throws Exception {
        return null;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        // TODO implement me
    }
}
