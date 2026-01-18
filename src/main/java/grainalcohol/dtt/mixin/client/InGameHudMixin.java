package grainalcohol.dtt.mixin.client;

import net.depression.client.DepressionClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                            args = "stringValue=food"
                    ),
                    to = @At(
                            value = "CONSTANT",
                            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                            args = "stringValue=air"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
                    ordinal = 1
            )
    )
    private void redirectFullHungerValueTextures(DrawContext context, Identifier identifier, int x, int y, int u, int v, int width, int height) {
        if (DepressionClient.clientMentalStatus.mentalHealthId < 3) {
            context.drawTexture(identifier, x, y, u, v, width, height);
        }
    }

    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                            args = "stringValue=food"
                    ),
                    to = @At(
                            value = "CONSTANT",
                            target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V",
                            args = "stringValue=air"
                    )
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIIII)V",
                    ordinal = 2
            )
    )
    private void redirectHalfHungerValueTextures(DrawContext context, Identifier identifier, int x, int y, int u, int v, int width, int height) {
        if (DepressionClient.clientMentalStatus.mentalHealthId < 3) {
            context.drawTexture(identifier, x, y, u, v, width, height);
        }
    }
}
