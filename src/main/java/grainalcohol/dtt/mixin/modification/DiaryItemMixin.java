package grainalcohol.dtt.mixin.modification;

import net.depression.client.ClientActionbarHint;
import net.depression.item.DiaryItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DiaryItem.class)
public class DiaryItemMixin {
    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/client/ClientActionbarHint;displayTranslatable(Ljava/lang/String;)V"
            )
    )
    private void changeUnwrittenMessage(String originalKey) {
        ClientActionbarHint.displayTranslatable("message.dtt.diary_unwritten");
    }
}
