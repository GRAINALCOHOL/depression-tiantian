package grainalcohol.dtt.mixin.modification;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobEntity.class, priority = 1500)
public class MobEntityMixinMixin {
    @TargetHandler(
            mixin = "net.depression.mixin.emotion.MobMixin",
            name = "onTick"
    )
    @ModifyConstant(
            method = "@MixinSquared:Handler",
            constant = @Constant(intValue = 20)
    )
    private int modifyPetMentalHealInterval(int original) {
        return DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyPetIntervalTicks;
    }

    @TargetHandler(
            mixin = "net.depression.mixin.emotion.MobMixin",
            name = "onTick"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTickHead(CallbackInfo originalCi, CallbackInfo ci) {
        if (DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyPetMode != ServerConfig.NearbyAnythingHealMode.EVERYONE) {
            ci.cancel();
        }
    }

    @TargetHandler(
            mixin = "net.depression.mixin.emotion.MobMixin",
            name = "onTick"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/passive/TameableEntity;getSavedEntityId()Ljava/lang/String;"
            )
    )
    private String modifyPetMentalHealMode(String original) {
        return "pet";
    }
}
