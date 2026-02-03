package grainalcohol.dtt.mental;

import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * <h1>EmotionHelper</h1>
 * 情绪值（Emotion Value）是反映玩家当前情绪状态的数值。<br>
 * 此类是用于处理和判断与情绪值相关内容的工具类。<br>
 * <br>
 * <h2>范围和划分：</h2>
 * 情绪值是取值范围为[-20.0, 20.0]的浮点数，其约定边界为-20和20，但实际上可以突破该边界。<br>
 * <ul>
 *     <li>高未定义：(20, +∞)</li>
 *     <li>激动：(12, 20]</li>
 *     <li>喜悦：(6, 12]</li>
 *     <li>略喜悦：(2, 6]</li>
 *     <li>平静：[-2, 2]</li>
 *     <li>略悲伤：[-6, -2)</li>
 *     <li>悲伤：[-12, -6)</li>
 *     <li>绝望：[-20, -12)</li>
 *     <li>低未定义：(-∞, -20)</li>
 * </ul>
 * <br>
 * <h2>表现：</h2>
 * 情绪值会以小黄豆的不同表情的形式实时现实在玩家的物品栏正上方（可以调整位置）。<br>
 * 小黄豆的表情根据情绪值的等级变化，并且同一等级时根据玩家是否处于战斗状态存在两个小黄豆表情。<br>
 * <br>
 * <h2>机制：</h2>
 * <h4>情绪值与精神健康值的关系</h4>
 * 情绪值越高，精神健康值恢复越快，反之亦然。
 * 具体数据为{@code emotionValue * 0.01}每秒，因此情绪值为负时表现为扣除精神健康值。<br>
 * 另外精神健康值也会反过来影响情绪值对其的恢复速度。<br>
 * <h4>影响情绪值的东西</h4>
 * 在受到伤害、宠物死亡、失眠或死亡时都会立即扣除一些。<br>
 * 特别的：触发了PTSD时会在一定时间内持续扣除情绪值、被吸收的伤害不会降低情绪值。<br>
 * 在睡眠、击杀怪物、打开战利品箱等行为时会立即增加一些。<br>
 * 特别的：由抑郁相转为躁狂相时会立即增加一些、在唱片机附近听音乐也会增加一些。<br>
 * <h4>情绪值影响的东西</h4>
 * 情绪值的高低会影响玩家的基础属性，如移动速度、挖掘速度等，反之亦然。<br>
 * <br>
 * <h2>战斗状态和禅定状态：</h2>
 * <h4>战斗状态（Combat State）</h4>
 * 情绪值在-2~20范围内时，玩家可以通过攻击任意实体进入战斗状态；在2~-20范围内时，玩家可以通过被任意实体攻击进入战斗状态。<br>
 * 战斗状态下，因情绪值较低导致的移动速度降低效果会被取消。<br>
 * <h4>禅定状态（Zen State）</h4>
 * 在与Boss战斗或袭击事件中，玩家会进入禅定状态。<br>
 * 禅定状态下，受伤不会扣除情绪值，也不会触发PTSD，但死亡惩罚更严重。<br>
 * <br>
 * 注：上述内容基于depression原版的源代码分析和MC百科攻略总结，不一定绝对准确，如有侵权请联系删除。
 * @author grainalcohol
 * @since 2026-02-03
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
