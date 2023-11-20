package io.github.surajkumar.wazei.config;

import java.util.List;
import java.util.Map;

public class ConfigSearcher {
    private final List<Config> configs;

    public ConfigSearcher(List<Config> configs) {
        this.configs = configs;
    }

    public Config getConfigForPath(String path) {
        for (Config configuration : configs) {
            for (Metadata metadata : configuration.getMetadata()) {
                Map<String, String> config = metadata.getConfig();
                if (config.containsKey("path")) {
                    if (config.get("path").equalsIgnoreCase(path)) {
                        return configuration;
                    }
                }
            }
        }
        return null;
    }
}
