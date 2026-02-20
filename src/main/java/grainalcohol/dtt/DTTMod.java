package grainalcohol.dtt;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.init.*;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTTMod implements ModInitializer {
	public static final String MOD_ID = "dtt";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        // TODO：增加一个类似“无力”的状态效果，该效果下无法与方块/实体互动，无法下载具/生物
        DTTNetwork.init();
        DTTConfig.getInstance();
        DTTStat.init();
        DTTDailyStat.init();
        DTTTopic.init();
        DTTStatusEffect.init();
        DTTListener.archEventInit();
        DTTListener.dttEventInit();
        DTTListener.dttAPIEventInit();
        DTTCommand.register();
	}
}