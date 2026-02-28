package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.client.ClientPTSDManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPTSDManager.class)
public class ClientPTSDManagerMixin {
    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;setPitch(F)V"
            )
    )
    private void wrapSetPitch(PlayerEntity player, float pitch, Operation<Void> original) {
        if (DTTConfig.getInstance().getClientConfig().visualConfig.disablePantingVisualAnimation) {
            return;
        }
        original.call(player, pitch);
    }
}
