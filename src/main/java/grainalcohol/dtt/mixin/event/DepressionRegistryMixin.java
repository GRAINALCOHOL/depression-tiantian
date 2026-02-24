package grainalcohol.dtt.mixin.event;

import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.EmotionEvent;
import net.depression.server.Registry;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registry.class)
public class DepressionRegistryMixin {
    @Inject(method = "eventAddPlayer", at = @At("HEAD"), cancellable = true)
    private static void onAddPlayerHead(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        EventResult eventResult = EmotionEvent.ZEN_STATE_ADDITION_EVENT.invoker().onZenStateChange(event, player);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            ci.cancel();
        }
        // true & default
    }

    @Inject(method = "eventRemovePlayer", at = @At("HEAD"), cancellable = true)
    private static void onRemovePlayerHead(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        EventResult eventResult = EmotionEvent.ZEN_STATE_REMOVAL_EVENT.invoker().onZenStateChange(event, player);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            ci.cancel();
        }
        // true & default
    }

    @Inject(method = "eventAddPlayer", at = @At("TAIL"))
    private static void onEnterZenState(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        if (Registry.playerEventMap.get(player.getUuid()).size() == 1) {
            EmotionEvent.ENTER_ZEN_STATE_EVENT.invoker().onEnterZenState(player);
        }
    }

    @Inject(method = "eventRemovePlayer", at = @At("TAIL"))
    private static void onExitZenState(ServerBossBar event, ServerPlayerEntity player, CallbackInfo ci) {
        if (Registry.playerEventMap.get(player.getUuid()).isEmpty()) {
            EmotionEvent.EXIT_ZEN_STATE_EVENT.invoker().onExitZenState(player);
        }
    }
}
