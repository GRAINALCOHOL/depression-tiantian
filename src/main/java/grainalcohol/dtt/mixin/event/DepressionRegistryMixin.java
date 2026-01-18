package grainalcohol.dtt.mixin.event;

import grainalcohol.dtt.api.event.DepressionCombatStateEvent;
import net.depression.server.Registry;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registry.class)
public class DepressionRegistryMixin {
    @Inject(method = "eventAddPlayer", at = @At("TAIL"))
    private static void onEnterZenState(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        DepressionCombatStateEvent.ENTER_ZEN_STATE_EVENT.invoker().onEnterZenState(event, player);
    }

    @Inject(method = "eventRemovePlayer", at = @At("TAIL"))
    private static void onExitZenState(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        DepressionCombatStateEvent.EXIT_ZEN_STATE_EVENT.invoker().onExitZenState(event, player);
    }
}
