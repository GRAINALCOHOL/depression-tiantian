package grainalcohol.dtt.mixin;

import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import net.minecraft.advancement.criterion.BrewedPotionCriterion;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewedPotionCriterion.class)
public class BrewedPotionCriterionMixin {
    @Inject(method = "trigger", at = @At("HEAD"))
    private void onTriggerInject(ServerPlayerEntity player, Potion potion, CallbackInfo ci) {
        if (player == null || potion == null || potion.getEffects().isEmpty() || potion.equals(Potions.WATER) || potion.equals(Potions.EMPTY)) {
            return;
        }
        DailyStatManager.getTodayDailyStat(player.getUuid()).increaseBrewedCount(1);
    }
}
