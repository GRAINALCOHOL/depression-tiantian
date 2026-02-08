package grainalcohol.dtt.mental;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.init.DTTStatusEffect;
import grainalcohol.dtt.util.MathUtil;
import net.depression.client.ClientMentalIllness;
import net.depression.client.DepressionClient;
import net.depression.mental.MentalIllness;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Random;

/**
 * <h1>MentalStatusHelper</h1>
 * 精神状态（Mental Status）是反映玩家精神健康状态的对象。<br>
 * 此类是用于处理和判断与精神状态相关内容的工具类。<br>
 * <br>
 * bug提示：使用{@code /time set}命令修改时间后使用日记可能导致游戏崩溃。<br>
 * @author grainalcohol
 * @since 2026-02-04
 * @see grainalcohol.dtt.api.event.MentalHealthEvent
 * @see grainalcohol.dtt.api.event.PTSDEvent
 * @see grainalcohol.dtt.api.event.MentalIllnessEvent
 * @see grainalcohol.dtt.api.event.SymptomEvent
 */
public class MentalStatusHelper {
    private static final Random RANDOM = new Random();

    /**
     * 精神健康值的最小值
     */
    public static final double MIN_MENTAL_HEALTH_VALUE = 0.0;
    /**
     * 精神健康值的最大值
     */
    public static final double MAX_MENTAL_HEALTH_VALUE = 100.0;

    /**
     * 健康与轻度的分界线
     */
    public static final double BETWEEN_HEALTHY_AND_MILD = 70.0;
    /**
     * 轻度与中度的分界线
     */
    public static final double BETWEEN_MILD_AND_MODERATE = 40.0;
    /**
     * 中度与严重的分界线
     */
    public static final double BETWEEN_MODERATE_AND_MAJOR = 20.0;

    /**
     * 获取精神健康值对应的精神健康率，即当前值和最大值的比率<br>
     * @param mentalHealthValue 精神健康值
     * @return 精神健康率，范围0.0~1.0
     */
    public static double getMentalHealthRate(double mentalHealthValue) {
        if (mentalHealthValue < MIN_MENTAL_HEALTH_VALUE) {
            return 0.0;
        } else if (mentalHealthValue > MAX_MENTAL_HEALTH_VALUE) {
            return 1.0;
        } else {
            return (mentalHealthValue / MAX_MENTAL_HEALTH_VALUE);
        }
    }

    /**
     * 获取精神健康评估文本，用于精神健康量表回复内容<br>
     * @param mentalHealthStatus 玩家精神健康状态
     * @return 评估文本
     */
    public static Text getAssessmentText(MentalHealthStatus mentalHealthStatus) {
        return Text.translatable("mental.assessment.dtt." + mentalHealthStatus.getName());
    }

    /**
     * 判断是否应该触发厌食症状态，用于{@linkplain grainalcohol.dtt.mixin.ItemStackMixin 进食前}判断是否应该打断和阻止<br>
     * 如果想判断玩家是否处于厌食状态，请使用{@link DTTStatusEffect#ANOREXIA}
     * @param mentalStatus 玩家精神状态
     * @return 是否应该触发厌食症
     * @see #shouldTriggerAnorexia(MentalIllnessStatus, boolean)
     */
    public static boolean shouldTriggerAnorexia(MentalStatus mentalStatus) {
        return shouldTriggerAnorexia(mentalStatus, mentalStatus.isMania());
    }

    public static boolean shouldTriggerAnorexia(MentalStatus mentalStatus, boolean isMania) {
        return shouldTriggerAnorexia(MentalIllnessStatus.from(mentalStatus), isMania);
    }

    public static boolean shouldTriggerAnorexia(MentalIllnessStatus mentalIllnessStatus, boolean isMania) {
        return switch (mentalIllnessStatus) {
            case NONE,HEALTHY -> false;
            case MILD_DEPRESSION -> MathUtil.chance(RANDOM, 0.05);
            case MODERATE_DEPRESSION -> MathUtil.chance(RANDOM, 0.25);
            case MAJOR_DEPRESSIVE_DISORDER -> MathUtil.chance(RANDOM, 0.8);
            case BIPOLAR_DISORDER -> {
                if (isMania) {
                    yield true;
                } else {
                    yield MathUtil.chance(RANDOM, 0.8);
                }
            }
        };
    }

