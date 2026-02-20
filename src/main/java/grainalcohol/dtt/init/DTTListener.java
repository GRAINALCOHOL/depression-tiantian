package grainalcohol.dtt.init;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.PTSDEvent;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.api.internal.PendingMessageQueueController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.mental.PTSDLevel;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
        PTSDEvent.PTSD_LEVEL_CHANGED_EVENT.register(((player, ptsdId, lastLevel, currentLevel) -> {
            if (currentLevel.isSickerThan(lastLevel)) {
                // PTSD等级提升
                sendPTSDFormMessage(player, ptsdId, currentLevel);
            }
            if (currentLevel.isHealthierThan(lastLevel)) {
                // PTSD等级降低
                sendPTSDDisperseMessage(player, ptsdId, currentLevel);
            }
        }));
        PTSDEvent.PTSD_REMISSION_EVENT.register((player, ptsdId, currentLevel) -> {
            sendPTSDRemissionMessage(player, ptsdId);
        });
    }
    public static void dttAPIEventInit() {
        SymptomEvent.CLOSE_EYES_EVENT.register((player, causedBySleepinessStatusEffect) -> {
            DTTMod.LOGGER.info("close eyes event triggered");
            if (player instanceof EyesStatusFlagController controller) {
                controller.dtt$setIsEyesClosedFlag(true);
            }
            return EventResult.pass();
        });
        SymptomEvent.OPEN_EYES_EVENT.register(player -> {
            DTTMod.LOGGER.info("open eyes event triggered");
            if (player instanceof EyesStatusFlagController controller) {
                controller.dtt$setIsEyesClosedFlag(false);
            }
        });
    }

    private static void sendPTSDFormMessage(PlayerEntity player, String ptsdId, PTSDLevel currentLevel) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhanced_ptsd_form_message) {
            return;
        }
        // PTSD等级提升至0级
        if (currentLevel.isLatent()) {
            ((PendingMessageQueueController) player).dtt$addPendingMessage(Text.translatable(
                    "message.dtt.ptsd_latent", ptsdId
            ));
        }
        // PTSD等级提升至1、2、3级
        if (currentLevel.hasSymptoms()) {
            ((PendingMessageQueueController) player).dtt$addPendingMessage(Text.translatable(
                    "message.dtt.ptsd_form", ptsdId
            ));
        }
        // PTSD等级提升至4级
        if (currentLevel.isExtreme()) {
            ((PendingMessageQueueController) player).dtt$addPendingMessage(Text.translatable(
                    "message.dtt.ptsd_extreme", ptsdId
            ));
        }
    }

    private static void sendPTSDDisperseMessage(PlayerEntity player, String ptsdId, PTSDLevel currentLevel) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhanced_ptsd_disperse_message) {
            return;
        }
        // PTSD等级降低至0级
        if (currentLevel.isLatent()) {
            ((PendingMessageQueueController) player).dtt$addPendingMessage(Text.translatable(
                    "message.dtt.ptsd_disperse", ptsdId
            ));
        }
    }

    private static void sendPTSDRemissionMessage(PlayerEntity player, String ptsdId) {
        if (!DTTConfig.getInstance().getClientConfig().messageDisplayConfig.enhanced_ptsd_remission_message) {
            return;
        }
        ((PendingMessageQueueController) player).dtt$addPendingMessage(Text.translatable(
                "message.dtt.ptsd_remission", ptsdId
        ));
    }
}
