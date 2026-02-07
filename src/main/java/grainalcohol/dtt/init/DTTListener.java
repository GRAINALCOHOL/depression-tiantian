package grainalcohol.dtt.init;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.SymptomEvent;
import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import net.minecraft.block.DeadBushBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class DTTListener {
    public static void archEventInit() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof Monster && source.getAttacker() instanceof ServerPlayerEntity player) {
                player.incrementStat(DTTStat.MONSTER_KILLED);
                DailyStatManager.getTodayDailyStat(player.getUuid()).increaseMonsterKilled(1);
            }
            if (entity instanceof PlayerEntity player) {
                DailyStatManager.getTodayDailyStat(player.getUuid()).setHasDead(true);
            }
            return EventResult.pass();
        });
        EntityEvent.ANIMAL_TAME.register(((animalEntity, playerEntity) -> {
            playerEntity.incrementStat(DTTStat.ANIMAL_TAMED);
            return EventResult.pass();
        }));
        InteractionEvent.RIGHT_CLICK_BLOCK.register(((player, hand, pos, face) -> {
            if (player.getWorld().getBlockState(pos).getBlock() instanceof FlowerPotBlock flowerPotBlock && !(flowerPotBlock.getContent() instanceof DeadBushBlock)) {
                DailyStatManager.getTodayDailyStat(player.getUuid()).setHasFlowerPotted(true);
            }
            return EventResult.pass();
        }));
    }
    public static void dttEventInit() {
        MentalIllnessEvent.MENTAL_HEALTH_CHANGED_EVENT.register((player, lastTickStatus, currentStatus) -> {
            // 患病情况恶化
            if (currentStatus.isSickerThan(lastTickStatus)) {
                if (currentStatus.isSeverelyIll()) {
                    // 恶化到严重程度
                    DailyStatManager.getTodayDailyStat(player.getUuid()).setHasWorsened(true);
                }
            }

            // 患病情况好转
            if (currentStatus.isHealthierThan(lastTickStatus)) {
                if (lastTickStatus.isSeverelyIll()) {
                    // 恢复到非严重程度
                    DailyStatManager.getTodayDailyStat(player.getUuid()).setHasCured(true);
                }

                if (currentStatus.isHealthy() && player.hasStatusEffect(DTTStatusEffect.ANOREXIA)) {
                    // 恢复到健康状态时移除厌食状态效果
                    player.removeStatusEffect(DTTStatusEffect.ANOREXIA);
                }
            }
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
}
