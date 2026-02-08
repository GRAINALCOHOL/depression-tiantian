package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import grainalcohol.dtt.config.ClientConfig;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.util.StringUtil;
import net.depression.client.ClientActionbarHint;
import net.depression.client.DepressionClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Random;

@Mixin(ClientActionbarHint.class)
public class ClientActionbarHintMixin {
    @Unique private static final Random RANDOM = new Random();

    @ModifyConstant(method = "receiveFishHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyFishHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @ModifyConstant(method = "receiveFeedAnimalHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyFeedAnimalHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @ModifyConstant(method = "receivePetHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyPetHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @ModifyConstant(method = "receiveLootHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyLootHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }
    
    @ModifyConstant(method = "receiveNearbyBlockHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyNearbyBlockHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @ModifyConstant(method = "receiveBreakBlockHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyBreakBlockHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @ModifyConstant(method = "receiveKillEntityHealPacket", constant = @Constant(longValue = 1200L))
    private long modifyKillEntityHealMessageInterval(long original) {
        return Math.max(1200, DTTConfig.getInstance().getClientConfig().heal_message_interval_ticks);
    }

    @WrapOperation(
            method = "receiveKillEntityHealPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"
            )
    )
    private void enhancedKillEntityHealMessage(InGameHud inGameHud, Text message, boolean tinted, Operation<Void> original, @Local(name = "id") Text entityId) {
        ClientConfig clientConfig = DTTConfig.getInstance().getClientConfig();
        if (!clientConfig.enhanced_kill_entity_message) {
            original.call(inGameHud, message, tinted);
        } else {
            double emotionValue = DepressionClient.clientMentalStatus.emotionValue;
            String translationKey = emotionValue >= 0 ? "message.dtt.kill_entity_heal.positive" : "message.dtt.kill_entity_heal.negative";
            inGameHud.setOverlayMessage(Text.translatable(translationKey, entityId), tinted);
        }
    }

    @WrapOperation(
            method = "receiveMentalFatiguePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText mentalFatigueMessageVariant(String originalKey, Operation<MutableText> original) {
        // TODO: 使文案能够根据精神健康状态不同而不同
        int variantCount = DTTConfig.getInstance().getClientConfig().mental_fatigue_message_variant_count;
        if (variantCount > 0) {
            return original.call(StringUtil.findTranslationKeyVariant(
                    "message.dtt.mental_fatigue", variantCount, RANDOM)
            );
        }
        return original.call(originalKey);
    }
}
