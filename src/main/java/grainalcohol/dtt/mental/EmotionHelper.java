package grainalcohol.dtt.mental;

import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * <h1>EmotionHelper</h1>
 * 情绪值（Emotion Value）是反映玩家当前情绪状态的数值。<br>
 * 此类是用于处理和判断与情绪值相关内容的工具类。<br>
 * @author grainalcohol
 * @since 2026-02-04
 * @see grainalcohol.dtt.api.event.EmotionEvent
 */
public class EmotionHelper {
    public static final double MAX_EMOTION_VALUE = 20.0;
    public static final double MIN_EMOTION_VALUE = -20.0;

    public static final double BETWEEN_EXCITED_PLEASED = 12.0;
    public static final double BETWEEN_PLEASED_SLIGHTLY_PLEASED = 6.0;
    public static final double BETWEEN_SLIGHTLY_PLEASED_CALM = 2.0;
    public static final double BETWEEN_CALM_SLIGHTLY_SAD = -2.0;
    public static final double BETWEEN_SLIGHTLY_SAD_SAD = -6.0;
    public static final double BETWEEN_SAD_DESPAIR = -12.0;

    /**
     * 获取玩家的情绪值
     * @param serverPlayerEntity 需要获取情绪值的玩家
     * @return 玩家的情绪值
     */
    public static double getEmotionValue(ServerPlayerEntity serverPlayerEntity) {
        return MentalStatusHelper.getMentalStatus(serverPlayerEntity).emotionValue;
    }

    /**
     * 判断玩家是否处于战斗状态<br>
     * 注：这不是通用的战斗状态判断，仅用于depression的战斗状态判断
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否处于战斗状态
     */
    public static boolean isInCombatState(ServerPlayerEntity serverPlayerEntity) {
        return MentalStatusHelper.getMentalStatus(serverPlayerEntity).combatCountdown > 0;
    }

    /**
     * 判断玩家是否处于禅定状态，即处于boss战或袭击事件中
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否处于禅定状态
     */
    public static boolean isInZenState(ServerPlayerEntity serverPlayerEntity) {
        return !Registry.playerEventMap.get(serverPlayerEntity.getUuid()).isEmpty();
    }

    /**
     * 根据情绪值获取对应的情绪等级。<br>
     * 虽然但是我还是想吐槽depression设计这里的取值范围时为什么不能让区间方向统一一点
     * @param emotionValue 情绪值
     * @return 对应的情绪等级
     */
    public static EmotionLevel getEmotionLevel(double emotionValue) {
        if (emotionValue > MAX_EMOTION_VALUE) {
            return EmotionLevel.HIGH_UNDEFINED;
        } else if (emotionValue > BETWEEN_EXCITED_PLEASED) {
            return EmotionLevel.EXCITED;
        } else if (emotionValue > BETWEEN_PLEASED_SLIGHTLY_PLEASED) {
            return EmotionLevel.PLEASED;
        } else if (emotionValue > BETWEEN_SLIGHTLY_PLEASED_CALM) {
            return EmotionLevel.SLIGHTLY_PLEASED;
        } else if (emotionValue >= BETWEEN_CALM_SLIGHTLY_SAD) {
            return EmotionLevel.CALM;
        } else if (emotionValue >= BETWEEN_SLIGHTLY_SAD_SAD) {
            return EmotionLevel.SLIGHTLY_SAD;
        } else if (emotionValue >= BETWEEN_SAD_DESPAIR) {
            return EmotionLevel.SAD;
        } else if (emotionValue >= MIN_EMOTION_VALUE) {
            return EmotionLevel.DESPAIR;
        } else {
            return EmotionLevel.LOW_UNDEFINED;
        }
    }
}
