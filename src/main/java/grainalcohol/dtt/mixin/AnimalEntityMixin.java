package grainalcohol.dtt.mixin;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.init.DTTDailyStat;
import grainalcohol.dtt.init.DTTStat;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin {
    @Inject(
            method = "breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V",
            at = @At("HEAD")
    )
    public void breed(ServerWorld world, AnimalEntity other, PassiveEntity baby, CallbackInfo ci) {
        AnimalEntity self = (AnimalEntity) (Object) this;
        Optional.ofNullable(self.getLovingPlayer()).or(
                () -> Optional.ofNullable(other.getLovingPlayer())
                // 从父母中尝试获取玩家信息
        ).ifPresent(
                (player) -> {
                    // 繁殖宠物
                    player.incrementStat(DTTStat.PET_BRED);
                    DailyStatManager.getTodayStat(player.getUuid()).setTrueStat(DTTDailyStat.PET_BRED);
                }
        );
    }
}
