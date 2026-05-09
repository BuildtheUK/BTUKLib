package org.btuk.minecraft.config;

import lombok.extern.java.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log
public class PluginConfig {

    private static final String CONFIG_VERSION_KEY = "version";

    private static final String CONFIG_FILE_NAME = "config.yml";

    private final JavaPlugin instance;

    private FileConfiguration config;

    private final Map<String, Object> configValues = new HashMap<>();

    public PluginConfig(JavaPlugin instance) {
        this.instance = instance;
        this.config = instance.getConfig();
        reload();
    }

    public void reload() {
        this.configValues.clear();
        updateConfig();
        loadConfig();
    }

    public <T> T get(String key, Class<T> type) {
        Object value = configValues.get(key);
        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                "Config value '" + key + "' is " + value.getClass().getSimpleName() + ", expected " + type.getSimpleName()
            );
        }

        return type.cast(value);
    }

    public <T> T get(String key, Class<T> type, T defaultValue) {
        Object value = configValues.getOrDefault(key, defaultValue);

        if (!type.isInstance(value)) {
            return defaultValue;
        }

        return type.cast(value);
    }

    private void loadConfig() {
        configValues.putAll(config.getValues(true));
    }

    private void updateConfig() {
        String configVersion = getConfigVersion();
        String latestConfigVersion = getLatestConfigVersion();

        if (latestConfigVersion == null) {
            log.severe("The plugin config is invalid, can't update the config!");
            return;
        }

        if (configVersion != null && configVersion.equals(latestConfigVersion)) {
            log.info("The config is up to date!");
            return;
        }

        log.info("The config version is not equal the latest version, updating config!");

        // Get the config values from the current file.
        Map<String, Object> values = config.getValues(true);

        // Generate a new config file from the default config.
        // Copy any values that can be reused.
        // Delete the current config and set the new one.
        File configFile = new File(instance.getDataFolder(), CONFIG_FILE_NAME);

        if (!configFile.delete()) {
            // Something went wrong.
            log.warning("The config file could not be cleared, updating config failed!");
            return;
        }

        // Copy the default config and get it.
        instance.saveDefaultConfig();
        instance.reloadConfig();
        config = instance.getConfig();

        // Replace any values from the previous config.
        for (Map.Entry<String, Object> value : values.entrySet()) {
            if (config.contains(value.getKey())) {
                // Check if this is a configuration section, if true, skip.
                if (config.isConfigurationSection(value.getKey())) {
                    continue;
                }

                // Skip the version since that needs to be the latest value.
                if (value.getKey().equals(CONFIG_VERSION_KEY)) {
                    continue;
                }
                config.set(value.getKey(), value.getValue());
            }
        }

        instance.saveConfig();
        config = instance.getConfig();
        log.info("Updated config to version " + latestConfigVersion);
    }

    private String getConfigVersion() {
        return config.getString(CONFIG_VERSION_KEY);
    }

    // Get latest config version.
    private String getLatestConfigVersion() {
        return Objects.requireNonNull(config.getDefaults()).getString(CONFIG_VERSION_KEY);
    }
}
