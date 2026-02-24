package grainalcohol.dtt.mixin;

import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.api.internal.AnorexiaController;
import grainalcohol.dtt.init.DTTStatusEffect;
import grainalcohol.dtt.api.wrapper.MentalHealthStatus;
import grainalcohol.dtt.api.wrapper.MentalIllnessStatus;
import grainalcohol.dtt.api.wrapper.Severity;
import grainalcohol.dtt.util.MathUtil;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements AnorexiaController {
    @Shadow protected int itemUseTimeLeft;
    @Unique private boolean dtt$shouldInterruptEating = false;

    @Override
    public void dtt$setShouldInterruptEating(boolean shouldInterrupt) {
        this.dtt$shouldInterruptEating = shouldInterrupt;
    }

    @Inject(
            method = "tickItemStackUsage",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/LivingEntity;itemUseTimeLeft:I",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private void interruptEating(ItemStack stack, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(stack.isFood() && self instanceof ServerPlayerEntity player)) {
            return;
        }

        int maxUseTime = stack.getMaxUseTime();
        if (maxUseTime <= 0) return;

        // 进食进度超过50%
        double eatingProgress = (double) (maxUseTime - this.itemUseTimeLeft) / maxUseTime;
        if (eatingProgress <= 0.5) return;

        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        if (this.dtt$shouldInterruptEating) {
            EventResult eventResult = SymptomEvent.INTERRUPT_EATING.invoker().onInterruptEating(player, MentalHealthStatus.from(mentalStatus));

            if (eventResult.isPresent() && eventResult.isFalse()) {
                // false
                this.dtt$shouldInterruptEating = false;
                return;
            }

            // true & default
            // 没有厌食效果，且饥饿值>=4。打断
            player.stopUsingItem();
            Severity severity = MentalIllnessStatus.from(mentalStatus).getSeverity();
            int durationTicks = MathUtil.inRange(new Random(), 20 * 10, 20 * 20) * Math.max(1, severity.getLevel()); // 10~20秒 * 严重度（1~3）
            if (severity.isSeverelyIll()) {
                durationTicks += MathUtil.inRange(new Random(), 20 * 20, 20 * 60); // 20~60秒
            }
            player.addStatusEffect(new StatusEffectInstance(
                    DTTStatusEffect.ANOREXIA, durationTicks, Math.max(0, severity.getLevel() - 1),
                    false, true, true)
            );
            // 这里期望换成actionbar的提示
            player.sendMessage(Text.literal("eating food is interrupted due to anorexia!"));
            this.dtt$shouldInterruptEating = false;
        }
    }
}
