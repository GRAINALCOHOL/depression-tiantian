package grainalcohol.dtt.mixin;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.mental.EmotionHelper;
import grainalcohol.dtt.util.StringUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements EyesStatusFlagController {
    @Unique private static final Random dtt$random = new Random();
    @Unique private boolean dtt$isEyesClosed = false;

    @Unique private boolean dtt$hasCheckInRain = false;
    @Unique private boolean dtt$hasResetSpawnPoint = false;

    @Override
    public boolean dtt$getIsEyesClosedFlag() {
        return this.dtt$isEyesClosed;
    }

    @Override
    public void dtt$setIsEyesClosedFlag(boolean isClosed) {
        this.dtt$isEyesClosed = isClosed;
    }

    @Inject(
            method = "setSpawnPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendMessage(Lnet/minecraft/text/Text;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onSetSpawnPoint(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        // 不是为啥ServerTask没法用，受不了了
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(() -> {
            try {
                // 3s later
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (!dtt$hasResetSpawnPoint) {
                EmotionHelper.addEmotionValue(self, 2);
                self.sendMessage(Text.translatable("message.dtt.respawn_point_reset"), true);
                dtt$hasResetSpawnPoint = true;
            }
        });
        executor.close();

//        serverWorld.getServer().send(new ServerTask(
//            serverWorld.getServer().getTicks() + 200,
//            () -> {
//                DTTMod.LOGGER.info("200ticks later");
//            }
//        ));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();

        if (!dtt$hasCheckInRain && self.age % 100 == 0 && serverWorld.hasRain(self.getBlockPos())) {
            // 每5秒 淋到雨时
            EmotionHelper.addEmotionValue(self, -2);
            self.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(
                            "message.dtt.in_rain", 3, dtt$random)), true
            );
            dtt$hasCheckInRain = true;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();
        if (serverWorld.getTimeOfDay() % 24000 == 0) {
            // 每天0时更新统计数据
            DailyStatManager.updateDailyStat(self);
        }
        if (serverWorld.getTimeOfDay() % 12000 == 0) {
            // 每天更新两次标记
            dtt$hasCheckInRain = false;
            dtt$hasResetSpawnPoint = false;
        }
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

    @Inject(method = "wakeUp", at = @At("TAIL"))
    private void onWakeUp(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();
        if (!(self.getSleepTimer() == 0)) {
            // 不是自然醒
            return;
        }

        if (serverWorld.getDimension().hasSkyLight() && serverWorld.isRaining()) {
            // 睡醒时
            DailyStatManager.getTodayDailyStat(self.getUuid()).setHasRained(true);
        }
    }
}
