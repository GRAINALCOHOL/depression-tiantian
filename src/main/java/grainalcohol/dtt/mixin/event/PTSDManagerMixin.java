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
                    target = "Lnet/depression/network/PTSDOnsetPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ID)V"
            )
    )
    private void PTSDEventTrigger(ServerPlayerEntity player, int onsetLevel, double distance) {
        EventResult result = PTSDEvent.PTSD_TRIGGERED_EVENT.invoker().onPTSDTriggered(player, onsetLevel, distance);
        
        if (result.isPresent()) {
            if (result.isTrue()) {
                // true
                PTSDOnsetPacket.sendToPlayer(player, onsetLevel, distance);
            }

            // false
            return;
        }

        // default
        PTSDOnsetPacket.sendToPlayer(player, onsetLevel, distance);
    }
}
