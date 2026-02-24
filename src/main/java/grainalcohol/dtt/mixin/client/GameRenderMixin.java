package grainalcohol.dtt.mixin.client;

import grainalcohol.dtt.api.wrapper.MentalIllnessStatus;
import net.depression.client.DepressionClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRenderMixin {
    @Shadow abstract void loadPostProcessor(Identifier identifier);
    @Shadow @Nullable PostEffectProcessor postProcessor;

    @Unique private Identifier originalShader;
    @Unique private boolean mentalIllnessShaderApplied = false;

    @Inject(method = "render", at = @At("TAIL"))
    private void loadShaderOnCameraEntity(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        if (MentalIllnessStatus.from(DepressionClient.clientMentalStatus.mentalHealthId).isSeverelyIll()) {
            if (!mentalIllnessShaderApplied) {
                if (postProcessor != null) {
                    originalShader = new Identifier(postProcessor.getName());
                }

                loadPostProcessor(new Identifier("shaders/post/desaturate.json"));
                mentalIllnessShaderApplied = true;
            }
        } else {
            if (originalShader != null) {
                loadPostProcessor(originalShader);
                originalShader = null;
            } else {
                postProcessor = null;
            }
            mentalIllnessShaderApplied = false;
        }
    }
}
