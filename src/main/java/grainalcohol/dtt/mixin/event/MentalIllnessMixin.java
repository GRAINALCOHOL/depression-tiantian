package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.mental.MentalIllness;
import net.depression.mental.MentalStatus;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MentalIllness.class)
public class MentalIllnessMixin {
    @Shadow @Final private MentalStatus mentalStatus;
    @Shadow private ServerPlayerEntity player;
    @Shadow public int mentalHealthId;
    @Shadow public boolean isMania;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendInsomniaPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onInsomniaTriggered(CallbackInfo ci) {
        SymptomEvent.INSOMNIA_EVENT.invoker().onInsomniaHappened(player);
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/CloseEyePacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
            )
    )
    private void wrapCloseEyes(ServerPlayerEntity serverPlayerEntity, Operation<Void> original) {
        if (!this.player.isOnGround()) {
            // 不在地面上不闭眼
            return;
        }

        boolean saferCombat = DTTConfig.getInstance().getServerConfig().combat_config.safer_combat;
        if (saferCombat && mentalStatus.combatCountdown > 0) {
            // 战斗状态下如果saferCombat配置为true则不会闭眼
            return;
        }

        MentalIllness self = (MentalIllness) (Object) this;
        // 触发闭眼事件
        boolean causedByMentalIllness = self.mentalHealthId >= 3 && !self.isMania;
        // 外部被mentalHealthId >= 3 && !isMania || isSleepy包围
        // 所以causedByMentalIllness和isSleepy肯定有一个为true
        // 两个都为true时优先说触发原因是精神疾病，即传入false
        // causedByMentalIllness为false时说明是Sleepiness状态效果触发的闭眼，此时传入true
        EventResult eventResult =  SymptomEvent.CLOSE_EYES_EVENT.invoker().onCloseEyes(serverPlayerEntity, !causedByMentalIllness);
        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            return;
        }
        // true & default
        original.call(serverPlayerEntity);
    }

    @Inject(method = "trigMentalFatigue", at = @At(value = "HEAD"), cancellable = true)
    private void onMentalFatigueTriggered(CallbackInfo ci) {
        EventResult eventResult = SymptomEvent.MENTAL_FATIGUE_EVENT.invoker().onMentalFatigueTriggered(this.player);
        if (eventResult.isPresent() && eventResult.isFalse()) {
            // false
            ci.cancel();
        }
        // true & default
    }

    @WrapOperation(
            method = "trigMentalFatigue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectInstance;getAmplifier()I"
            )
    )
    private int wrapMentalFatigueAmplifier(StatusEffectInstance instance, Operation<Integer> original) {
        // 没法改局部变量啊，重写太不优雅了，只能改这个（
        if (DTTConfig.getInstance().getServerConfig().common_config.mental_fatigue_trigger_chance_fixer) {
            if (mentalHealthId == 4 && isMania) {
                // 躁狂状态不触发精神疲劳，即使持有抗抑郁状态效果
                return -1;
            }
            return (original.call(instance) + 1) * 2 - 1; // 使抗抑郁状态效果的触发加成翻倍
        }
        return original.call(instance);
    }
}
