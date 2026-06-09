package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class SubscriptionHintMessage extends HintMessage {
    public SubscriptionHintMessage(Identifier identifier, boolean isImportant, int variantCount) {
        super(identifier, isImportant, variantCount);
    }

    public void trigger(ServerPlayerEntity player, @NotNull Text... context) {
        HintMessageSender.trigger(player, this, context);
    }

    public void trigger(ServerPlayerEntity player, @NotNull String... context) {
        HintMessageSender.trigger(player, this, context);
    }

    public void trigger(ServerPlayerEntity player) {
        HintMessageSender.trigger(player, this);
    }

    @Override
    public final @Nullable Predicate<ServerPlayerEntity> getGlobalCondition() {
        return null;
    }

    @Override
    public final boolean autoSend() {
        return false;
    }

    @Override
    public final @Nullable Timer getAccumulateTimer() {
        return null;
    }
}
