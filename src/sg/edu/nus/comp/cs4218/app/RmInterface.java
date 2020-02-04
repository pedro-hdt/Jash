package sg.edu.nus.comp.cs4218.app;

import sg.edu.nus.comp.cs4218.Application;

public interface RmInterface extends Application {
    /**
     * Remove the file. (It does not remove folder by default)
     * <p>
     * Assumption: 'rm -d' will delete directories iff they are empty
     * 'rm -r' will recursively delete stuff so no matter if it is empty
     * Therefore, 'rm -r -d' == 'rm -r'
     *
     * @param isEmptyFolder Boolean option to delete a folder only if it is empty
     * @param isRecursive   Boolean option to recursively delete the folder contents (traversing
     *                      through all folders inside the specified folder)
     * @param fileName      Array of String of file names
     * @throws Exception
     */

    void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws Exception;
}
