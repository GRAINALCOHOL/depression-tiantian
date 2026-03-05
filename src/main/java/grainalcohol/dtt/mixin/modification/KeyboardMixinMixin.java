package grainalcohol.dtt.mixin.modification;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.client.DTTServerConfigCache;
import grainalcohol.dtt.config.DTTConfig;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Keyboard.class, priority = 1500)
public class KeyboardMixinMixin {
    @TargetHandler(
            mixin = "net.depression.mixin.client.KeyboardHandlerMixin",
            name = "onKeyPress"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;cancel()V",
                    ordinal = 1
            )
    )
    private void saferCombatForCatatonicStupor(CallbackInfo originalCi, Operation<Void> original) {
        if (!DTTServerConfigCache.saferCatatonicStupor) {
            original.call(originalCi);
        }
    }
}
