package grainalcohol.dtt.mixin;

import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Raid.class)
public abstract class RaidMixin {
    @Unique private boolean dtt$raidHasLostYet = false;

    @Shadow protected abstract Predicate<ServerPlayerEntity> isInRaidDistance();

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        Raid raid = (Raid) (Object) this;
        if (!(raid.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        if (!raid.hasLost()) {
            dtt$raidHasLostYet = false;
            return;
        }

        if (!dtt$raidHasLostYet && raid.hasLost()) {
            dtt$raidHasLostYet = true;
            List<ServerPlayerEntity> players = serverWorld.getPlayers(isInRaidDistance());
            players.forEach(player -> DailyStatManager.getTodayDailyStat(player.getUuid()).setHasRaidFailed(true));
        }
    }
}
