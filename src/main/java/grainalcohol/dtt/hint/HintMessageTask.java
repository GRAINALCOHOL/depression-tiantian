package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HintMessageTask implements Sendable {
    @NotNull
    private final HintMessage hintMessage;
    @NotNull
    private final String[] context;
    @NotNull
    private final Timer lifecycleTimer = Timer.of(30, TimeUnit.SECOND);

    public HintMessageTask(@NotNull HintMessage hintMessage) {
        this(hintMessage, null);
    }

    public HintMessageTask(@NotNull HintMessage hintMessage, @Nullable String[] context) {
        this.hintMessage = hintMessage;
        this.context = context == null ? new String[0] : context;
    }

    public void tick(ServerPlayerEntity player) {
        getLifecycleTimer().tick(player);
    }

    public boolean isExpired() {
        return getLifecycleTimer().isFinished();
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return asHintMessage().getIdentifier();
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
        return true;
    }

    @Override
    public void send(ServerPlayerEntity player) {
        HintMessageSender.immediately(player, this);
        if (getAfterSend() != null) getAfterSend().accept(player);
    }

    @Override
    public @NotNull String[] getContext() {
        return context;
    }

    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return asHintMessage().getAfterSend();
    }

    public @NotNull Timer getLifecycleTimer() {
        return lifecycleTimer;
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }

    @Override
    public @NotNull HintMessage asHintMessage() {
        return hintMessage;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("Identifier", getIdentifier().toString());
        if (getContext().length > 0) {
            NbtList contextList = new NbtList();
            for (String ctx : getContext()) {
                NbtCompound ctxNbt = new NbtCompound();
                ctxNbt.putString("Value", ctx);
                contextList.add(ctxNbt);
            }
            nbt.put("Context", contextList);
        }
        nbt.putInt("LifecycleTimerTicks", getLifecycleTimer().getTicks());
        return nbt;
    }

    public static HintMessageTask fromNbt(NbtCompound nbt) {
        Identifier identifier = new Identifier(nbt.getString("Identifier"));
        HintMessage hintMessage = DTTRegistries.GLOBAL_HINT_MESSAGE_REGISTRY.get(identifier);
        if (hintMessage == null) throw new IllegalStateException("No HintMessage found for identifier: " + identifier);

        String[] context = null;
        if (nbt.contains("Context")) {
            NbtList contextList = nbt.getList("Context", NbtElement.COMPOUND_TYPE);
            context = new String[contextList.size()];
            for (int i = 0; i < contextList.size(); i++) {
                context[i] = contextList.getCompound(i).getString("Value");
            }
        }

        HintMessageTask task = new HintMessageTask(hintMessage, context);
        task.getLifecycleTimer().setTicks(nbt.getInt("LifecycleTimerTicks"));
        return task;
    }
}