    /**
     * 获取玩家的MentalStatus对象
     * @param serverPlayerEntity 需要获取的玩家
     * @return 玩家的MentalStatus对象
     */
    public static MentalStatus getMentalStatus(ServerPlayerEntity serverPlayerEntity) {
        return Registry.mentalStatus.get(serverPlayerEntity.getUuid());
    }

    /**
     * 获取玩家的MentalIllness对象
     * @param serverPlayerEntity 需要获取的玩家
     * @return 玩家的MentalIllness对象
     */
    public static MentalIllness getMentalIllness(ServerPlayerEntity serverPlayerEntity) {
        return getMentalStatus(serverPlayerEntity).mentalIllness;
    }

    /**
     * 判断玩家是否处于紧张性木僵状态，即患重病时闭眼的情况<br>
     * 注：闭眼这件事是可能由安眠药（即{@linkplain net.depression.effect.SleepinessEffect sleepiness状态效果}）引起的，
     * 但是depression没有区分这种情况，也就是说只要在患重病的时候触发闭眼就是紧张性木僵
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否处于紧张性木僵状态
     */
    public static boolean isInCatatonicStuporStatus(ServerPlayerEntity serverPlayerEntity) {
        MentalStatus mentalStatus = Registry.mentalStatus.get(serverPlayerEntity.getUuid());
        return MentalIllnessStatus.from(mentalStatus).isSeverelyIll() && isEyesClosed(serverPlayerEntity);
    }

    /**
     * 获取客户端的眼睛状态标记，用于depression内部渲染特效
     * @return 玩家是否闭着眼
     * @see ClientMentalIllness#isCloseEye
     */
    public static boolean isEyesClosed() {
        return DepressionClient.clientMentalStatus.mentalIllness.isCloseEye;
    }

    /**
     * 获取服务端的眼睛状态标记，用于可能的服务端逻辑
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否闭着眼
     * @see EyesStatusFlagController
     */
    public static boolean isEyesClosed(ServerPlayerEntity serverPlayerEntity) {
        if (serverPlayerEntity instanceof EyesStatusFlagController controller) {
            return controller.dtt$getIsEyesClosedFlag();
        }
        return false;
    }

    /**
     * 判断玩家是否处于躁狂状态，即双相情感障碍的躁狂相
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否处于躁狂状态
     */
    public static boolean isMania(ServerPlayerEntity serverPlayerEntity) {
        return MentalHealthStatus.from(serverPlayerEntity).isMania();
    }

    /**
     * 判断玩家是否对非玩家事物有PTSD<br>
     * 如生物：僵尸、箭矢等<br>
     * 如事件：摔落、火焰等<br>
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对非玩家事物有PTSD
     */
    public static boolean hasPTSDForEvent(ServerPlayerEntity serverPlayerEntity) {
        return !getMentalStatus(serverPlayerEntity).PTSD.isEmpty();
    }

    /**
     * 判断玩家是否对任意玩家有PTSD
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对其它玩家有PTSD
     */
    public static boolean hasPTSDForPlayer(ServerPlayerEntity serverPlayerEntity) {
        return !getMentalStatus(serverPlayerEntity).playerPTSDSet.isEmpty();
    }

    /**
     * 判断玩家是否对任何事物有PTSD
     * @param serverPlayerEntity 需要判断的玩家
     * @return 玩家是否对任何事物有PTSD
     */
    public static boolean hasPTSDForAnything(ServerPlayerEntity serverPlayerEntity) {
        return hasPTSDForEvent(serverPlayerEntity) || hasPTSDForPlayer(serverPlayerEntity);
    }

    public static boolean isSeverelyIll(MentalStatus mentalStatus) {
        return isSeverelyIll(mentalStatus.getMentalHealthId());
    }

    public static boolean isSeverelyIll(int mentalHealthId) {
        return MentalIllnessStatus.from(mentalHealthId).isSeverelyIll();
    }
}
