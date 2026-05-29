package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class SubscriptionHintMessage extends HintMessage {
    public SubscriptionHintMessage(Identifier identifier, boolean isImportant, int variantCount) {
        super(identifier, isImportant, false, variantCount);
    }

    @Override
    public boolean getGlobalCondition(ServerPlayerEntity player) {
        return true;
    }

    @Override
    public @Nullable Timer getAccumulateTimer() {
        return null;
    }
}
