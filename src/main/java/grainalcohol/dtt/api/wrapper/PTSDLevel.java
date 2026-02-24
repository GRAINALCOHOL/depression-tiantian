package grainalcohol.dtt.api.wrapper;

public enum PTSDLevel {
    LATENT("latent", 0),
    MILD("mild", 1),
    MODERATE("moderate", 2),
    SEVERE("severe", 3),
    EXTREME("extreme", 4),
    CLEAR("clear", -1)
    ;

    PTSDLevel(String name, int level) {
        this.name = name;
        this.level = level;
    }

    private final String name;
    private final int level;

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public boolean isLatent() {
        return this == LATENT;
    }

    public boolean hasSymptoms() {
        return this.isSickerThan(LATENT);
    }

    public boolean isExtreme() {
        return this == EXTREME;
    }

    public boolean willPanting() {
        return this.isSickerThan(LATENT);
    }

    public boolean willHeartBeat() {
        return this.isSickerThan(LATENT);
    }

    public boolean willTinnitus() {
        return this.isSickerThan(MILD);
    }

    public boolean willPhonism() {
        return this.isSickerThan(MODERATE);
    }

    public boolean willPhotism() {
        return this.isSickerThan(SEVERE);
    }

    public boolean isHealthierThan(PTSDLevel other) {
        return this.getLevel() < other.getLevel();
    }

    public boolean isSickerThan(PTSDLevel other) {
        return this.getLevel() > other.getLevel();
    }

    public static PTSDLevel from(int level) {
        for (PTSDLevel ptsdLevel : values()) {
            if (ptsdLevel.getLevel() == level) {
                return ptsdLevel;
            }
        }
        return CLEAR;
    }
}
