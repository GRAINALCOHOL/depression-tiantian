package grainalcohol.dtt.mental;

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

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
