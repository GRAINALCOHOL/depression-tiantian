package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.PTSDEvent;
import grainalcohol.dtt.api.helper.PTSDHelper;
import grainalcohol.dtt.api.wrapper.PTSDLevel;
import net.depression.mental.PTSDManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(PTSDManager.class)
public class PTSDManagerMixin {
    @Shadow
    @Final
    private ConcurrentHashMap<String, Double> PTSD;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityType;get(Ljava/lang/String;)Ljava/util/Optional;",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void PTSDRemissionEvent(ServerPlayerEntity player, CallbackInfo ci, @Local(name = "key") String PTSDId) {
        // PTSD缓解事件
        PTSDEvent.PTSD_REMISSION_EVENT.invoker().onPTSDRemission(player, PTSDId, PTSDHelper.getPTSDLevel(this.PTSD.get(PTSDId)));
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/PTSDOnsetPacket;sendPhotismPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/lang/String;)V"
            )
    )
    private void PTSDPhotismEvent(ServerPlayerEntity player, String photismId, Operation<Void> original) {
        EventResult eventResult = PTSDEvent.PTSD_PHOTISM_EVENT.invoker().onPTSDPhotismTriggered(player, photismId);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }

        // true & default
        original.call(player, photismId);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/PTSDOnsetPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;ID)V"
            )
    )
    private void PTSDTriggerEvent(ServerPlayerEntity player, int onsetLevel, double distance, Operation<Void> original) {
        EventResult eventResult = PTSDEvent.PTSD_TRIGGERED_EVENT.invoker().onPTSDTriggered(player, PTSDLevel.from(onsetLevel), distance);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }

        // true & default
        original.call(player, onsetLevel, distance);
    }
}
