package grainalcohol.dtt.mixin.event;

import grainalcohol.dtt.api.event.DepressionCombatStateEvent;
import grainalcohol.dtt.api.event.MentalHealthEvent;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.mental.MentalIllnessStatus;
import net.depression.mental.MentalStatus;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MentalStatus.class)
public class MentalStatusMixin {
    @Shadow private ServerPlayerEntity player;

    @Unique private MentalIllnessStatus dtt$lastTickMentalIllnessStatus = MentalIllnessStatus.HEALTHY;
    @Unique private boolean dtt$lastTickIsInCombatState = false;
    @Unique private boolean dtt$lastTickIsManicPhase = false;

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
            DepressionCombatStateEvent.ENTER_COMBAT_STATE_EVENT.invoker().onEnterCombatState(player);
        }

        if (dtt$lastTickIsInCombatState && !currentIsInCombatState) {
            // 退出战斗状态
            DepressionCombatStateEvent.EXIT_COMBAT_STATE_EVENT.invoker().onExitCombatState(player);
        }

        dtt$lastTickIsInCombatState = currentIsInCombatState;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/MentalStatusPacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/depression/mental/MentalStatus;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void manicPhaseChangedEvent(CallbackInfo ci) {
        MentalStatus self = (MentalStatus) (Object) this;

        if (!MentalIllnessStatus.from(self).isBipolarDisorder()) {
            dtt$lastTickIsManicPhase = false;
            return;
        }

        boolean currentIsManicPhase = self.isMania();

        if (dtt$lastTickIsManicPhase == currentIsManicPhase) {
            // 躁狂状态未改变
            return;
        }

        if (!dtt$lastTickIsManicPhase && currentIsManicPhase) {
            // 进入躁狂相
            MentalHealthEvent.ENTER_MANIC_PHASE_EVENT.invoker().onEnterManicPhase(player);
        }

        if (dtt$lastTickIsManicPhase && !currentIsManicPhase) {
            // 退出躁狂相
            MentalHealthEvent.EXIT_MANIC_PHASE_EVENT.invoker().onExitManicPhase(player);
        }

        dtt$lastTickIsManicPhase = currentIsManicPhase;
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
