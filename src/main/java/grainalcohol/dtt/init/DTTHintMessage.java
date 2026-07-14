package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.hint.HintMessage;
import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.SubscriptionHintMessage;
import grainalcohol.dtt.hint.messages.*;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.registry.Registry;

public class DTTHintMessage {
    public static final SubscriptionHintMessage ANOREXIA_MESSAGE = new AnorexiaMessage(DTTMod.id("anorexia"));
    public static final SubscriptionHintMessage ANOREXIA_WORSEN_MESSAGE = new AnorexiaWorsenMessage(DTTMod.id("anorexia_worsen"));
    public static final SimpleHintMessage DARKNESS_MESSAGE = new DarknessMessage(DTTMod.id("darkness"));
    public static final SimpleHintMessage IN_RAIN_MESSAGE = new InRainMessage(DTTMod.id("in_rain"));
    public static final SubscriptionHintMessage RESET_SPAWN_POINT_MESSAGE = new ResetSpawnPointMessage(DTTMod.id("reset_spawn_point"));
    public static final SubscriptionHintMessage JUKEBOX_MESSAGE = new JukeboxMessage(DTTMod.id("jukebox"));
    public static final SubscriptionHintMessage PET_MESSAGE = new PetMessage(DTTMod.id("pet"));
    // PTSD
    public static final SubscriptionHintMessage LATENT_PTSD_MESSAGE = new LatentPTSDMessage(DTTMod.id("ptsd_latent"));
    public static final SubscriptionHintMessage FORM_PTSD_MESSAGE = new FormPTSDMessage(DTTMod.id("ptsd_form"));
    public static final SubscriptionHintMessage EXTREME_PTSD_MESSAGE = new ExtremePTSDMessage(DTTMod.id("ptsd_extreme"));
    public static final SubscriptionHintMessage DISPERSE_PTSD_MESSAGE = new DispersePTSDMessage(DTTMod.id("ptsd_disperse"));
    public static final SubscriptionHintMessage REMISSION_PTSD_MESSAGE = new RemissionPTSDMessage(DTTMod.id("ptsd_remission"));

    public static void init() {
        register(ANOREXIA_MESSAGE);
        register(DARKNESS_MESSAGE);
        register(IN_RAIN_MESSAGE);
        register(RESET_SPAWN_POINT_MESSAGE);
        register(JUKEBOX_MESSAGE);
        register(PET_MESSAGE);
        register(LATENT_PTSD_MESSAGE);
        register(FORM_PTSD_MESSAGE);
        register(EXTREME_PTSD_MESSAGE);
        register(DISPERSE_PTSD_MESSAGE);
        register(REMISSION_PTSD_MESSAGE);
    }

    private static void register(HintMessage hintMessage) {
        Registry.register(DTTRegistries.HINT_MESSAGE_REGISTRY, hintMessage.getIdentifier(), hintMessage);
    }
}
