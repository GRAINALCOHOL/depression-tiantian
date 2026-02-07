package grainalcohol.dtt.mixin;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements EyesStatusFlagController {
    @Unique private boolean dtt$isEyesClosed = false;

    @Override
    public boolean dtt$getIsEyesClosedFlag() {
        return this.dtt$isEyesClosed;
    }

    @Override
    public void dtt$setIsEyesClosedFlag(boolean isClosed) {
        this.dtt$isEyesClosed = isClosed;
    }

    @Inject(
            method = "worldChanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancement/criterion/ChangedDimensionCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/registry/RegistryKey;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onWorldChanged(ServerWorld origin, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld newWorld = self.getServerWorld();
        if (newWorld.getDimension().hasSkyLight() && newWorld.isRaining()) {
            // 切换维度时
            DailyStatManager.getTodayDailyStat(self.getUuid()).setHasRained(true);
        }
    }

    @Inject(method = "wakeUp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;wakeUp(ZZ)V"))
    private void onWakeUp(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();
        if (serverWorld.getDimension().hasSkyLight() && serverWorld.isRaining()) {
            // 睡醒时
            DailyStatManager.getTodayDailyStat(self.getUuid()).setHasRained(true);
        }
    }
}
