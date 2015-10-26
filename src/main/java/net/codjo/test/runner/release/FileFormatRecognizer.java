/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.test.runner.release;
import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.util.Arrays;

/**
 * Classe utilitaire permettant de reconnaître un fichier au format release-test.
 */
class FileFormatRecognizer {
    private Logger logger = Logger.getInstance("idea.jalopy.JalopyPlugin");

    private static final FileFilter FILE_FILTER = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".xml");
        }
    };

    FileFormatRecognizer() {
    }

    public boolean isReleaseTestFile(String filePath) {
        return isReleaseTestFile(new File(filePath), createReadBuffer());
    }

    public boolean isReleaseTestFileFormat(String filePath) {
        return isReleaseTestFileFormat(new File(filePath), createReadBuffer());
    }

    // used only by tests
    boolean isReleaseTestFileFormat(Reader reader) throws IOException {
        return isReleaseTestFileFormat(reader, createReadBuffer());
    }

    private boolean isReleaseTestFileFormat(Reader reader, char[] buffer)
          throws IOException {
        // Algorithme extrêmement complexe est évolué permettant de déterminer le format :)
        reader.read(buffer);
        String content = new String(buffer);
        return content.contains("<release-test");
    }

    private boolean isReleaseTestFile(File file, char[] buffer) {
        if (file.isDirectory()) {
            return isReleaseTestDirectory(file, buffer);
        }

        return isReleaseTestFileFormat(file, buffer);
    }

    private boolean isReleaseTestDirectory(File file, char[] buffer) {
        File[] files = file.listFiles(FILE_FILTER);
        for (File currentFile : files) {
            if (isReleaseTestFile(currentFile, buffer)) {
                return true;
            }
        }

        return false;
    }

    private boolean isReleaseTestFileFormat(File file, char[] buffer) {
        try {
            if (!file.exists() || !file.isFile()) {
                return false;
            }
            FileReader reader = new FileReader(file);
            try {
                return isReleaseTestFileFormat(reader, buffer);
            }
            finally {
                Arrays.fill(buffer, (char) 0);
                reader.close();
            }
        }
        catch (IOException e) {
            logger.error("Impossible de déterminer si le fichier est au format ReleaseTest",
                         e);
            return false;
        }
    }

    private char[] createReadBuffer() {
        return new char[10000];
    }
}
