package grainalcohol.dtt.api.wrapper;

import grainalcohol.dtt.api.helper.EmotionHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public enum EmotionLevel {
    HIGH_UNDEFINED("high_undefined", 8),
    EXCITED("excited", 7),
    PLEASED("pleased", 6),
    SLIGHTLY_PLEASED("slightly_pleased", 5),
    CALM("calm", 4),
    SLIGHTLY_SAD("slightly_sad", 3),
    SAD("sad", 2),
    DESPAIR("despair", 1),
    LOW_UNDEFINED("low_undefined", 0)
    ;

    private final String name;
    private final int level;

    EmotionLevel(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public static EmotionLevel from(ServerPlayerEntity player) {
        return EmotionHelper.getEmotionLevel(player);
    }

    public static @Nullable EmotionLevel from(int level) {
        if (level >= HIGH_UNDEFINED.getLevel()) return HIGH_UNDEFINED;
        if (level <= LOW_UNDEFINED.getLevel()) return LOW_UNDEFINED;

        for (EmotionLevel emotionLevel : values()) {
            if (emotionLevel.level == level) {
                return emotionLevel;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
