package grainalcohol.dtt.mixin;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.init.DTTDailyStat;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "eatFood", at = @At("RETURN"))
    private void onEatFoodReturn(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.getWorld().isClient()) return;

        DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.ATE);
    }

    @Inject(
            method = "applyDamage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;addExhaustion(F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void easierCombatStateAboutDamageTaken(DamageSource source, float amount, CallbackInfo ci) {
        // 未开启功能
        if (!DTTConfig.getInstance().getServerConfig().combatConfig.easierCombatState) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        MentalStatus mentalStatus = Registry.mentalStatus.get(self.getUuid());
        // depression这里写的是 mentalStatus.emotionValue <= 2.0
        if (mentalStatus.emotionValue > 2.0) {
            mentalStatus.combatCountdown = 10;
        }
    }
}
