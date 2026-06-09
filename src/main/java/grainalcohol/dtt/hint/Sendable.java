package grainalcohol.dtt.hint;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface Sendable {
    @NotNull HintMessage asHintMessage();
    @NotNull Identifier getIdentifier();
    void send(ServerPlayerEntity player);
    @NotNull String getTranslationKey();
    int getVariantCount();
    boolean isImportant();
    @NotNull String[] getContext();
}
