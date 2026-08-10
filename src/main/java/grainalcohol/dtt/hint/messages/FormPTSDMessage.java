package grainalcohol.dtt.hint.messages;

import grainalcohol.dtt.hint.SubscriptionHintMessage;
import grainalcohol.dtt.hint.timer.Timer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FormPTSDMessage extends SubscriptionHintMessage {
    public FormPTSDMessage(Identifier identifier) {
        super(identifier, false, 3);
    }

    @Override
    public @NotNull String getTranslationKey() {
        return "hint.dtt.form_ptsd";
    }

    @Override
    public @Nullable Consumer<ServerPlayerEntity> getAfterSend() {
        return null;
    }

    @Override
    public @NotNull Timer getCooldownTimer() {
        return DEFAULT_TIMER;
    }
}
