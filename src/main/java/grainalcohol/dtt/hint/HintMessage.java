package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class HintMessage implements Sendable {
    private final Identifier identifier;
    private final boolean isImportant;
    private final int variantCount;
    private final boolean autoSend;

    public HintMessage(Identifier identifier, boolean isImportant, boolean autoSend, int variantCount) {
        this.identifier = identifier;
        this.isImportant = isImportant;
        this.autoSend = autoSend;
        this.variantCount = variantCount;
    }

    public HintMessage(Identifier identifier, boolean isImportant, int variantCount) {
        this(identifier, isImportant, true, variantCount);
    }

    public void tick(ServerPlayerEntity player) {
        getCooldownTimer().tick(player);

        if (getAccumulateTimer() != null && getCooldownTimer().isFinished()) {
            getAccumulateTimer().tick(player);
        }

        if (isAutoSend() && canSend() && getGlobalCondition(player)) send(player);
    }

    public void send(ServerPlayerEntity player) {
        HintMessageSender.send(player, this);

        getCooldownTimer().reset();
        if (getAccumulateTimer() != null) getAccumulateTimer().reset();
        if (getAfterSend() != null) getAfterSend().accept(player);
    }

    private boolean canSend() {
        return getCooldownTimer().isFinished() && (getAccumulateTimer() == null || getAccumulateTimer().isFinished());
    }

    public abstract boolean getGlobalCondition(ServerPlayerEntity player);

    @NotNull
    public abstract String getTranslationKey();
    @Nullable
    public abstract Consumer<ServerPlayerEntity> getAfterSend();
    @NotNull
    public abstract Timer getCooldownTimer();
    @Nullable
    public abstract Timer getAccumulateTimer();

    public int getVariantCount() {
        return variantCount;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public boolean isAutoSend() {
        return autoSend;
    }
}
