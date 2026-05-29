package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class HintMessageTask implements Sendable {
    private final Identifier identifier;
    private final String translationKey;
    private final int variantCount;
    @Nullable
    private final Consumer<ServerPlayerEntity> afterSend;
    @Nullable
    private final HintMessageContext<?> context;
    @NotNull
    private final Timer lifecycleTimer = Timer.Builder.builder(30, TimeUnit.SECOND).build();

    public HintMessageTask(Sendable sendable) {
        this(sendable, null);
    }

    public HintMessageTask(Sendable sendable, @Nullable HintMessageContext<?> context) {
        this(sendable.getIdentifier(), sendable.getTranslationKey(),
                sendable.getVariantCount(), sendable.getAfterSend(), context);
    }

    private HintMessageTask(
            Identifier identifier, String translationKey, int variantCount,
            @Nullable Consumer<ServerPlayerEntity> afterSend,
            @Nullable HintMessageContext<?> context
    ) {
        this.identifier = identifier;
        this.translationKey = translationKey;
        this.variantCount = variantCount;
        this.afterSend = afterSend;
        this.context = context;
    }

    public void tick(ServerPlayerEntity player) {
        getLifecycleTimer().tick(player);
    }

    public boolean isExpired() {
        return getLifecycleTimer().isFinished();
    }

    @Override
    public Identifier getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public int getVariantCount() {
        return this.variantCount;
    }

    @Override
    public boolean isImportant() {
        // 这是即将要发送的消息，为false的话就永远在队列里发不出去了
        return true;
    }

    @Override
    public void send(ServerPlayerEntity player) {
        HintMessageSender.send(player, this);
    }

    public @Nullable HintMessageContext<?> getHintMessageContext() {
        return this.context;
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return this.afterSend;
    }

    public @NotNull Timer getLifecycleTimer() {
        return lifecycleTimer;
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
