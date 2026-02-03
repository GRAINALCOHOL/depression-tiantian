package grainalcohol.dtt.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.mental.MentalIllnessStatus;
import net.depression.client.ClientMentalStatus;
import net.depression.client.DepressionClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {
    @WrapWithCondition(
            method = "updateMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"
            )
    )
    private boolean wrapChangeLookDirectionCondition(ClientPlayerEntity instance, double cursorDeltaX, double cursorDeltaY) {
        if (!DTTConfig.getInstance().getServerConfig().commonConfig.shouldFixedFaceDirectionWhenCatatonicStupor) {
            // 配置未启用，允许改变视角
            return true;
        }
        ClientMentalStatus clientMentalStatus = DepressionClient.clientMentalStatus;
        boolean isSeverelyIll = MentalIllnessStatus.from(clientMentalStatus.mentalHealthId).isSeverelyIll();
        boolean isEyesClosed = clientMentalStatus.mentalIllness.isCloseEye;

        // 患重病且闭眼时
        if (isSeverelyIll && isEyesClosed) return false;
        return true;
    }
}
