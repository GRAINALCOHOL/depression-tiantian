package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.sugar.Local;
import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.mental.MentalIllness;
import net.depression.mental.MentalStatus;
import net.depression.network.CloseEyePacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MentalIllness.class)
public class MentalIllnessMixin {
    @Unique private int dtt$preventedCloseEyeCount = 0;

    @Shadow @Final private MentalStatus mentalStatus;
    @Shadow private ServerPlayerEntity player;

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendInsomniaPacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onInsomniaTriggered(CallbackInfo ci) {
        MentalIllnessEvent.INSOMNIA_EVENT.invoker().onInsomniaHappened(player);
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/CloseEyePacket;sendToPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
            )
    )
    private void onCloseEyesRedirectAndEvent(ServerPlayerEntity serverPlayerEntity, @Local(name = "isSleepy") boolean isSleepy) {
        // 非战斗状态下正常闭眼
        if (!(mentalStatus.combatCountdown > 0 && DTTConfig.getInstance().getServerConfig().commonConfig.saferCombat)) {
            closeEyes(serverPlayerEntity, isSleepy);
            return;
        }

        int maxCount = DTTConfig.getInstance().getServerConfig().commonConfig.maximumPreventCloseEyesCount;
        if (maxCount <= -1 || dtt$preventedCloseEyeCount < maxCount) {
            // 设置为-1则无限制阻止闭眼
            // 处于战斗状态并且saferCombat配置为true则不会闭眼
            DTTMod.LOGGER.info("{} tried to close eyes during combat, but was prevented by the saferCombat setting.", serverPlayerEntity.getName().getString());
            dtt$preventedCloseEyeCount++;
        } else {
            // 阻止太多次闭眼后强制闭眼
            closeEyes(serverPlayerEntity, isSleepy);
            DTTMod.LOGGER.info("{} has been prevented from closing eyes too many times.", serverPlayerEntity.getName().getString());
            dtt$preventedCloseEyeCount = 0;
        }
    }

    @Unique
    private void closeEyes(ServerPlayerEntity serverPlayerEntity, boolean isSleepy) {
        MentalIllness self = (MentalIllness) (Object) this;
        CloseEyePacket.sendToPlayer(serverPlayerEntity);
        // 触发闭眼事件
        boolean causedByMentalIllness = self.mentalHealthId >= 3 && !self.isMania;
        // 即使depression设计为短路与，这里获取到的isSleepy变量也不会因为短路与而失效
        // 所以必须处理两者都为true的情况，但由于短路与的特性，causedBySleepinessStatusEffect为false
        if (!causedByMentalIllness && isSleepy) {
            // 由困倦状态效果触发闭眼
            MentalIllnessEvent.CLOSE_EYES_EVENT.invoker().onCloseEyes(serverPlayerEntity, true);
        }
        if ((causedByMentalIllness && !isSleepy) || (causedByMentalIllness && isSleepy)) {
            // 由精神疾病触发闭眼
            MentalIllnessEvent.CLOSE_EYES_EVENT.invoker().onCloseEyes(serverPlayerEntity, false);
        }
    }

    @Inject(
            method = "trigMentalFatigue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendMentalFatiguePacket(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onMentalFatigueTriggered(CallbackInfo ci) {
        MentalIllnessEvent.MENTAL_FATIGUE_EVENT.invoker().onMentalFatigueTriggered(player);
    }
}
