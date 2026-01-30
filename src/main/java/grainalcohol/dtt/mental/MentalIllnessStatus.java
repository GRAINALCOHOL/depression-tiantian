package grainalcohol.dtt.mental;

import net.depression.mental.MentalStatus;
import net.depression.server.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 医学诊断，或者俗称“患精神疾病情况”，根据医学定义分类，仅表示玩家患病情况。<br>
 * 无法区分患双相时的抑郁相和躁狂相，如果需要区分请查看{@link MentalHealthStatus}。<br>
 * <br>
 * 包括健康、轻度抑郁、中度抑郁、重度抑郁障碍和双相情感障碍。<br>
 */
public enum MentalIllnessStatus {
    HEALTHY("healthy", Severity.HEALTHY, 0),
    MILD_DEPRESSION("mild_depression", Severity.MILD, 1),
    MODERATE_DEPRESSION("moderate_depression", Severity.MODERATE, 2),
    MAJOR_DEPRESSIVE_DISORDER("major_depressive_disorder", Severity.SEVERE, 3),
    BIPOLAR_DISORDER("bipolar_disorder", Severity.SEVERE, 3),
    NONE("none", Severity.NONE, -1)
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(MentalIllnessStatus.class);
    private final int mentalHealthId;
    private final Severity severity; // 来自mentalHealthLevel字段
    private final String name;

    MentalIllnessStatus(String name, Severity severity, int mentalHealthId) {
        this.name = name;
        this.severity = severity;
        this.mentalHealthId = mentalHealthId;
    }

    public static MentalIllnessStatus from(ServerPlayerEntity player) {
        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        if (mentalStatus == null) {
            LOGGER.error("No mentalStatus found for player {}", player.getUuid());
            return MentalIllnessStatus.NONE;
        }
        return from(mentalStatus);
    }

    public static MentalIllnessStatus from(MentalStatus mentalStatus) {
        if (mentalStatus == null) {
            LOGGER.error("MentalStatus is null when trying to get MentalIllnessStatus");
            return MentalIllnessStatus.NONE;
        }
        return from(mentalStatus.getMentalHealthId());
    }

    public static MentalIllnessStatus from(int mentalHealthId) {
        return switch (mentalHealthId) {
            case 0 -> HEALTHY;
            case 1 -> MILD_DEPRESSION;
            case 2 -> MODERATE_DEPRESSION;
            case 3 -> MAJOR_DEPRESSIVE_DISORDER;
            case 4 -> BIPOLAR_DISORDER;
            default -> NONE;
        };
    }

    /**
     * 判断玩家是否患有双相情感障碍。
     * mentalHealthId取值为[0, 4]，表示具体的病理名称，分别对应健康、轻度抑郁、中度抑郁、重度抑郁、双相情感障碍。
     * @param player 需要判断的玩家
     * @return 是否患有双相情感障碍
     */
    public static boolean isBipolarDisorder(ServerPlayerEntity player) {
        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        if (mentalStatus == null) {
            LOGGER.error("No mentalStatus found for player {}", player.getUuid());
            return false;
        }
        return isBipolarDisorder(mentalStatus);
    }

    public static boolean isBipolarDisorder(MentalStatus mentalStatus) {
        return isBipolarDisorder(mentalStatus.getMentalHealthId());
    }

    public static boolean isBipolarDisorder(int mentalHealthId) {
        return mentalHealthId == 4;
    }

    public boolean isBipolarDisorder() {
        return this == BIPOLAR_DISORDER;
    }

    public String getName() {
        return name;
    }

    public boolean isHealthierThan(MentalIllnessStatus other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is healthier when one is NONE");
            return false;
        }
        return this.getSeverity().isHealthierThan(other.getSeverity());
    }

    public boolean isSickerThan(MentalIllnessStatus other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is sicker when one is NONE");
            return false;
        }
        return this.getSeverity().isSickerThan(other.getSeverity());
    }

    public static MentalIllnessStatus from(String name) {
        for (MentalIllnessStatus status : values()) {
            if (status.getName().equals(name)) {
                return status;
            }
        }
        return NONE;
    }

    public Text getDisplayText() {
        return Text.translatable("mental.illness.dtt." + this.getName());
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getSeverityInt() {
        return severity.getLevel();
    }

    public int getMentalHealthId() {
        return mentalHealthId;
    }

    public boolean isSeverelyIll() {
        return this.getSeverity().isSeverelyIll();
    }
}
