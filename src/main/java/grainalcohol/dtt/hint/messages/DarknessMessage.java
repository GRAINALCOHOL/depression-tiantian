package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.hint.SimpleHintMessage;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class DarknessMessage extends SimpleHintMessage {
    private static final Timer COOLDOWN_TIMER = Timer.Builder.builder(12, TimeUnit.GAME_HOUR)
            // 亮度大于7开始处理冷却
            .condition(player -> player.getServerWorld().getLightLevel(player.getBlockPos()) > 7)
            .build();
    private static final Timer ACCUMULATE_TIMER = Timer.Builder.builder(2, TimeUnit.MINUTE)
            // 亮度小于4开始积累
            .condition(player -> player.getServerWorld().getLightLevel(player.getBlockPos()) < 4)
            .build();

    public DarknessMessage(Identifier identifier) {
        super(identifier, true, 3);
    }

    @Override
    public boolean autoSend() {
        return true;
    }

    @Override
    public @Nullable Predicate<ServerPlayerEntity> getGlobalCondition() {
        return null;
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "hint.dtt.darkness";
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return null;
    }

    @Override
    public @NotNull Timer getCooldownTimer() {
        return COOLDOWN_TIMER;
    }

    @Override
    public @Nullable Timer getAccumulateTimer() {
        return ACCUMULATE_TIMER;
    }
}
