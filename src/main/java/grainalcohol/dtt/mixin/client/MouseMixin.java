package grainalcohol.dtt.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import grainalcohol.dtt.api.wrapper.MentalIllnessStatus;
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
    private boolean fixLookDirection(ClientPlayerEntity instance, double cursorDeltaX, double cursorDeltaY) {
        ClientMentalStatus clientMentalStatus = DepressionClient.clientMentalStatus;
        boolean isSeverelyIll = MentalIllnessStatus.from(clientMentalStatus.mentalHealthId).isSeverelyIll();
        boolean isEyesClosed = clientMentalStatus.mentalIllness.isCloseEye;

        // 患重病且闭眼时固定视角
        if (isSeverelyIll && isEyesClosed) return false;
        return true;
    }
}
