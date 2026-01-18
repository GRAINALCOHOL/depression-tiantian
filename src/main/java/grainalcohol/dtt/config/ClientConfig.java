package grainalcohol.dtt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grainalcohol.dtt.mixin.event.ClientMentalIllnessMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG = new File(DTTConfig.CONFIG_DIR, "client.json");

    /**
     * 默认60，单位为tick，触发闭眼效果后，客户端延迟多少tick开始展示视觉效果
     * @see ClientMentalIllnessMixin
     */
    public int closeEyeDelayTicks = 60;

    public static ClientConfig load() {
        try {
            if (!DTTConfig.CONFIG_DIR.exists()) {
                Files.createDirectories(DTTConfig.CONFIG_DIR.toPath());
            }

            if (CONFIG.exists()) {
                try (FileReader reader = new FileReader(CONFIG)) {
                    return GSON.fromJson(reader, ClientConfig.class);
                } catch (IOException e) {
                    LOGGER.error("Failed to load common config file", e);
                }
            }

            ClientConfig config = new ClientConfig();
            config.save();
            return config;
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return new ClientConfig();
        }
    }

    public void save() {
        try {
            if (!DTTConfig.CONFIG_DIR.exists()) {
                Files.createDirectories(DTTConfig.CONFIG_DIR.toPath());
            }

            try (FileWriter writer = new FileWriter(CONFIG)) {
                GSON.toJson(this, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save common config file", e);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
    }
}
