package grainalcohol.dtt.diary.feeling;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum Feeling {
    // 有希望的 & 虚无的
    HOPEFUL("hopeful"), NIHILISTIC("nihilistic"),

    // 温暖的 & 孤独的
    WARM("warm"), LONELY("lonely"),

    // 成功的 & 失败的
    SUCCESSFUL("successful"), FAILED("failed")
    ;

    private final String name;

    Feeling(String name) {
        this.name = name;
    }

    public Feeling getOppositeFeeling() {
        return switch (this) {
            case HOPEFUL -> NIHILISTIC;
            case NIHILISTIC -> HOPEFUL;
            case WARM -> LONELY;
            case LONELY -> WARM;
            case SUCCESSFUL -> FAILED;
            case FAILED -> SUCCESSFUL;
        };
    }

    public boolean isPositiveFeeling() {
        return switch (this) {
            case HOPEFUL, WARM, SUCCESSFUL -> true;
            case NIHILISTIC, LONELY, FAILED -> false;
        };
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public static Feeling fromName(String name) {
        for (Feeling feeling : Feeling.values()) {
            if (feeling.getName().equalsIgnoreCase(name)) {
                return feeling;
            }
        }
        return null;
    }

    public static List<Feeling> positiveFeelings() {
        return List.of(HOPEFUL, WARM, SUCCESSFUL);
    }

    public  static List<Feeling> negativeFeelings() {
        return List.of(NIHILISTIC, LONELY, FAILED);
    }
}
