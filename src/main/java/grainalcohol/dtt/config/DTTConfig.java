package grainalcohol.dtt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;
import grainalcohol.dtt.DTTMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class DTTConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DTTConfig.class);
    private static final File CONFIG_DIR = new File(Platform.getConfigFolder().toFile(), DTTMod.MOD_ID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final DTTConfig INSTANCE = load();

    private ServerConfig serverConfig = new ServerConfig();
    private ClientConfig clientConfig = new ClientConfig();

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public static DTTConfig getInstance() {
        return INSTANCE;
    }

    private static DTTConfig load() {
        try {
            if (!CONFIG_DIR.exists()) {
                Files.createDirectories(CONFIG_DIR.toPath());
            }

            DTTConfig config = new DTTConfig();

            config.serverConfig = loadConfig(
                    new File(DTTConfig.CONFIG_DIR, "server.json"),
                    ServerConfig.class,
                    new ServerConfig()
            );
            config.clientConfig = loadConfig(
                    new File(DTTConfig.CONFIG_DIR, "client.json"),
                    ClientConfig.class,
                    new ClientConfig()
            );

            return config;
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return new DTTConfig();
        }
    }

    protected static <T> T loadConfig(File configFile, Class<T> configClass, T defaultConfig) {
        try {
            if (!CONFIG_DIR.exists()) {
                Files.createDirectories(CONFIG_DIR.toPath());
            }

            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    return GSON.fromJson(reader, configClass);
                } catch (IOException e) {
                    LOGGER.error("Failed to load config file: {}", configFile.getName(), e);
                }
            }

            saveConfig(configFile, defaultConfig);
            return defaultConfig;
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return defaultConfig;
        }
    }

    protected static <T> void saveConfig(File configFile, T config) {
        try {
            if (!CONFIG_DIR.exists()) {
                Files.createDirectories(CONFIG_DIR.toPath());
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save config file: {}", configFile.getName(), e);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
    }
}
