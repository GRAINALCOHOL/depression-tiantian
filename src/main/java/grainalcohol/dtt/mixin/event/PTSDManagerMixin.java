package grainalcohol.dtt.mixin.event;

import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.PTSDEvent;
import net.depression.mental.PTSDManager;
import net.depression.network.PTSDOnsetPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PTSDManager.class)
public class PTSDManagerMixin {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/PTSDOnsetPacket;sendPhotismPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/lang/String;)V"
            )
    )
    private void onPTSDPhotismTriggered(ServerPlayerEntity player, String photismId) {
        EventResult eventResult = PTSDEvent.PTSD_PHOTISM_EVENT.invoker().onPTSDPhotismTriggered(player, photismId);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }

        // true & default
        PTSDOnsetPacket.sendPhotismPacket(player, photismId);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/PTSDOnsetPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ID)V"
            )
    )
    private void PTSDEventTrigger(ServerPlayerEntity player, int onsetLevel, double distance) {
        EventResult eventResult = PTSDEvent.PTSD_TRIGGERED_EVENT.invoker().onPTSDTriggered(player, onsetLevel, distance);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }

        // true & default
        PTSDOnsetPacket.sendToPlayer(player, onsetLevel, distance);
    }
}
