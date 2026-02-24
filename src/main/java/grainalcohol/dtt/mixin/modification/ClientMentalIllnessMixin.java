package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.util.StringUtil;
import net.depression.client.ClientMentalIllness;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ClientMentalIllness.class)
public class ClientMentalIllnessMixin {
    @WrapOperation(
            method = "receiveCloseEyePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;"
            )
    )
    private MutableText closeEyesMessageVariant(String originalKey, Operation<MutableText> original) {
        // TODO: 使文案能够根据精神健康状态不同而不同
        int variantCount = DTTConfig.getInstance().getClientConfig().message_variant_config.close_eyes_message_variant_count;
        if (variantCount > 0) {
            return original.call(StringUtil.findTranslationKeyVariant("message.dtt.close_eyes", variantCount, new Random(), null));
        }
        return original.call(originalKey);
    }
}
