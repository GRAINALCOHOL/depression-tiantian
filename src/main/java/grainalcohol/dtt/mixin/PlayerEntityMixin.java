package grainalcohol.dtt.mixin;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
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
        if (self.getWorld().isClient()) {
            return;
        }
        DailyStatManager.getTodayDailyStat(self.getUuid()).setHasAte(true);
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
        if (!DTTConfig.getInstance().getServerConfig().combatConfig.easier_combat_state) {
            // 未开启功能
            return;
        }

        PlayerEntity self = (PlayerEntity) (Object) this;
        MentalStatus mentalStatus = Registry.mentalStatus.get(self.getUuid());
        // depression这里写的是 mentalStatus.emotionValue <= 2.0
        if (mentalStatus.emotionValue > 2.0) {
            mentalStatus.combatCountdown = 10;
        }
    }

    @Inject(method = "increaseStat(Lnet/minecraft/stat/Stat;I)V", at = @At("HEAD"))
    private void increaseDailyStat(Stat<?> stat, int amount, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (self.getWorld().isClient()) {
            return;
        }

        if (stat.getType() == Stats.CUSTOM) {
            Identifier statId = (Identifier) stat.getValue();

            if (statId.equals(Stats.WALK_ONE_CM)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).increaseDistanceMoved(amount);
            }
            if (statId.equals(Stats.DAMAGE_TAKEN)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).increaseDamageTaken(amount);
            }
            if (statId.equals(Stats.TRADED_WITH_VILLAGER)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).increaseTradedCount(amount);
            }
            if (statId.equals(Stats.POT_FLOWER)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).setHasFlowerPotted(true);
            }
            if (statId.equals(Stats.RAID_WIN)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).setHasRaidWon(true);
            }
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readDailyStatFromNbt(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (self.getWorld().isClient()) {
            return;
        }

        if (nbt.contains(DailyStat.DAILY_STAT_NBT_KEY)) {
            NbtCompound dailyStatNbt = nbt.getCompound(DailyStat.DAILY_STAT_NBT_KEY);

            if (dailyStatNbt.contains(DailyStat.TODAY_DAILY_STAT_NBT_KEY)) {
                DailyStat todayStat = new DailyStat();
                todayStat.readFromNbt(dailyStatNbt.getCompound(DailyStat.TODAY_DAILY_STAT_NBT_KEY));
                DailyStatManager.setTodayDailyStat(self.getUuid(), todayStat);
            }
            if (dailyStatNbt.contains(DailyStat.YESTERDAY_DAILY_STAT_NBT_KEY)) {
                DailyStat yesterdayStat = new DailyStat();
                yesterdayStat.readFromNbt(dailyStatNbt.getCompound(DailyStat.YESTERDAY_DAILY_STAT_NBT_KEY));
                DailyStatManager.setYesterdayDailyStat(self.getUuid(), yesterdayStat);
            }
            if (dailyStatNbt.contains(DailyStat.MOVING_AVERAGE_DAILY_STAT_NBT_KEY)) {
                DailyStat movingAverageStat = new DailyStat();
                movingAverageStat.readFromNbt(dailyStatNbt.getCompound(DailyStat.MOVING_AVERAGE_DAILY_STAT_NBT_KEY));
                DailyStatManager.setMovingAverageDailyStat(self.getUuid(), movingAverageStat);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeDailyStatToNbt(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (self.getWorld().isClient()) {
            return;
        }

        NbtCompound dailyStatNbt = new NbtCompound();

        NbtCompound todayNbtCompound = new NbtCompound();
        DailyStatManager.getTodayDailyStat(self.getUuid()).writeToNbt(todayNbtCompound);
        dailyStatNbt.put(DailyStat.TODAY_DAILY_STAT_NBT_KEY, todayNbtCompound);

        NbtCompound yesterdayNbtCompound = new NbtCompound();
        DailyStatManager.getYesterdayDailyStat(self.getUuid()).writeToNbt(yesterdayNbtCompound);
        dailyStatNbt.put(DailyStat.YESTERDAY_DAILY_STAT_NBT_KEY, yesterdayNbtCompound);

        NbtCompound movingAverageNbtCompound = new NbtCompound();
        DailyStatManager.getMovingAverageDailyStat(self.getUuid()).writeToNbt(movingAverageNbtCompound);
        dailyStatNbt.put(DailyStat.MOVING_AVERAGE_DAILY_STAT_NBT_KEY, movingAverageNbtCompound);

        nbt.put(DailyStat.DAILY_STAT_NBT_KEY, dailyStatNbt);
    }
}
