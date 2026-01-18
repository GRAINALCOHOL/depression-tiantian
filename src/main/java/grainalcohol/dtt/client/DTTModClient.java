package grainalcohol.dtt.client;

import net.fabricmc.api.ClientModInitializer;

public class DTTModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        DTTKeyBinds.registerKeyBindings();
        DTTKeyBinds.init();
    }
}
