package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.api.helper.MentalStatusHelper;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.util.NearbyMentalHealHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PetMessage extends SimpleHintMessage {
    private static final Timer ACCUMULATE_TIMER = Timer.Builder
            .builder(3, TimeUnit.SECOND)
            // 在宠物附近待3秒
            .condition(player -> NearbyMentalHealHelper.isPetNearby(player, 4))
            .build();

    public PetMessage(Identifier identifier) {
        super(identifier, false, 3);
    }

    @Override
    public @Nullable Predicate<ServerPlayerEntity> getGlobalCondition() {
        return player -> DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyPetMode == ServerConfig.NearbyAnythingHealMode.EXIST;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "hint.dtt.pet";
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return player -> MentalStatusHelper.getMentalStatus(player).mentalHeal("pet", 2.0);
    }

    @Override
    public @NotNull Timer getCooldownTimer() {
        return DEFAULT_TIMER;
    }

    @Override
    public @Nullable Timer getAccumulateTimer() {
        return ACCUMULATE_TIMER;
    }
}
