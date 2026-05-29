package grainalcohol.dtt.mixin;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.topic.v2.TopicManager;
import grainalcohol.dtt.hint.HintMessageSender;
import grainalcohol.dtt.init.DTTDailyStat;
import grainalcohol.dtt.api.helper.MentalStatusHelper;
import grainalcohol.dtt.init.DTTHintMessage;
import grainalcohol.dtt.util.NearbyMentalHealHelper;
import net.depression.mental.MentalStatus;
import net.depression.network.ActionbarHintPacket;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements EyesStatusFlagController {
    // 服务端标记
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
            method = "setSpawnPoint",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendMessage(Lnet/minecraft/text/Text;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onSetSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (!sendMessage) {
            return;
        }

        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();

        new Thread(() -> {
            try {
                // 5s later
                Thread.sleep(5000);

                serverWorld.getServer().execute(() ->
                        HintMessageSender.trigger(self, DTTHintMessage.RESET_SPAWN_POINT_MESSAGE)
                );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        ServerConfig.MentalHealConfig mentalHealConfig = DTTConfig.getInstance().getServerConfig().mentalHealConfig;
        MentalStatus mentalStatus = MentalStatusHelper.getMentalStatus(self);
        // 宠物恢复情绪
        if (mentalHealConfig.nearbyPetMode == ServerConfig.NearbyAnythingHealMode.EXIST) {
            // exist模式
            if (self.age % mentalHealConfig.nearbyPetIntervalTicks == 0
                    && NearbyMentalHealHelper.isPetNearby(self, 4)) {
                // 每隔一段时间，并且附近存在宠物时
                double healValue = mentalStatus.mentalHeal("pet", 1.5);
                if (healValue > 0.5) {
                    // depression原版的管线
                    ActionbarHintPacket.sendPetHealPacket(self, Text.translatable("message.dtt.pet"));
                }
            }
        }
        // 唱片机恢复情绪
        if (mentalHealConfig.nearbyJukeboxMode == ServerConfig.NearbyAnythingHealMode.EXIST) {
            // exist模式
            JukeboxBlockEntity nearestPlayingJukebox = NearbyMentalHealHelper.findNearestPlayingJukeboxEntity(self, 4);
            if (self.age % mentalHealConfig.nearbyJukeboxIntervalTicks == 0 && nearestPlayingJukebox != null) {
                // 每隔一段时间，并且附近存在正在播放的唱片机时
                Identifier recordItemId = Registries.ITEM.getId(nearestPlayingJukebox.getStack().getItem());
                double healValue = mentalStatus.mentalHeal(recordItemId.toString(), 1.0);
                if (healValue > 0.5) {
                    HintMessageSender.trigger(self, DTTHintMessage.JUKEBOX_MESSAGE);
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();
        ServerConfig serverConfig = DTTConfig.getInstance().getServerConfig();
        if (serverWorld.getTimeOfDay() % 24000 == 0) {
            // 每天0时更新统计数据
            double EMA_Factor = serverConfig.diaryConfig.EMAFactor;
            DailyStatManager.updateDailyStat(self.getUuid(), EMA_Factor);
        }
        if (dtt$isEyesClosed && self.age % 20 == 0 && serverConfig.combatConfig.saferCatatonicStupor) {
            // 缓慢 + 挖掘疲劳 + 虚弱
            self.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    30, 4
            ));
            self.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.MINING_FATIGUE,
                    30, 4
            ));
            self.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.WEAKNESS,
                    30, 4
            ));
        }
    }

    @Inject(
            method = "increaseStat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/stat/ServerStatHandler;increaseStat(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/stat/Stat;I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void increaseDailyStat(Stat<?> stat, int amount, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        if (stat.getType() == Stats.KILLED && stat.getValue().equals(EntityType.ENDER_DRAGON)) {
            DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.ENDER_DRAGON_KILLED);
        }

        if (stat.getType() == Stats.CUSTOM) {
            Identifier statId = (Identifier) stat.getValue();

            if (statId.equals(Stats.WALK_ONE_CM)) {
                DailyStatManager.getTodayStat(self.getUuid()).increaseNumberStat(DTTDailyStat.DISTANCE_MOVED, amount);
            }
            if (statId.equals(Stats.TRADED_WITH_VILLAGER)) {
                DailyStatManager.getTodayStat(self.getUuid()).increaseNumberStat(DTTDailyStat.TRADED_COUNT, amount);
            }
            if (statId.equals(Stats.POT_FLOWER)) {
                DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.FLOWER_POTTED);
            }
            if (statId.equals(Stats.RAID_WIN)) {
                DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.RAID_WON);
            }
            if (statId.equals(Stats.DAMAGE_TAKEN)) {
                DailyStatManager.getTodayStat(self.getUuid()).increaseNumberStat(DTTDailyStat.DAMAGE_TAKEN, amount);
            }
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
            DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.RAINED);
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
            DailyStatManager.getTodayStat(self.getUuid()).setTrueStat(DTTDailyStat.RAINED);
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        nbt.putBoolean("dtt$isEyesClosed", this.dtt$isEyesClosed);

        DailyStatManager.writeToNbt(self.getUuid(), nbt);

        TopicManager.writeToNbt(self.getUuid(), nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        this.dtt$isEyesClosed = nbt.getBoolean("dtt$isEyesClosed");

        DailyStatManager.readFromNbt(self.getUuid(), nbt);

        TopicManager.readFromNbt(self.getUuid(), nbt);
    }
}
