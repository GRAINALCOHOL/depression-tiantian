package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.api.helper.MentalStatusHelper;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.util.NearbyMentalHealHelper;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class JukeboxMessage extends SimpleHintMessage {
    private static final Timer ACCUMULATE_TIMER = Timer.Builder
            .builder(3, TimeUnit.SECOND)
            // 在活动的唱片机附近待3秒
            .condition(player -> NearbyMentalHealHelper.findNearestPlayingJukeboxEntity(player, 4) != null)
            .build();

    public JukeboxMessage(Identifier identifier) {
        super(identifier, false, 3);
    }

    @Override
    public @Nullable Predicate<ServerPlayerEntity> getGlobalCondition() {
        return player -> DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyJukeboxMode == ServerConfig.NearbyAnythingHealMode.EXIST;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "hint.dtt.jukebox";
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return player -> {
            var nearestPlayingJukebox = NearbyMentalHealHelper.findNearestPlayingJukeboxEntity(player, 4);
            if (nearestPlayingJukebox != null) {
                Identifier recordItemId = Registries.ITEM.getId(nearestPlayingJukebox.getStack().getItem());
                MentalStatusHelper.getMentalStatus(player).mentalHeal(recordItemId.toString(), 2.0);
            }
        };
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
