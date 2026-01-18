package grainalcohol.dtt.client;

import dev.architectury.event.events.client.ClientTickEvent;
import grainalcohol.dtt.DTTMod;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class DTTKeyBinds {
    public static final KeyBinding KEY = new KeyBinding(
            "key." + DTTMod.MOD_ID +".test",
            InputUtil.Type.KEYSYM,
            InputUtil.GLFW_KEY_G,
            "category." + DTTMod.MOD_ID
    );

    public static void registerKeyBindings() {
        KeyBindingHelper.registerKeyBinding(KEY);
    }

    public static void init() {
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (KEY.isPressed()) {
                // Toggle a custom shader effect
            }
        });
    }
}
