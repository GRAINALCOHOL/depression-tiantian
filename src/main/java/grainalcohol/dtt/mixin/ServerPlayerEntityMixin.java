package grainalcohol.dtt.mixin;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.api.internal.PendingMessageQueueController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.topic.v2.TopicManager;
import grainalcohol.dtt.init.DTTDailyStat;
import grainalcohol.dtt.api.helper.EmotionHelper;
import grainalcohol.dtt.api.helper.MentalStatusHelper;
import grainalcohol.dtt.util.NearbyMentalHealHelper;
import grainalcohol.dtt.util.StringUtil;
import net.depression.mental.MentalStatus;
import net.depression.network.ActionbarHintPacket;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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

    // 黑暗消息要在离开黑暗环境后重置
    @Unique private static final int dtt$darknessMessageMaxTime = 10; // 200秒
    @Unique private int dtt$darknessMessageTimer = 0;
    @Unique private boolean dtt$hasSendDarknessMessage = false;

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

        // TODO: 发送消息的逻辑也太复杂了，考虑做个系统单独管理

        // 黑暗环境中
        if (self.age % 20 == 0) {
            ServerConfig.CommonConfig commonConfig = DTTConfig.getInstance().getServerConfig().commonConfig;
            int maxSeconds = commonConfig.darknessMessageTriggerSeconds;
            // getLightLevel会返回天空光和区块光的较大值
            int lightLevelThreshold = MathHelper.clamp(commonConfig.darknessMessageLightLevelThreshold, 0, 15);
            boolean inDarkness = serverWorld.getLightLevel(self.getBlockPos()) <= lightLevelThreshold;

            if (inDarkness) {
                if (dtt$darknessMessageTimer < maxSeconds) {
                    dtt$darknessMessageTimer++;
                }
            } else {
                if (dtt$darknessMessageTimer > 0) {
                    dtt$darknessMessageTimer--;
                }

                if (dtt$darknessMessageTimer <= 0) {
                    // 这里有一个重复赋值的问题
                    dtt$hasSendDarknessMessage = false;
                }
            }

            // 发送消息
            if (dtt$darknessMessageTimer >= maxSeconds && !dtt$hasSendDarknessMessage) {
                EmotionHelper.mentalHurt(self, 8.0);
                self.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(
                        "message.dtt.darkness", 3
                )), true);
                dtt$hasSendDarknessMessage = true;
            }
        }

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
        ServerConfig serverConfig = DTTConfig.getInstance().getServerConfig();
        if (serverWorld.getTimeOfDay() % 24000 == 0) {
            // 每天0时更新统计数据
            double EMA_Factor = serverConfig.diaryConfig.EMAFactor;
            DailyStatManager.updateDailyStat(self.getUuid(), EMA_Factor);
        }
        if (serverWorld.getTimeOfDay() % 12000 == 0) {
            // 每天更新两次标记
            dtt$hasSendInRainMessage = false;
            dtt$hasSendResetSpawnPointMessage = false;
            dtt$hasSendJukeboxHealMessage = false;
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

        nbt.putBoolean("dtt$hasCheckInRain", this.dtt$hasSendInRainMessage);
        nbt.putBoolean("dtt$hasResetSpawnPoint", this.dtt$hasSendResetSpawnPointMessage);
        nbt.putBoolean("dtt$hasSendJukeboxHealMessage", this.dtt$hasSendJukeboxHealMessage);

        nbt.putInt("dtt$darknessMessageTimer", this.dtt$darknessMessageTimer);
        nbt.putBoolean("dtt$hasSendDarknessMessage", this.dtt$hasSendDarknessMessage);

        NbtList pendingMessagesNbt = new NbtList();
        for (Text message : this.dtt$pendingMessageQueue) {
            // 不对啊，不能存结果，应该存translation key
            pendingMessagesNbt.add(NbtString.of(message.getString()));
        }
        nbt.put("dtt$pendingMessageQueue", pendingMessagesNbt);

        DailyStatManager.writeToNbt(self.getUuid(), nbt);

        TopicManager.writeToNbt(self.getUuid(), nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        this.dtt$isEyesClosed = nbt.getBoolean("dtt$isEyesClosed");

        this.dtt$hasSendInRainMessage = nbt.getBoolean("dtt$hasCheckInRain");
        this.dtt$hasSendResetSpawnPointMessage = nbt.getBoolean("dtt$hasResetSpawnPoint");
        this.dtt$hasSendJukeboxHealMessage = nbt.getBoolean("dtt$hasSendJukeboxHealMessage");

        this.dtt$darknessMessageTimer = nbt.getInt("dtt$darknessMessageTimer");
        this.dtt$hasSendDarknessMessage = nbt.getBoolean("dtt$hasSendDarknessMessage");

        NbtList nbtList = nbt.getList("dtt$pendingMessageQueue", NbtElement.STRING_TYPE);
        for (NbtElement nbtElement : nbtList) {
            // 反正就是不应该用翻译结果
            this.dtt$pendingMessageQueue.add(Text.literal(nbtElement.asString()));
        }

        DailyStatManager.readFromNbt(self.getUuid(), nbt);

        TopicManager.readFromNbt(self.getUuid(), nbt);
    }
}
