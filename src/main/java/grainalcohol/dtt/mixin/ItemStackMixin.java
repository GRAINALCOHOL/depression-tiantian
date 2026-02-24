package grainalcohol.dtt.mixin;

import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.api.internal.AnorexiaController;
import grainalcohol.dtt.init.DTTStatusEffect;
import grainalcohol.dtt.api.wrapper.MentalHealthStatus;
import grainalcohol.dtt.api.helper.MentalStatusHelper;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUseHead(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (!stack.isFood() || stack.getItem().getFoodComponent() == null) return;

        if (user.hasStatusEffect(DTTStatusEffect.ANOREXIA)) {
            // 厌食且没有特别饥饿时，禁止进食
            if (user.getHungerManager().getFoodLevel() >= 4) {
                cir.setReturnValue(TypedActionResult.fail(stack));
            }

            // 特别饥饿时允许进食，但延长厌食症状态时间
            StatusEffectInstance anorexiaStatusEffect = user.getStatusEffect(DTTStatusEffect.ANOREXIA);
            if (anorexiaStatusEffect == null) {
                return;
            }

            user.removeStatusEffect(DTTStatusEffect.ANOREXIA);
            FoodComponent foodComponent = stack.getItem().getFoodComponent();
            int extraDuration = (foodComponent.getHunger() + (int) foodComponent.getSaturationModifier()) * 20 * 2; // 每点饥饿值和饱和度各增加1秒
            user.addStatusEffect(new StatusEffectInstance(
                    DTTStatusEffect.ANOREXIA,
                    Math.max(0, anorexiaStatusEffect.getDuration() + extraDuration),
                    anorexiaStatusEffect.getAmplifier(),
                    anorexiaStatusEffect.isAmbient(),
                    anorexiaStatusEffect.shouldShowParticles(),
                    anorexiaStatusEffect.shouldShowIcon()
            ));
        } else {
            // 没有厌食时，随机触发厌食症
            MentalStatus mentalStatus = Registry.mentalStatus.get(user.getUuid());
            boolean shouldTriggerAnorexia = MentalStatusHelper.shouldTriggerAnorexia(mentalStatus);

            // 且饥饿值大于4
            if (user.getHungerManager().getFoodLevel() < 4) {
                shouldTriggerAnorexia = false;
            }

            if (shouldTriggerAnorexia && user instanceof ServerPlayerEntity serverUser) {
                EventResult eventResult = SymptomEvent.ANOREXIA_TRIGGERED.invoker().onAnorexiaTriggered(serverUser, MentalHealthStatus.from(mentalStatus));

                if (eventResult.isPresent() && eventResult.isFalse()) {
                    // false
                    shouldTriggerAnorexia = false;
                }
            }

            // true & default
            // 设置打断进食标志
            if (user instanceof AnorexiaController controller) {
                controller.dtt$setShouldInterruptEating(shouldTriggerAnorexia);
            }
        }
    }
}
