/*
 * codjo.net
 *
 * Common Apache License 2.0
 */
package net.codjo.test.runner.release;

import com.intellij.openapi.util.io.FileUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Classe de test de {@link FileFormatRecognizer}.
 */
public class FileFormatRecognizerTest {
    private FileFormatRecognizer recognizer;

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();


    @Test
    public void test_isReleaseTestFileFormat_ok()
          throws Exception {
        assertTrue("Un contenu ReleaseTest",
                   recognizer.isReleaseTestFileFormat(
                         new StringReader("...<release-test name='MyTest' ...")));

        assertTrue("Un fichier au format ReleaseTest",
                   recognizer.isReleaseTestFileFormat(toPath("ReleaseTestFile.xml")));
    }

    @Test
    public void test_isReleaseTestFileFormat_nok()
          throws Exception {
        assertFalse("Un fichier introuvable n'est pas un ReleaseTest",
                    recognizer.isReleaseTestFileFormat("c:/unknown.file"));

        assertFalse("Un repertoire n'est pas un ReleaseTest",
                    recognizer.isReleaseTestFileFormat(getDirectory()));

        assertFalse("Un contenu non ReleaseTest",
                    recognizer.isReleaseTestFileFormat(new StringReader("not release test")));
    }


    @Test
    public void test_isReleaseTestFile_ok() throws Exception {
        assertTrue("Un repertoire vide n'est pas un répertoire de ReleaseTest",
                   recognizer.isReleaseTestFile(getDirectory()));
        assertTrue("Un fichier au format ReleaseTest",
                   recognizer.isReleaseTestFile(toPath("ReleaseTestFile.xml")));
    }


    @Test
    public void test_isReleaseTestFile_unknownFile() throws Exception {
        assertFalse("Un fichier introuvable n'est pas un ReleaseTest",
                    recognizer.isReleaseTestFile("c:/unknown.file"));
    }


    @Test
    public void test_isReleaseTestFile_emptyDirectory() throws Exception {
        File tempBadDirectory = tempFolder.newFolder("tempBadDirectory");
        File.createTempFile("pasTest", ".xml", tempBadDirectory);

        assertFalse("Un repertoire vide n'est pas un répertoire de ReleaseTest",
                    recognizer.isReleaseTestFile(tempBadDirectory.getAbsolutePath()));
    }


    @Test
    public void test_isReleaseTestFile_subDirectory_oneValidFile() throws Exception {
        String dir1 = "dir1";
        String dir2 = "dir2";
        File dir2File = tempFolder.newFolder(dir1, dir2);
        File file1 = new File(dir2File, "file1.xml");
        FileUtil.copy(getReleaseTestFile(), file1);

        assertTrue("A ReleaseTest file must be searched in sub directories",
                    recognizer.isReleaseTestFile(tempFolder.getRoot().getAbsolutePath()));
    }


    @Before
    public void setUp() throws Exception {
        recognizer = new FileFormatRecognizer();
    }

    private String toPath(String name) {
        URL resource = getClass().getResource(name);
        String path = resource.getFile();
        return (path.startsWith("/") ? path.substring(1) : path);
    }


    private File getReleaseTestFile() {
        return new File(toPath("ReleaseTestFile.xml"));
    }


    private String getDirectory() {
        return getReleaseTestFile().getParent();
    }
}
