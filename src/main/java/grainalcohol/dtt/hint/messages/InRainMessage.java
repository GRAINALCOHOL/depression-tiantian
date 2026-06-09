package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.api.helper.EmotionHelper;
import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class InRainMessage extends SimpleHintMessage {
    public InRainMessage(Identifier identifier) {
        super(identifier, false, 3);
    }

    @Override
    public boolean autoSend() {
        return true;
    }

    @Override
    public @Nullable Predicate<ServerPlayerEntity> getGlobalCondition() {
        return player -> player.getServerWorld().hasRain(player.getBlockPos());
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "hint.dtt.in_rain";
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return player -> EmotionHelper.mentalHurt(player, 2.0);
    }

    @Override
    public @NotNull Timer getCooldownTimer() {
        return DEFAULT_TIMER;
    }

    @Override
    public @Nullable Timer getAccumulateTimer() {
        return null;
    }
}
