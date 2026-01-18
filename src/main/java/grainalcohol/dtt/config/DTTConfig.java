package grainalcohol.dtt.config;

import dev.architectury.platform.Platform;
import grainalcohol.dtt.DTTMod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DTTConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DTTConfig.class);
    public static final File CONFIG_DIR = new File(Platform.getConfigFolder().toFile(), DTTMod.MOD_ID);

    private static DTTConfig instance;

    private ServerConfig serverConfig = new ServerConfig();
    private ClientConfig clientConfig = new ClientConfig();

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public static DTTConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    // TODO：优化配置加载方法的结构，看起来有不少重复代码
    private static DTTConfig load() {
        try {
            if (!CONFIG_DIR.exists()) {
                Files.createDirectories(CONFIG_DIR.toPath());
            }

            DTTConfig config = new DTTConfig();

            config.serverConfig = ServerConfig.load();
            config.clientConfig = ClientConfig.load();

            return config;
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return new DTTConfig();
        }
    }
}
