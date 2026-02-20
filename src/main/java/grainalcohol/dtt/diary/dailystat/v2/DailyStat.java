package grainalcohol.dtt.diary.dailystat.v2;

import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class DailyStat {
    private final Map<DailyStatKey<Integer>, Integer> NUMBER_STATS_MAP;
    private final Map<DailyStatKey<Boolean>, Boolean> BOOLEAN_STATS_MAP;

    public DailyStat() {
        NUMBER_STATS_MAP = new HashMap<>();
        for (var statKey : DTTRegistries.NUMBER_STAT_KEY_REGISTRY) {
            NUMBER_STATS_MAP.put(statKey, 0);
        }
        BOOLEAN_STATS_MAP = new HashMap<>();
        for (var statKey : DTTRegistries.BOOLEAN_STAT_KEY_REGISTRY) {
            BOOLEAN_STATS_MAP.put(statKey, false);
        }
    }

    public DailyStat(DailyStat other) {
        this.NUMBER_STATS_MAP = new HashMap<>(other.NUMBER_STATS_MAP);
        this.BOOLEAN_STATS_MAP = new HashMap<>(other.BOOLEAN_STATS_MAP);
    }

    public Map<DailyStatKey<Integer>, Integer> getNumberStatMap() {
        return NUMBER_STATS_MAP;
    }

    public Map<DailyStatKey<Boolean>, Boolean> getBooleanStatMap() {
        return BOOLEAN_STATS_MAP;
    }

    public void incrementNumberStat(DailyStatKey<Integer> key) {
        increaseNumberStat(key, 1);
    }

    public void increaseNumberStat(DailyStatKey<Integer> key, int amount) {
        setNumberStat(key, getNumberStat(key) + amount);
    }

    public void setNumberStat(DailyStatKey<Integer> key, int value) {
        NUMBER_STATS_MAP.replace(key, value);
    }

    public void setBooleanStat(DailyStatKey<Boolean> key, boolean value) {
        BOOLEAN_STATS_MAP.replace(key, value);
    }

    public void setTrueStat(DailyStatKey<Boolean> key) {
        setBooleanStat(key, true);
    }

    public void setFalseStat(DailyStatKey<Boolean> key) {
        setBooleanStat(key, false);
    }

    public int getNumberStat(DailyStatKey<Integer> key) {
        NUMBER_STATS_MAP.putIfAbsent(key, 0);
        return NUMBER_STATS_MAP.get(key);
    }

    public boolean getBooleanStat(DailyStatKey<Boolean> key) {
        BOOLEAN_STATS_MAP.putIfAbsent(key, false);
        return BOOLEAN_STATS_MAP.get(key);
    }

    public DailyStat copy() {
        DailyStat copy = new DailyStat();
        copy.NUMBER_STATS_MAP.putAll(this.NUMBER_STATS_MAP);
        copy.BOOLEAN_STATS_MAP.putAll(this.BOOLEAN_STATS_MAP);
        return copy;
    }

    public void multiply(double multiplier) {
        NUMBER_STATS_MAP.replaceAll((k, v) -> (int) (v * multiplier));
    }

    public void merge(DailyStat other) {
        other.NUMBER_STATS_MAP.forEach((k, v) -> NUMBER_STATS_MAP.merge(k, v, Integer::sum));
        other.BOOLEAN_STATS_MAP.forEach((k, v) -> BOOLEAN_STATS_MAP.merge(k, v, (a, b) -> a || b));
    }

    public void writeToNbt(NbtCompound nbt) {
        NUMBER_STATS_MAP.forEach((key, value) -> nbt.putInt(key.toString(), value));
        BOOLEAN_STATS_MAP.forEach((key, value) -> nbt.putBoolean(key.toString(), value));
    }

    public void readFromNbt(NbtCompound nbt) {
        for (var numberStatKey : DTTRegistries.NUMBER_STAT_KEY_REGISTRY) {
            if (nbt.contains(numberStatKey.toString())) {
                NUMBER_STATS_MAP.put(numberStatKey, nbt.getInt(numberStatKey.toString()));
            } else {
                NUMBER_STATS_MAP.put(numberStatKey, 0);
            }
        }
        for (var booleanStatKey : DTTRegistries.BOOLEAN_STAT_KEY_REGISTRY) {
            if (nbt.contains(booleanStatKey.toString())) {
                BOOLEAN_STATS_MAP.put(booleanStatKey, nbt.getBoolean(booleanStatKey.toString()));
            } else {
                BOOLEAN_STATS_MAP.put(booleanStatKey, false);
            }
        }
    }
}
