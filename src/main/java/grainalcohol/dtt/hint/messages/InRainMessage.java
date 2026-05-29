package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.api.helper.EmotionHelper;
import grainalcohol.dtt.hint.HintMessageContext;
import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class InRainMessage extends SimpleHintMessage {
    private final Timer COOLDOWN_TIMER = Timer.Builder.builder(12, TimeUnit.GAME_HOUR).build();

    public InRainMessage(Identifier identifier) {
        super(identifier, false, 3);
    }

    @Override
    public boolean getGlobalCondition(ServerPlayerEntity player) {
        return player.getServerWorld().hasRain(player.getBlockPos());
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
    return COOLDOWN_TIMER;
    }

    @Override
    public @Nullable Timer getAccumulateTimer() {
        return null;
    }
}
