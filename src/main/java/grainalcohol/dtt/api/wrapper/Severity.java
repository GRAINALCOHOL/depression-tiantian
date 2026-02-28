package grainalcohol.dtt.api.wrapper;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于描述患病状态或精神健康状态的严重程度
 */
public enum Severity {
    HEALTHY(0, "healthy"),
    MILD(1, "mild"),
    MODERATE(2, "moderate"),
    SEVERE(3, "severe"),
    NONE(-1, "none")
    ;

    Severity(int level, String name) {
        this.level = level;
        this.name = name;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Severity.class);
    private final int level;
    private final String name;

    /**
     * 根据精神健康状态解析严重程度
     * @param mentalHealthStatus 需要解析的精神健康状态
     * @return 精神健康状态对应的严重程度
     */
    public static Severity from(MentalHealthStatus mentalHealthStatus) {
        return mentalHealthStatus.getSeverity();
    }

    /**
     * 根据患病状态解析严重程度
     * @param mentalIllnessStatus 需要解析的患病状态
     * @return 患病状态对应的严重程度
     */
    public static Severity from(MentalIllnessStatus mentalIllnessStatus) {
        return mentalIllnessStatus.getSeverity();
    }

    /**
     * 根据PTSD等级解析严重程度
     * @param ptsdLevel 需要解析的PTSD等级
     * @return PTSD等级对应的严重程度
     */
    public static Severity from(PTSDLevel ptsdLevel) {
        return ptsdLevel.getSeverity();
    }

    /**
     * 默认根据玩家的患病状态解析严重程度
     * @param player 需要解析的玩家
     * @return 玩家的患病状态对应的严重程度
     */
    public static Severity from(ServerPlayerEntity player) {
        return from(MentalIllnessStatus.from(player));
    }

    public Text getDisplayText() {
        return Text.translatable("mental.severity.dtt." + name + ".name");
    }

    public boolean isHealthierThan(Severity other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is healthier when one is NONE");
            return false;
        }

        return this.level < other.level;
    }

    public boolean isSickerThan(Severity other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is sicker when one is NONE");
            return false;
        }

        return this.level > other.level;
    }

    public boolean isSeverelyIll() {
        return this.level >= SEVERE.level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
