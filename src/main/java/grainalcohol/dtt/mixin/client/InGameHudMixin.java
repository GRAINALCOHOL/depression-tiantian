package grainalcohol.dtt.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.depression.client.DepressionClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @WrapOperation(
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
    private void redirectFullHungerValueTextures(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height, Operation<Void> original) {
        if (DepressionClient.clientMentalStatus.mentalHealthId < 3) {
            original.call(context, texture, x, y, u, v, width, height);
        }
    }

    @WrapOperation(
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
    private void redirectHalfHungerValueTextures(DrawContext context, Identifier texture, int x, int y, int u, int v, int width, int height, Operation<Void> original) {
        if (DepressionClient.clientMentalStatus.mentalHealthId < 3) {
            original.call(context, texture, x, y, u, v, width, height);
        }
    }
}
