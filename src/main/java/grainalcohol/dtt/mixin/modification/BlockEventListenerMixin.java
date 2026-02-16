package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.api.internal.BlockBreakMentalHealCooldownController;
import net.depression.listener.BlockEventListener;
import net.depression.mental.MentalStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockEventListener.class)
public class BlockEventListenerMixin {
    @WrapOperation(
            method = "onBlockBreak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/mental/MentalStatus;mentalHeal(Ljava/lang/String;D)D"
            )
    )
    private static double wrapBlockBreakMentalHeal(MentalStatus mentalStatus, String string, double value, Operation<Double> original) {
        BlockBreakMentalHealCooldownController controller = (BlockBreakMentalHealCooldownController) mentalStatus;
        if (controller.dtt$getCooldownTicks() > 0) {
            return 0.0;
        } else {
            controller.dtt$setCooldownTicks(20);
            return original.call(mentalStatus, string, value);
        }
    }
}
