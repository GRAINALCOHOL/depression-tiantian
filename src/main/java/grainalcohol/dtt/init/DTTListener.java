package grainalcohol.dtt.init;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.PTSDEvent;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.api.helper.MentalStatusHelper;
import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.api.wrapper.PTSDLevel;
import grainalcohol.dtt.hint.HintMessageManager;
import grainalcohol.dtt.network.ServerConfigPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DTTListener {
    public static void archEventInit() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            // 宠物死亡
            if (entity instanceof TameableEntity tameable
                    && tameable.isTamed()
                    && tameable.getOwner() instanceof ServerPlayerEntity owner
            ) {
                DailyStatManager.getTodayStat(owner.getUuid()).setTrueStat(DTTDailyStat.PET_DIED);
            }
            // 击杀怪物
            if (entity instanceof Monster
                    && source.getAttacker() instanceof ServerPlayerEntity player
            ) {
                player.incrementStat(DTTStat.MONSTER_KILLED);
                DailyStatManager.getTodayStat(player.getUuid()).incrementNumberStat(DTTDailyStat.MONSTER_KILLED);
            }
            // 玩家死亡
            if (entity instanceof PlayerEntity player) {
                DailyStatManager.getTodayStat(player.getUuid()).setTrueStat(DTTDailyStat.DIED);
            }
            return EventResult.pass();
        });
        EntityEvent.ANIMAL_TAME.register(((animalEntity, playerEntity) -> {
            playerEntity.incrementStat(DTTStat.ANIMAL_TAMED);
            return EventResult.pass();
        }));
    }
    public static void dttEventInit() {
        MentalIllnessEvent.MENTAL_HEALTH_CHANGED_EVENT.register((player, lastTickStatus, currentStatus) -> {
            // 患病情况恶化
            if (currentStatus.isSickerThan(lastTickStatus)) {
                if (currentStatus.isSeverelyIll()) {
                    // 恶化到严重程度
                    DailyStatManager.getTodayStat(player.getUuid()).setTrueStat(DTTDailyStat.CURED);
                }
            }

            // 患病情况好转
            if (currentStatus.isHealthierThan(lastTickStatus)) {
                if (lastTickStatus.isSeverelyIll()) {
                    // 恢复到非严重程度
                    DailyStatManager.getTodayStat(player.getUuid()).setTrueStat(DTTDailyStat.WORSENED);
                }

                if (currentStatus.isHealthy() && player.hasStatusEffect(DTTStatusEffect.ANOREXIA)) {
                    // 恢复到健康状态时移除厌食状态效果
                    player.removeStatusEffect(DTTStatusEffect.ANOREXIA);
                }
            }
        });
        PTSDEvent.PTSD_LEVEL_CHANGED_EVENT.register(((player, context, lastLevel, currentLevel) -> {
            if (currentLevel.isSickerThan(lastLevel)) {
                // PTSD等级提升
                onPTSDLevelUp(player, context.getPtsdInfo(), currentLevel);
            }
        }));
        PTSDEvent.PTSD_DISPERSE_EVENT.register((player, context) -> {
            onPTSDDisperse(player, context.getPtsdInfo());
        });
        PTSDEvent.PTSD_REMISSION_EVENT.register((player, context, currentLevel) -> {
            onPTSDRemission(player, context.getPtsdInfo());
        });
        SymptomEvent.CLOSE_EYES_EVENT.register((player, causedBySleepinessStatusEffect) -> {
            if (!causedBySleepinessStatusEffect &&
                    MentalStatusHelper.isInCatatonicStuporStatus(player) &&
                    DTTConfig.getInstance().getServerConfig().combatConfig.saferCatatonicStupor
            ) {
                // 缓慢 + 挖掘疲劳 + 虚弱
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS,
                        30, 4
                ));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.MINING_FATIGUE,
                        30, 4
                ));
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS,
                        30, 4
                ));
            }
            return EventResult.pass();
        });
    }

    public static void dttInternalEventInit() {
        SymptomEvent.CLOSE_EYES_EVENT.register((player, causedBySleepinessStatusEffect) -> {
            if (player instanceof EyesStatusFlagController controller) {
                controller.dtt$setIsEyesClosedFlag(true);
            }
            return EventResult.pass();
        });
        SymptomEvent.OPEN_EYES_EVENT.register(player -> {
            if (player instanceof EyesStatusFlagController controller) {
                controller.dtt$setIsEyesClosedFlag(false);
            }
        });
    }
    public static void fabricEventInit() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            ServerConfig serverConfig = DTTConfig.getInstance().getServerConfig();

            DTTNetwork.CHANNEL.sendToPlayer(player, new ServerConfigPacket(
                    serverConfig.combatConfig.saferCatatonicStupor,
                    serverConfig.commonConfig.disableMentalTraitSelectScreen
            ));

//            HintMessageManager.init(player);
            HintMessageManager.onLogin(player);
        });
    }

    public static void onPTSDLevelUp(ServerPlayerEntity player, String ptsdInfo, PTSDLevel currentLevel) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhancedPTSDFormationMessage) {
            return;
        }
        // PTSD等级提升至0级
        if (currentLevel.isLatent()) {
            DTTHintMessage.LATENT_PTSD_MESSAGE.trigger(player, ptsdInfo);
        }
        // PTSD等级提升至1、2、3级
        if (currentLevel.hasSymptoms()) {
            DTTHintMessage.FORM_PTSD_MESSAGE.trigger(player, ptsdInfo);
        }
        // PTSD等级提升至4级
        if (currentLevel.isExtreme()) {
            DTTHintMessage.EXTREME_PTSD_MESSAGE.trigger(player, ptsdInfo);
        }
    }

    private static void onPTSDDisperse(ServerPlayerEntity player, String ptsdInfo) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhancedPTSDDispersalMessage) {
            return;
        }
        DTTHintMessage.DISPERSE_PTSD_MESSAGE.trigger(player, ptsdInfo);
    }

    private static void onPTSDRemission(ServerPlayerEntity player, String ptsdInfo) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhancedPTSDRemissionMessage) {
            return;
        }
        DTTHintMessage.REMISSION_PTSD_MESSAGE.trigger(player, ptsdInfo);
    }
}
