package grainalcohol.dtt.mixin.modification;

import grainalcohol.dtt.api.internal.PlayerLookDirectionController;
import net.depression.client.ClientMentalIllness;
import net.depression.client.DepressionClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements PlayerLookDirectionController {
    @Unique
    private boolean dtt$shouldFixFaceDirection = false;

    @Override
    public void dtt$setShouldFixFaceDirection(boolean shouldFix) {
        dtt$shouldFixFaceDirection = shouldFix;
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (dtt$shouldFixFaceDirection) {
            ci.cancel();
        }
    }
}
