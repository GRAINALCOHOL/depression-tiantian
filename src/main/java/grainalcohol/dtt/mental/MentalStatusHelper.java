package grainalcohol.dtt.mental;

import grainalcohol.dtt.api.internal.EyesStatusFlagController;
import grainalcohol.dtt.init.DTTStatusEffect;
import grainalcohol.dtt.util.MathUtil;
import net.depression.client.ClientMentalIllness;
import net.depression.client.DepressionClient;
import net.depression.mental.MentalIllness;
import net.depression.mental.MentalStatus;
import net.depression.mental.PTSDManager;
import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Random;

/**
 * <h1>MentalStatusHelper</h1>
 * 精神健康状态（{@linkplain MentalStatus Mental Status}）是储存与玩家精神健康相关数据的类，
 * 但也包含了情绪值、PTSD信息、患病情况、精神特质的存储、管理和逻辑。<br>
 * 其中：<br>
 * <ul>
 *     <li>精神健康值（{@linkplain MentalStatus#mentalHealthValue Mental Health Value}）是反映玩家精神健康程度的数据。</li>
 *     <li>精神疾病状态（{@linkplain MentalIllness Mental Illness Status}）是存储和处理患病情况和相关行为的类。</li>
 *     <li>{@linkplain PTSDManager PTSDManager}是管理PTSD和幻觉的消散和触发的类。</li>
 * </ul>
 * <br>
 * 此类是用于处理和判断与精神状态相关内容的的工具类。<br>
 * <br>
 * <h2>精神健康值（Mental Health Value）：</h2>
 * <h4>范围和划分</h4>
 * 精神健康值是取值范围为[0.0, 100.0]的浮点数：<br>
 * <ul>
 *     <li>健康：[70, 100]</li>
 *     <li>轻度：[40, 70)</li>
 *     <li>中度：[20, 40)</li>
 *     <li>重度：[0, 20)</li>
 * </ul>
 * <h4>机制</h4>
 * 精神健康值的变化与两大影响因素有关，分别是情绪值和药物干预。<br>
 * 精神健康值越高，情绪值对其的恢复速度越快，反之亦然。<br>
 * 特别的：当精神健康值归零或接近于0时，情绪值将几乎无法恢复精神健康值，因此需要药物介入才有恢复的可能。<br>
 * <br>
 * 精神健康值降低到一定程度时会患上对应严重程度的精神疾病，反之，恢复到一定程度就会从疾病中痊愈。<br>
 * 精神健康值较低（或者说患精神疾病）时会影响玩家的基础属性，如移动速度、挖掘速度等。<br>
 * <br>
 * <h2>精神特质（Mental Trail）：</h2>
 * <b>---WIP---</b>
 * <br>
 * <h2>精神疾病（Mental Illness）：</h2>
 * <h4>机制</h4>
 * 玩家根据精神健康值的高低会患上不同严重程度的精神疾病，其中当精神健康值降低到重度等级时会随机罹患重度抑郁障碍或双相情感障碍。<br>
 * 患病情况恶化和痊愈都有对应的文案提示，包括与健康状态的转换。<br>
 * 吐槽：depression只做了不同严重程度的抑郁症和双相情感障碍，并且扩展性极差，很难或几乎不可能直接在其基础上进行扩展。<br>
 * <h4>抑郁障碍（Depressive Disorder）</h4>
 * 包括轻度抑郁（Mild Depression）、中度抑郁（Moderate Depression）和重度抑郁（Major Depressive Disorder）。<br>
 * 抑郁症的症状表现包括降低玩家基础属性、进食困难、疲惫、失眠等。<br>
 * 其中：重度抑郁还会引起紧张性木僵（Catatonic Stupor）状态，即闭眼且无法行动。<br>
 * <h4>双相情感障碍（Bipolar Disorder）</h4>
 * 躁狂相期间情绪暴涨，但依旧会进食困难和失眠；抑郁相期间负面效果比重度抑郁更严重。<br>
 * <br>
 * <h2>躯体化症状（Somatization Symptoms）：</h2>
 * <h4>精神疲劳（Mental Fatigue）</h4>
 * 玩家在身患抑郁症时，打破/放置方块、做出攻击行为时都有可能触发精神疲劳，
 * 该效果对游戏没有实际影响，仅是在<a href="https://minecraft.wiki/w/Action_bar">动作栏（Actionbar）</a>的位置显示一行文字。<br>
 * <br>
 * <h2>创伤后应激障碍（PTSD，Post-Traumatic Stress Disorder）：</h2>
 * <h4>机制</h4>
 * 在PTSD的机制中存在一个浮点数值，姑且可以称呼为“创伤值”。<br>
 * 创伤值的范围为[0.0, 40.0]，创伤值在不同范围内属于不同创伤等级，分别对应PTSD的五个等级（0~4）：<br>
 * <ul>
 *     <li>极低：[0, 20.0]</li>
 *     <li>低：(20.0, 26.0]</li>
 *     <li>中：(26.0, 32.0]</li>
 *     <li>高：(32.0, 36.0]</li>
 *     <li>极高：(36.0, 40.0]</li>
 * </ul>
 * 其中0级不会有任何症状且缓解速度较快，当其加重到1级后会出现喘息声和心跳声、加重到2级后会追加耳鸣声、
 * 加重到3级会追加幻听、达到4级则会追加幻视。<br>
 * <h4>形成</h4>
 * 一次性受到某伤害源造成的大量伤害 或 长时间受到相同伤害源的伤害时，有概率对该伤害源形成PTSD。<br>
 * 伤害源不限于生物，还包括一些事件（如摔伤、火焰等，甚至是{@code /Kill}指令）。<br>
 * <h4>触发</h4>
 * 触发PTSD的方式有多种，以下我们称呼使PTSD形成的实体或事件为“触发源”：<br>
 * <ul>
 *     <li>受到来自对应触发源的伤害时</li>
 *     <li>听到有关对应触发源的声音时</li>
 *     <li>看到对应触发源的实体</li>
 * </ul>
 * 特别的：PTSD触发期间会持续扣除情绪值，并且被对应伤害源实体攻击时扣除的情绪值会变得更多。<br>
 * <h4>消散</h4>
 * PTSD可以随时间缓慢恢复（每tick恢复），其中0级恢复速度较快。<br>
 * 特别的：击杀对应的触发源实体会立刻恢复一定创伤值，安全地接触触发源（触发PTSD但未受到伤害）可以加快恢复速度（恢复速度逐渐变快）。<br>
 * <br>
 * <h2>药物和过量服药（OD，Overdoes）：</h2>
 * <h4>药物</h4>
 * 药物根据作用可以分为以下几种：<br>
 * <ul>
 *     <li>抗抑郁：用于治疗抑郁障碍，可以缓慢恢复精神健康值。</li>
 *     <li>抗躁狂：用于治疗双相情感障碍，本身无效果，与抗抑郁类药物联合使用时可以缓慢恢复精神健康值。<br>
 *     特别的：同时满足患双相情感障碍、联合用药和抗躁狂状态效果等级大于等于3级（等级倍率大于等于2）三个条件时，
 *     可以大幅降低情绪值对精神健康值的伤害。</li>
 *     <li>安眠药：用于缓解失眠症状，可以使玩家正常入睡。</li>
 * </ul>
 * <h4>过量服药</h4>
 * 在抗抑郁或抗躁狂状态效果期间再次服用对应药物将会触发OD事件。<br>
 * 第一次额外服用没有任何效果，只有提示信息。<br>
 * 第二次及以上额外服用会给予一定负面效果，其强度与OD的次数正相关。<br>
 * <br>
 * <h2>心理医生（村民职业，Psychiatrist）：</h2>
 * <h4>职业</h4>
 * 心理医生会出现在村庄中的心理诊所中，该诊所会自然生成在村庄中。<br>
 * 心理医生的工作站点方块是depression模组添加的{@linkplain net.depression.block.ModBlocks#COMPUTER 电脑方块}。<br>
 * <h4>交易</h4>
 * 心理医生的商品包括精神量表和各种精神类药品，新手心理医生只会售卖精神健康量表和轻度抗抑郁片剂，提升等级可以使其售卖更多药品。<br>
 * 其售卖的商品价格不受供需关系影响。<br>
 * <br>
 * <h2>日记和精神量表（Diary & Mental Health Scale）：</h2>
 * <h4>日记</h4>
 * 玩家可以通过使用“书与笔”和任意一朵花无序合成一本日记。
 * 当日落之后玩家即可使用日记生成对应精神健康状态的日记内容。<br>
 * 日记可以保存最多50页文字，超出的部分会被丢掉，也就是文案不会完整显示。<br>
 * 注：使用{@code /time set}命令修改时间后使用日记可能导致游戏崩溃。<br>
 * <h4>精神量表</h4>
 * 精神量表可以通过与心理医生交易获得。<br>
 * 使用其不会被消耗，但有10秒冷却时间。<br>
 * 使用后会在聊天栏显示玩家当前的精神健康值（保留两位小数）和患病情况。<br>
 * <br>
 * 注：上述内容基于depression原版的源代码分析和MC百科攻略总结，不一定绝对准确，如有侵权请联系删除。
 * @author grainalcohol
 * @since 2026-02-03
 * @see EmotionHelper 查看情绪值与精神健康值的关系
 * @see MentalHealthStatus
 * @see MentalIllnessStatus
 * @see Severity
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
