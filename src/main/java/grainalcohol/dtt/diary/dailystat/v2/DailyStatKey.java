package grainalcohol.dtt.diary.dailystat.v2;

import net.minecraft.util.Identifier;

public class DailyStatKey<T> {
    private final Identifier identifier;
    private final Class<T> type;

    public DailyStatKey(Identifier identifier, Class<T> type) {
        this.identifier = identifier;
        this.type = type;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Class<T> getType() {
        return type;
    }

    public static DailyStatKey<Boolean> createBooleanKey(Identifier identifier) {
        return new DailyStatKey<>(identifier, Boolean.class);
    }

    public static DailyStatKey<Integer> createNumberKey(Identifier identifier) {
        return new DailyStatKey<>(identifier, Integer.class);
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
