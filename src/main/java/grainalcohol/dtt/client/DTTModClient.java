package grainalcohol.dtt.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DTTModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DTTKeyBinds.registerKeyBindings();
        DTTKeyBinds.init();
    }
}
