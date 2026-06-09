package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import grainalcohol.dtt.api.event.EmotionEvent;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.PTSDContext;
import grainalcohol.dtt.api.event.PTSDEvent;
import grainalcohol.dtt.api.wrapper.MentalIllnessStatus;
import grainalcohol.dtt.api.helper.PTSDHelper;
import grainalcohol.dtt.api.wrapper.PTSDLevel;
import net.depression.mental.MentalStatus;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(MentalStatus.class)
public class MentalStatusMixin {
    @Shadow private ServerPlayerEntity player;

    @Unique private MentalIllnessStatus dtt$lastTickMentalIllnessStatus = MentalIllnessStatus.HEALTHY;
    @Unique private boolean dtt$lastTickIsInCombatState = false;
    @Unique private boolean dtt$lastTickIsManicPhase = false;

    @Inject(
            method = "lambda$tick$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDFormPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private static void PTSDFormEvent1(ServerPlayerEntity player, EntityType<?> entityType, CallbackInfo ci) {
        String ptsdId = EntityType.getId(entityType).toString();
        PTSDContext context = PTSDContext.of(ptsdId, entityType.getName().getString());
        PTSDEvent.PTSD_FORM_EVENT.invoker().onPTSDFormed(player, context, PTSDLevel.LATENT);
    }

    @WrapOperation(
            method = "lambda$tick$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDFormPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private void PTSDFormEvent2(ServerPlayerEntity player, Text id, Operation<Void> original, @Local(name = "string", argsOnly = true) String key) {
        PTSDContext context = PTSDContext.of(key, id.toString());
        PTSDEvent.PTSD_FORM_EVENT.invoker().onPTSDFormed(player, context, PTSDLevel.LATENT);
        original.call(player, id);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/ConcurrentHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object PTSDLevelChangedEvent(
            ConcurrentHashMap<String, Double> map, Object key, Object value,
            Operation<Object> original,
            @Local(name = "originValue") Double originValue
    ) {
        PTSDLevel lastLevel = PTSDHelper.getPTSDLevel(originValue);
        PTSDLevel currentLevel = PTSDHelper.getPTSDLevel((Double) value);
        if (lastLevel != currentLevel) {
            // PTSD等级发生变化
            AtomicReference<String> info = new AtomicReference<>();

            EntityType.get((String) key).ifPresentOrElse(entityType -> {
                info.set(entityType.getName().getString());
            }, () -> info.set((String) key));

            PTSDContext context = PTSDContext.of((String) key, info.get());
            PTSDEvent.PTSD_LEVEL_CHANGED_EVENT.invoker().onPTSDLevelChanged(this.player, context, lastLevel, currentLevel);
        }
        return original.call(map, key, value);
    }

    @Inject(
            method = "lambda$removePTSD$4",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDDispersePacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private void PTSDDisperseEvent1(EntityType<?> entityType, CallbackInfo ci) {
        String ptsdId = EntityType.getId(entityType).toString();
        PTSDContext context = PTSDContext.of(ptsdId, entityType.getName().toString());
        PTSDEvent.PTSD_DISPERSE_EVENT.invoker().onPTSDDisperse(this.player, context);
    }

    @WrapOperation(
            method = "lambda$removePTSD$5",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendPTSDDispersePacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V"
            )
    )
    private void PTSDDisperseEvent2(ServerPlayerEntity player, Text id, Operation<Void> original, @Local(name = "id", argsOnly = true) String key) {
        PTSDContext context = PTSDContext.of(key, id.toString());
        PTSDEvent.PTSD_DISPERSE_EVENT.invoker().onPTSDDisperse(player, context);
        original.call(player, id);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/MentalStatusPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/depression/mental/MentalStatus;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void mentalIllnessChangedEvent(ServerPlayerEntity player, CallbackInfo ci) {
        MentalStatus self = (MentalStatus) (Object) this;
        MentalIllnessStatus currentIllness = MentalIllnessStatus.from(self);

        if (dtt$lastTickMentalIllnessStatus == null) {
            dtt$lastTickMentalIllnessStatus = currentIllness;
            return;
        }

        if (dtt$lastTickMentalIllnessStatus == currentIllness) {
            // 患病情况未改变
            return;
        }

        // 患病情况改变
        MentalIllnessEvent.MENTAL_HEALTH_CHANGED_EVENT.invoker().onMentalIllnessChanged(player, dtt$lastTickMentalIllnessStatus, currentIllness);

        dtt$lastTickMentalIllnessStatus = currentIllness;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/MentalStatusPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/depression/mental/MentalStatus;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void combatStateChangedEvent(CallbackInfo ci) {
        MentalStatus self = (MentalStatus) (Object) this;
        boolean currentIsInCombatState = (self.combatCountdown > 0);

        if (dtt$lastTickIsInCombatState == currentIsInCombatState) {
            // 战斗状态未改变
            return;
        }

        if (!dtt$lastTickIsInCombatState && currentIsInCombatState) {
            // 进入战斗状态
            EmotionEvent.ENTER_COMBAT_STATE_EVENT.invoker().onEnterCombatState(player);
        }

        if (dtt$lastTickIsInCombatState && !currentIsInCombatState) {
            // 退出战斗状态
            EmotionEvent.EXIT_COMBAT_STATE_EVENT.invoker().onExitCombatState(player);
        }

        dtt$lastTickIsInCombatState = currentIsInCombatState;
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("lastTickMentalIllnessStatus")) {
            dtt$lastTickMentalIllnessStatus = MentalIllnessStatus.from(nbt.getString("lastTickMentalIllnessStatus"));
        }
        if (nbt.contains("lastTickIsInCombatState")) {
            dtt$lastTickIsInCombatState = nbt.getBoolean("lastTickIsInCombatState");
        }
        if (nbt.contains("lastTickIsManicPhase")) {
            dtt$lastTickIsManicPhase = nbt.getBoolean("lastTickIsManicPhase");
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("lastTickMentalIllnessStatus", dtt$lastTickMentalIllnessStatus.getName());
        nbt.putBoolean("lastTickIsInCombatState", dtt$lastTickIsInCombatState);
        nbt.putBoolean("lastTickIsManicPhase", dtt$lastTickIsManicPhase);
    }
}
