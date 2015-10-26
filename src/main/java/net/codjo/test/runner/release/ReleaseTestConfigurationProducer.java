package net.codjo.test.runner.release;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.junit.JavaRunConfigurationProducerBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 *
 */
public class ReleaseTestConfigurationProducer extends JavaRunConfigurationProducerBase<ReleaseTestRunConfiguration> {
    static final String MODULE_NAME_SUFFIX = "-release-test";
    static final String USE_CASE_DIRECTORY = "/src/main/usecase";

    private final FileFormatRecognizer recognizer = new FileFormatRecognizer();

    protected ReleaseTestConfigurationProducer(ConfigurationFactory configurationFactory) {
        super(configurationFactory);
    }

    protected ReleaseTestConfigurationProducer(ConfigurationType configurationType) {
        super(configurationType);
    }

    @Override
    protected boolean setupConfigurationFromContext(ReleaseTestRunConfiguration configuration, ConfigurationContext configurationContext, Ref<PsiElement> ref) {
        Location location = configurationContext.getLocation();
        if (location == null) {
            return false;
        }

        VirtualFile currentFile = getReleaseTestFile(location);
        if (currentFile == null) {
            return false;
        }

        Project project = location.getProject();
        configuration.setReleaseTestFileName(currentFile.getPath());
        configuration.setTargetModuleName(ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(currentFile).getName());

        VirtualFile path = manageMultiReleaseTestModules(currentFile, project);

        if (path == null) {
            path = project.getBaseDir()
                  .findFileByRelativePath(
                        project.getName() + MODULE_NAME_SUFFIX + "/target/config/test-release.config");
        }
        configuration.setVMParameters(getVMParametersForReleaseTest(path));
        configuration.setName(buildSettingsName(location, currentFile));
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(ReleaseTestRunConfiguration runConfiguration, ConfigurationContext configurationContext) {
        VirtualFile releaseTestFile = getReleaseTestFile(configurationContext.getLocation());
        if (releaseTestFile == null) {
            return false;
        }

        return runConfiguration.getReleaseTestFileName().equals(releaseTestFile.getPath());
    }

    @Nullable
    private VirtualFile getReleaseTestFile(Location location) {
        if (!ReleaseTestRunConfiguration.isValidModule(location.getModule())) {
            return null;
        }

        VirtualFile currentFile;
        if (location.getOpenFileDescriptor() == null) {
            PsiDirectory psiDirectory = (PsiDirectory)location.getPsiElement();
            currentFile = psiDirectory.getVirtualFile();
        }
        else {
            currentFile = location.getOpenFileDescriptor().getFile();
        }

        String expectedRootDir = getExpectedRootDir(location, currentFile);
        if (!currentFile.getPath().startsWith(expectedRootDir)) {
            // inside release-test module but not under use case directory
            return location.getModule().getModuleFile().getParent().findFileByRelativePath(USE_CASE_DIRECTORY);
        }

        if (!recognizer.isReleaseTestFile(currentFile.getPath())) {
            return null;
        }
        return currentFile;
    }

    @NotNull
    private String getExpectedRootDir(Location location, VirtualFile currentFile) {
        String expectedRootDir = location.getModule().getModuleFile().getParent().getCanonicalPath().replace(File.separatorChar, '/') + USE_CASE_DIRECTORY;
        if (!currentFile.isDirectory()) {
            expectedRootDir += '/';
        }
        return expectedRootDir;
    }

    private String getVMParametersForReleaseTest(VirtualFile ideaConfig) {
        try {
            InputStream inputStream = ideaConfig.getInputStream();

            return ReleaseTestRunConfigurationType.getVMParametersForReleaseTest(inputStream);
        }

        catch (Exception exception) {
            return exception.getLocalizedMessage();
        }
    }


    @SuppressWarnings({"ConstantConditions"})
    private VirtualFile manageMultiReleaseTestModules(VirtualFile currentFile, Project project) {
        VirtualFile releaseTestModuleFile = null;
        VirtualFile parentFile = currentFile;
        while (releaseTestModuleFile == null && parentFile != null) {
            parentFile = parentFile.getParent();
            if (parentFile != null && parentFile.getPath().endsWith(MODULE_NAME_SUFFIX)) {
                releaseTestModuleFile = parentFile;
            }
        }
        if (releaseTestModuleFile != null) {
            String releaseTestPath = releaseTestModuleFile.getPath();
            int beginLength = releaseTestModuleFile.getParent().getParent().getPath().length();

            String baseTestReleasePath = releaseTestPath.substring(beginLength + 1);

            return project.getBaseDir()
                  .findFileByRelativePath(baseTestReleasePath + "/target/config/test-release.config");
        }
        return null;
    }


    private String buildSettingsName(Location location, VirtualFile currentFile) {
        if (currentFile.isDirectory()) {
            if (currentFile.getPath().equals(getExpectedRootDir(location, currentFile))) {
                return "All stories";
            } else {
                return "Stories " + currentFile.getName();
            }
        }
        return "Story " + currentFile.getNameWithoutExtension();
    }
}
