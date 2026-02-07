package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.mental.MentalStatusHelper;
import grainalcohol.dtt.mental.MentalHealthStatus;
import grainalcohol.dtt.mental.MentalIllnessStatus;
import net.depression.client.ClientMentalStatus;
import net.depression.item.MentalHealthScaleItem;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MentalHealthScaleItem.class)
public class MentalHealthScaleItemMixin {
    @WrapWithCondition(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ItemCooldownManager;set(Lnet/minecraft/item/Item;I)V"))
    private boolean shouldApplyCooldown(ItemCooldownManager instance, Item item, int duration){
        // true表示禁用，所以取反
        return !DTTConfig.getInstance().getServerConfig().itemConfig.disable_mental_health_scale_cooldown;
    }

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
                    ordinal = 1
            )
    )
    private TypedActionResult<ItemStack> itemDecrement(Item instance, World world, PlayerEntity player, Hand interactionHand) {
        ItemStack itemStack = player.getStackInHand(interactionHand);
        if (DTTConfig.getInstance().getServerConfig().itemConfig.disposable_mental_health_scale) {
            if (!player.isCreative()) {
                itemStack.decrement(1);
            }
            return TypedActionResult.success(itemStack);
        }

        return instance.use(world, player, interactionHand);
    }

    @WrapWithCondition(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;sendMessage(Lnet/minecraft/text/Text;)V",
                    ordinal = 0
            )
    )
    private boolean enhancedFirstMessage(PlayerEntity player, Text message, @Local(name = "mentalStatus") ClientMentalStatus mentalStatus) {
        if (DTTConfig.getInstance().getServerConfig().itemConfig.enhanced_mental_health_scale_action) {
            // 心理健康指数
            player.sendMessage(
                    Text.translatable(
                            "message.dtt.mental_health_scale.mental_health_index",
                            String.format("%.2f", MentalStatusHelper.getMentalHealthRate(mentalStatus.mentalHealthValue) * 10) // 这可能是暂时的？
                    ).formatted(Formatting.UNDERLINE)
                    // 悬停时显示详细数据
                    .styled(style -> style.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(Text.translatable(
                            "text.dtt.mental_health_scale.mental_health_index.hover",
                            String.format("%.4f", mentalStatus.mentalHealthValue),
                            String.format("%.2f", mentalStatus.emotionValue))))
                    )
            );
            return false;
        }
        return true;
    }

    @WrapWithCondition(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;sendMessage(Lnet/minecraft/text/Text;)V",
                    ordinal = 1
            )
    )
    private boolean enhancedSecondMessage(PlayerEntity player, Text message, @Local(name = "mentalStatus") ClientMentalStatus mentalStatus) {
        if (DTTConfig.getInstance().getServerConfig().itemConfig.enhanced_mental_health_scale_action) {
            // 心理评估结果
            player.sendMessage(
                    Text.translatable(
                            "message.dtt.mental_health_scale.assessment",
                            MentalStatusHelper.getAssessmentText(MentalHealthStatus.from(mentalStatus))
                    ).formatted(Formatting.UNDERLINE)
                    // 悬停时显示患病情况
                    .styled(style -> style.withHoverEvent(HoverEvent.Action.SHOW_TEXT.buildHoverEvent(Text.translatable(
                            "text.dtt.mental_health_scale.assessment.hover", MentalIllnessStatus.from(mentalStatus.mentalHealthId).getDisplayText())))
                    )
            );
            return false;
        }
        return true;
    }
}
