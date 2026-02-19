package grainalcohol.dtt.diary.feeling.v2;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum Feeling {
    // 温暖的 & 虚无的
    WARM("warm", false), NIHILISTIC("nihilistic", true),

    // 成功的 & 失败的
    SUCCESSFUL("successful", false), FAILED("failed", true)
    ;

    private final String translationKey;
    private final boolean negative;

    Feeling(String translationKey, boolean negative) {
        this.translationKey = translationKey;
        this.negative = negative;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public boolean isNegative() {
        return negative;
    }

    public Feeling getOpposite() {
        return switch (this) {
            case NIHILISTIC -> WARM;
            case WARM -> NIHILISTIC;
            case SUCCESSFUL -> FAILED;
            case FAILED -> SUCCESSFUL;
        };
    }

    @Nullable
    public static Feeling fromTranslationKey(String translationKey) {
        for (Feeling feeling : Feeling.values()) {
            if (feeling.getTranslationKey().equals(translationKey)) {
                return feeling;
            }
        }
        return null;
    }

    public static List<Feeling> getPositiveFeelings() {
        return List.of(WARM, SUCCESSFUL);
    }

    public  static List<Feeling> getNegativeFeelings() {
        return List.of(NIHILISTIC, FAILED);
    }
}
