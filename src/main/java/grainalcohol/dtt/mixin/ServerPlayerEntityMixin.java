package grainalcohol.dtt.mixin;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.api.internal.PendingMessageQueueController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.mental.EmotionHelper;
import grainalcohol.dtt.mental.MentalStatusHelper;
import grainalcohol.dtt.util.NearbyMentalHealHelper;
import grainalcohol.dtt.util.StringUtil;
import net.depression.mental.MentalStatus;
import net.depression.network.ActionbarHintPacket;
import net.minecraft.block.entity.JukeboxBlockEntity;
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

import java.util.LinkedList;
import java.util.Queue;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements EyesStatusFlagController, PendingMessageQueueController {
    // 服务端标记
    @Unique private boolean dtt$isEyesClosed = false;

    // 每个游戏日最多生成两次消息
    @Unique private boolean dtt$hasSendInRainMessage = false;
    @Unique private boolean dtt$hasSendResetSpawnPointMessage = false;
    @Unique private boolean dtt$hasSendJukeboxHealMessage = false;

    // 用于避免文案触发频繁导致的覆盖问题
    @Unique private final Queue<Text> dtt$pendingMessageQueue = new LinkedList<>();

    @Override
    public boolean dtt$getIsEyesClosedFlag() {
        return this.dtt$isEyesClosed;
    }

    @Override
    public void dtt$setIsEyesClosedFlag(boolean isClosed) {
        this.dtt$isEyesClosed = isClosed;
    }

    @Override
    public void dtt$addPendingMessage(Text message) {
        this.dtt$pendingMessageQueue.add(message);
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
                serverWorld.getServer().execute(() -> {
                    if (!dtt$hasSendResetSpawnPointMessage) {
                        EmotionHelper.mentalHeal(self, "reset_spawn_point", 2.0);
                        // 我觉得这个不应该避免覆盖，这个比较好看
                        self.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(
                                "message.dtt.reset_spawn_point", 3
                        )), true);
                        dtt$hasSendResetSpawnPointMessage = true;
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld serverWorld = self.getServerWorld();

        if (!dtt$pendingMessageQueue.isEmpty() && self.age % 200 == 0) {
            // 每10秒pull剩余的待发送消息
            self.sendMessage(dtt$pendingMessageQueue.poll(), true);
        }

        if (!dtt$hasSendInRainMessage && self.age % 100 == 0 && serverWorld.hasRain(self.getBlockPos())) {
            // 每5秒 淋到雨时
            EmotionHelper.mentalHurt(self, 2.0);
            dtt$pendingMessageQueue.add(Text.translatable(StringUtil.findTranslationKeyVariant(
                    "message.dtt.in_rain", 3
            )));
            dtt$hasSendInRainMessage = true;
        }

        ServerConfig.MentalHealConfig mentalHealConfig = DTTConfig.getInstance().getServerConfig().mentalHealConfig;
        MentalStatus mentalStatus = MentalStatusHelper.getMentalStatus(self);
        // 宠物恢复情绪
        if (mentalHealConfig.nearby_pet_mode == ServerConfig.NearbyAnythingHealMode.EXIST) {
            // exist模式
            if (self.age % mentalHealConfig.nearby_pet_interval_ticks == 0
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
        if (mentalHealConfig.nearby_jukebox_mode == ServerConfig.NearbyAnythingHealMode.EXIST) {
            // exist模式
            JukeboxBlockEntity nearestPlayingJukebox = NearbyMentalHealHelper.findNearestPlayingJukeboxEntity(self, 4);
            if (self.age % mentalHealConfig.nearby_jukebox_interval_ticks == 0 && nearestPlayingJukebox != null) {
                // 每隔一段时间，并且附近存在正在播放的唱片机时
                Identifier recordItemId = Registries.ITEM.getId(nearestPlayingJukebox.getStack().getItem());
                double healValue = mentalStatus.mentalHeal(recordItemId.toString(), 1.0);
                if (!dtt$hasSendJukeboxHealMessage && healValue > 0.5) {
                    dtt$pendingMessageQueue.add(Text.translatable(StringUtil.findTranslationKeyVariant(
                            "message.dtt.nearby_jukebox", 3
                    )));
                    dtt$hasSendJukeboxHealMessage = true;
                }
            }
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
            dtt$hasSendInRainMessage = false;
            dtt$hasSendResetSpawnPointMessage = false;
            dtt$hasSendJukeboxHealMessage = false;
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
        if (stat.getType() == Stats.CUSTOM) {
            Identifier statId = (Identifier) stat.getValue();

            if (statId.equals(Stats.WALK_ONE_CM)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).increaseDistanceMoved(amount);
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
            if (statId.equals(Stats.DAMAGE_TAKEN)) {
                DailyStatManager.getTodayDailyStat(self.getUuid()).increaseDamageTaken(amount);
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

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        nbt.putBoolean("dtt$hasCheckInRain", this.dtt$hasSendInRainMessage);
        nbt.putBoolean("dtt$hasResetSpawnPoint", this.dtt$hasSendResetSpawnPointMessage);

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

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        this.dtt$hasSendInRainMessage = nbt.getBoolean("dtt$hasCheckInRain");
        this.dtt$hasSendResetSpawnPointMessage = nbt.getBoolean("dtt$hasResetSpawnPoint");

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
