package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class HintMessageInstance implements Sendable {
    @NotNull
    private final HintMessage hintMessage;
    @NotNull
    private final Timer cooldownTimer;
    @Nullable
    private final Timer accumulateTimer;

    public HintMessageInstance(HintMessage hintMessage) {
        this.hintMessage = hintMessage;
        this.cooldownTimer = hintMessage.getCooldownTimer().copy();
        this.accumulateTimer = hintMessage.getAccumulateTimer() == null ? null : hintMessage.getAccumulateTimer().copy();
    }

    @Nullable
    public Timer getAccumulateTimer() {
        return accumulateTimer;
    }

    @NotNull
    public Timer getCooldownTimer() {
        return cooldownTimer;
    }

    @Override
    public @NotNull HintMessage asHintMessage() {
        return hintMessage;
    }

    public void tick(ServerPlayerEntity player) {
        if (getGlobalCondition() != null && !getGlobalCondition().test(player)) return;

        getCooldownTimer().tick(player);
        if (getAccumulateTimer() != null && getCooldownTimer().isFinished()) getAccumulateTimer().tick(player);

        if (asHintMessage().autoSend() && canSend()) send(player);
    }

    private boolean canSend() {
        return (getAccumulateTimer() == null || getAccumulateTimer().isFinished()) && getCooldownTimer().isFinished();
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return asHintMessage().getIdentifier();
    }

    @Override
    public void send(ServerPlayerEntity player) {
        HintMessageSender.send(player, this);

        resetTimer();

        if (getAfterSend() != null) getAfterSend().accept(player);
    }

    public void resetTimer() {
        getCooldownTimer().reset();
        if (getAccumulateTimer() != null) getAccumulateTimer().reset();
    }

    @Override
    public @NotNull String getTranslationKey() {
        return asHintMessage().getTranslationKey();
    }

    @Override
    public int getVariantCount() {
        return asHintMessage().getVariantCount();
    }

    @Override
    public boolean isImportant() {
        return asHintMessage().isImportant();
    }

    @Override
    public @NotNull String[] getContext() {
        return new String[0];
    }

    @Nullable
    public Predicate<ServerPlayerEntity> getGlobalCondition() {
        return asHintMessage().getGlobalCondition();
    }

    @Nullable
    public Consumer<ServerPlayerEntity> getAfterSend() {
        return asHintMessage().getAfterSend();
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Identifier", getIdentifier().toString());
        nbt.putInt("CooldownTimerTicks", getCooldownTimer().getTicks());
        if (getAccumulateTimer() != null) {
            nbt.putInt("AccumulateTimerTicks", getAccumulateTimer().getTicks());
        }
        return nbt;
    }

    public static HintMessageInstance fromNbt(NbtCompound nbt) {
        Identifier identifier = new Identifier(nbt.getString("Identifier"));
        HintMessage hintMessage = DTTRegistries.GLOBAL_HINT_MESSAGE_REGISTRY.get(identifier);
        if (hintMessage == null) throw new IllegalStateException("No HintMessage found for identifier: " + identifier);

        HintMessageInstance instance = hintMessage.createInstance();
        instance.getCooldownTimer().setTicks(nbt.getInt("CooldownTimerTicks"));
        if (nbt.contains("AccumulateTimerTicks") && instance.getAccumulateTimer() != null) {
            instance.getAccumulateTimer().setTicks(nbt.getInt("AccumulateTimerTicks"));
        }
        return instance;
    }
}
