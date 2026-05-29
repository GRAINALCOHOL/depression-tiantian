package grainalcohol.dtt.hint.timer;

public enum TimeUnit {
    TICK(1),
    SECOND(20),
    MINUTE(20 * 60),
    GAME_HOUR(1_000),
    GAME_DAY(24_000)
    ;

    TimeUnit(int durationTicks) {
        this.durationTicks = durationTicks;
    }

    private final int durationTicks;

    public int getDurationTicks() {
        return durationTicks;
    }
}
