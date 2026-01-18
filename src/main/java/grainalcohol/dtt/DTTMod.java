package grainalcohol.dtt;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.init.DTTCommand;
import grainalcohol.dtt.init.DTTListener;
import grainalcohol.dtt.init.DTTNetwork;
import grainalcohol.dtt.init.DTTStat;
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

        DTTNetwork.init();
        DTTConfig.getInstance();
        DTTStat.init();
        DTTListener.archEventInit();
        DTTListener.dttEventInit();
        DTTCommand.register();
	}
}