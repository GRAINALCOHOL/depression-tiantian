package grainalcohol.dtt.mental;

import net.depression.mental.MentalIllness;
import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 当前发作相，或者俗称“精神健康状态”，根据当前的表现分类，而不是患病情况。<br>
 * 患病情况请查看{@link MentalIllnessStatus}。<br>
 * <br>
 * 其中重度抑郁和（患双相时的）抑郁相同属于重度抑郁状态（MAJOR_DEPRESSION）。<br>
 * 包括健康、轻度抑郁、中度抑郁、重度抑郁/抑郁相和躁狂相状态。<br>
 */
public enum MentalHealthStatus {
    HEALTHY(0, "healthy", 0.0), // 健康
    MILD_DEPRESSION(1, "mild_depression", 0.1), // 轻度抑郁
    MODERATE_DEPRESSION(2, "moderate_depression", 0.2), // 中度抑郁
    MAJOR_DEPRESSION(3, "major_depression", 0.4), // 重度抑郁或抑郁相
    MANIC_PHASE(3, "manic_phase", 0.4), // 躁狂相
    NONE(-1, "none", 0.0)
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(MentalHealthStatus.class);
    private final int healthyLevel;
    private final String name;
    private final double feelingInfluenceMultiplier;

    MentalHealthStatus(int healthyLevel, String name, double feelingInfluenceMultiplier) {
        this.healthyLevel = healthyLevel;
        this.name = name;
        this.feelingInfluenceMultiplier = feelingInfluenceMultiplier;
    }

    public int getHealthyLevel() {
        return healthyLevel;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据depression的魔法值处理逻辑，可以总结出以下结论，并由此包装枚举：<br>
     * <br>
     * 1. mentalHealthId取值为[0, 4]，表示具体的病理名称，分别对应健康、轻度抑郁、中度抑郁、重度抑郁、双相情感障碍。<br>
     * 2. mentalHealthLevel取值为[0, 3]，表示患病严重程度，分别表示健康、轻度、中度、重度。其中重度抑郁和双相情感障碍同属于重度。<br>
     * 3. isMania变量仅在mentalHealthLevel为3（患重病）且mentalHealthId为4（患双相情感障碍）时可能为true，表示患者处于躁狂期。<br>
     * <br>
     * 注：异常情况会返回NONE状态。
     * @param player 需要判断精神健康状态的玩家
     * @see MentalStatus
     * @return 玩家当前的精神健康状态
     */
    public static MentalHealthStatus from(ServerPlayerEntity player) {
        if (player == null) {
            LOGGER.error("Null player provided");
            return MentalHealthStatus.NONE;
        }

        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        return from(mentalStatus);
    }

    public static MentalHealthStatus from(MentalIllness mentalIllness) {
        if (mentalIllness == null) {
            LOGGER.error("Null mentalIllness provided");
            return MentalHealthStatus.NONE;
        }

        if (mentalIllness.isMania) {
            return MentalHealthStatus.MANIC_PHASE;
        }
        return from(mentalIllness.mentalHealthId);
    }

    public static MentalHealthStatus from(MentalStatus mentalStatus) {
        if (mentalStatus == null) {
            LOGGER.error("Null mentalStatus provided");
            return MentalHealthStatus.NONE;
        }

        return from(mentalStatus.mentalIllness);
    }

    /**
     * 由于Depression的魔法值，根据mentalHealthId获取健康状态枚举是不现实、不可靠的。
     * 虽然mentalHealthId的取值范围是[0, 4]，但无法通过mentalHealthId区分重度抑郁和躁狂相，因此返回NONE。
     * @param mentalHealthId 魔法值mentalHealthId
     * @see MentalStatus
     * @return 玩家当前的精神健康状态
     */
    public static MentalHealthStatus from(int mentalHealthId) {
        return switch (mentalHealthId) {
            case 0 -> MentalHealthStatus.HEALTHY;
            case 1 -> MentalHealthStatus.MILD_DEPRESSION;
            case 2 -> MentalHealthStatus.MODERATE_DEPRESSION;
            case 3 -> MentalHealthStatus.MAJOR_DEPRESSION;
            default -> MentalHealthStatus.NONE;
        };
    }

    public static MentalHealthStatus from(String name) {
        for (MentalHealthStatus status : values()) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        return NONE;
    }

    /**
     * 根据MentalIllnessStatus和是否处于躁狂状态来获取MentalHealthStatus。
     * @param mentalIllnessStatus 患精神疾病情况
     * @param isMania 是否处于躁狂状态，仅在传入BIPOLAR_DISORDER时有效。
     * @return 对应的精神健康状态
     */
    public static MentalHealthStatus from(MentalIllnessStatus mentalIllnessStatus, boolean isMania) {
        if (mentalIllnessStatus == null) {
            LOGGER.error("MentalIllnessStatus is null when trying to get MentalHealthStatus");
            return MentalHealthStatus.NONE;
        }

        return switch (mentalIllnessStatus) {
            case HEALTHY -> MentalHealthStatus.HEALTHY;
            case MILD_DEPRESSION -> MentalHealthStatus.MILD_DEPRESSION;
            case MODERATE_DEPRESSION -> MentalHealthStatus.MODERATE_DEPRESSION;
            case MAJOR_DEPRESSIVE_DISORDER -> MentalHealthStatus.MAJOR_DEPRESSION;
            case BIPOLAR_DISORDER -> {
                if (isMania) {
                    yield MentalHealthStatus.MANIC_PHASE;
                } else {
                    yield MentalHealthStatus.MAJOR_DEPRESSION;
                }
            }
            case NONE -> {
                LOGGER.warn("MentalIllnessStatus is NONE when trying to get MentalHealthStatus");
                yield MentalHealthStatus.NONE;
            }
        };
    }

    public boolean isHealthierThan(MentalHealthStatus other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare when one is NONE");
            return false;
        }
        return this.getHealthyLevel() < other.getHealthyLevel();
    }

    public boolean isSickerThan(MentalHealthStatus other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare when one is NONE");
            return false;
        }
        return this.getHealthyLevel() > other.getHealthyLevel();
    }

    public boolean isSeverelyIll() {
        return this.getHealthyLevel() >= 3;
    }

    public boolean isSick() {
        return (this != HEALTHY);
    }

    public boolean isHealthy() {
        return (this == HEALTHY);
    }

    public boolean isNormal() {
        return (this == HEALTHY || this == NONE);
    }

    public boolean isDepressed() {
        return (this == MILD_DEPRESSION || this == MODERATE_DEPRESSION || this == MAJOR_DEPRESSION);
    }

    public boolean isMania() {
        return (this == MANIC_PHASE);
    }

    public double getFeelingInfluenceMultiplier() {
        return feelingInfluenceMultiplier;
    }
}
