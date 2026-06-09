package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.PTSDContext;
import grainalcohol.dtt.api.event.PTSDEvent;
import grainalcohol.dtt.api.helper.PTSDHelper;
import grainalcohol.dtt.api.wrapper.PTSDLevel;
import net.depression.mental.PTSDManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(PTSDManager.class)
public class PTSDManagerMixin {
    @Shadow @Final private ConcurrentHashMap<String, Double> PTSD;

    @Inject(
            method = "lambda$tick$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDRemissionPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private static void PTSDRemissionEvent1(ServerPlayerEntity player, EntityType<?> entityType, CallbackInfo ci) {
        // PTSD缓解事件
        String ptsdId = EntityType.getId(entityType).toString();
        PTSDContext context = PTSDContext.of(ptsdId, entityType.getName().getString());
        PTSDEvent.PTSD_REMISSION_EVENT.invoker().onPTSDRemission(player, context, PTSDHelper.getPTSDLevel(player, ptsdId));
    }

    @WrapOperation(
            method = "lambda$tick$3",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDRemissionPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private void PTSDRemissionEvent2(ServerPlayerEntity player, Text id, Operation<Void> original, @Local(name = "key", argsOnly = true) String key) {
        // PTSD缓解事件
        PTSDContext context = PTSDContext.of(key, id.toString());
        PTSDEvent.PTSD_REMISSION_EVENT.invoker().onPTSDRemission(player, context, PTSDHelper.getPTSDLevel(player, key));
        original.call(player, id);
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
        EventResult eventResult = PTSDEvent.PTSD_ONSET_EVENT.invoker().onPTSDOnset(player, PTSDLevel.from(onsetLevel), distance);

        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }

        // true & default
        original.call(player, onsetLevel, distance);
    }
}
