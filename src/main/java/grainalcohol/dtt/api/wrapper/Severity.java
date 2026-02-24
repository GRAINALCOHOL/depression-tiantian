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

    public static Severity from(MentalHealthStatus mentalHealthStatus) {
        for (Severity severity : Severity.values()) {
            if (severity == mentalHealthStatus.getSeverity()) {
                return severity;
            }
        }
        return NONE;
    }

    public static Severity from(MentalIllnessStatus mentalIllnessStatus) {
        for (Severity severity : Severity.values()) {
            if (severity == mentalIllnessStatus.getSeverity()) {
                return severity;
            }
        }
        return NONE;
    }

    public static Severity from(ServerPlayerEntity player) {
        return from(MentalIllnessStatus.from(player));
    }

    public Text getDisplayText() {
        return Text.translatable("mental.severity.dtt." + name + ".name");
    }

    public boolean isHealthierThan(Severity other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is healthier when one is CLEAR");
            return false;
        }

        return this.level < other.level;
    }

    public boolean isSickerThan(Severity other) {
        if (this == NONE || other == NONE) {
            LOGGER.error("Cannot compare which is sicker when one is CLEAR");
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
