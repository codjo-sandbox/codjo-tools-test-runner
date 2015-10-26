package net.codjo.test.runner.release;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.intellij.openapi.components.ApplicationComponent.Adapter;

public class ReleaseTestRunConfigurationType extends Adapter implements ConfigurationType {
    private final ReleaseTestConfigurationFactory factory;
    private Icon icon;


    public ReleaseTestRunConfigurationType() {
        factory = new ReleaseTestConfigurationFactory(this);
    }


    public String getDisplayName() {
        return "Release Test";
    }


    public String getConfigurationTypeDescription() {
        return "Configuration d'exécution d'un Test au format codjo-test-release";
    }


    public Icon getIcon() {
        if (icon == null) {
            icon = IconLoader.getIcon("icon-small.png", getClass());
        }
        return icon;
    }


    @NotNull
    public String getId() {
        return "ReleaseTestRunConfigurationType";
    }


    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{factory};
    }


    static String getVMParametersForReleaseTest(InputStream inputStream)
          throws IOException, RuntimeConfigurationException {
        Properties properties = getPropertyFromInputStream(inputStream);

        if (properties.containsKey("vmParameters") && properties.containsKey("vmParameter")) {
            throw new RuntimeException(
                  "Fichier de config incorrect, propriétés incompatibles : vmParameters et vmParameter.");
        }

        String vmParameters = properties.getProperty("vmParameters");
        if (vmParameters != null) {
            return vmParameters;
        }

        return properties.getProperty("vmParameter");
    }


    static Properties getPropertyFromInputStream(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }
}
