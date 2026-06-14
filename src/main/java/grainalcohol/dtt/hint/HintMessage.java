package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class HintMessage {
    public static final Timer DEFAULT_TIMER = Timer.of(12, TimeUnit.GAME_HOUR);
    private final Identifier identifier;
    private final boolean isImportant;
    private final int variantCount;

    public HintMessage(Identifier identifier, boolean isImportant, int variantCount) {
        this.identifier = identifier;
        this.isImportant = isImportant;
        this.variantCount = variantCount;
        // 自动注册到全局注册表中，方便同时管理
        Registry.register(DTTRegistries.GLOBAL_HINT_MESSAGE_REGISTRY, this.getIdentifier(), this);
    }

    @NotNull
    public Identifier getIdentifier() {
        return identifier;
    }

    public int getVariantCount() {
        return variantCount;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public abstract boolean autoSend();

    @Nullable
    public abstract Predicate<ServerPlayerEntity> getGlobalCondition();

    @NotNull
    public abstract String getTranslationKey();

    @Nullable
    public abstract Consumer<ServerPlayerEntity> getAfterSend();

    @NotNull
    public abstract Timer getCooldownTimer();

    @Nullable
    public abstract Timer getAccumulateTimer();

    @NotNull
    public HintMessageInstance createInstance() {
        return new HintMessageInstance(this);
    }
}
