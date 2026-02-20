package grainalcohol.dtt.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @WrapOperation(
            method = "tickWeather",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerWorldProperties;setRaining(Z)V"
            )
    )
    private void onSetRaining(ServerWorldProperties properties, boolean isRaining, Operation<Void> original) {
        if (!properties.isRaining() && isRaining || properties.isRaining() && !isRaining) {
            // 开始或停止下雨
            ServerWorld serverWorld = (ServerWorld) (Object) this;
            updateHasRained(serverWorld);
        }
        original.call(properties, isRaining);
    }

    @WrapOperation(
            method = "tickWeather",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ServerWorldProperties;setThundering(Z)V"
            )
    )
    private void onSetThundering(ServerWorldProperties properties, boolean isThundering, Operation<Void> original) {
        if (properties.isThundering() != isThundering) {
            // 开始或停止雷暴
            ServerWorld serverWorld = (ServerWorld) (Object) this;
            updateHasRained(serverWorld);
        }
        original.call(properties, isThundering);
    }

    @Unique
    private static void updateHasRained(ServerWorld serverWorld) {
        if (18000 < serverWorld.getTimeOfDay() && serverWorld.getTimeOfDay() < 22000) {
            // 0点后到日出前的变化不记录
            return;
        }
        for (ServerPlayerEntity player : serverWorld.getPlayers(player -> player.getServerWorld().getDimension().hasSkyLight())) {
            DailyStatManager.getTodayStat(player.getUuid()).setTrueStat(DTTDailyStat.RAINED);
        }
    }
}
