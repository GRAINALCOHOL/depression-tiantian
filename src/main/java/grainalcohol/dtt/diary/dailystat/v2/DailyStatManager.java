package grainalcohol.dtt.diary.dailystat.v2;

import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DailyStatManager {
    // NBT keys
    public static final String DAILY_STAT_NBT_KEY = "DTTDailyStat";
    public static final String TODAY_DAILY_STAT_NBT_KEY = "Today";
    public static final String YESTERDAY_DAILY_STAT_NBT_KEY = "Yesterday";
    public static final String MOVING_AVERAGE_DAILY_STAT_NBT_KEY = "MovingAverage";

    private static final Map<UUID, DailyStat> todayStats = new ConcurrentHashMap<>();
    private static final Map<UUID, DailyStat> yesterdayStats = new ConcurrentHashMap<>();
    private static final Map<UUID, DailyStat> EMAStats = new ConcurrentHashMap<>();

    public static void updateDailyStat(UUID playerId, double EMAFactor) {
        // 日期更新时：
        DailyStat todayStat = todayStats.get(playerId);

        if (todayStat == null) return;

        // 覆盖昨天的数据
        yesterdayStats.replace(playerId, todayStat);

        // 将当前的数据用于计算移动平均值
        DailyStat previousEMA = EMAStats.get(playerId);
        if (previousEMA == null) {
            previousEMA = new DailyStat();
        }

        // EMA因子越大，新数据越重要，旧数据越不重要
        DailyStat newEMA = new DailyStat(todayStat);
        // 新数据
        newEMA.multiply(EMAFactor);
        // 旧数据
        previousEMA.multiply(1 - EMAFactor);

        // 合并并覆盖
        newEMA.merge(previousEMA);
        EMAStats.replace(playerId, newEMA);

        // 清空今天的数据
        todayStats.replace(playerId, new DailyStat());
    }

    public static DailyStat getTodayStat(UUID playerId) {
        todayStats.computeIfAbsent(playerId, k -> new DailyStat());
        return todayStats.get(playerId);
    }

    public static DailyStat getYesterdayStat(UUID playerId) {
        yesterdayStats.computeIfAbsent(playerId, k -> new DailyStat());
        return yesterdayStats.get(playerId);
    }

    public static DailyStat getEMAStat(UUID playerId) {
        EMAStats.computeIfAbsent(playerId, k -> new DailyStat());
        return EMAStats.get(playerId);
    }

    public static void writeToNbt(UUID playerId, NbtCompound nbt) {
        NbtCompound dailyStatNbt = new NbtCompound();

        DailyStat todayStat = getTodayStat(playerId);
        DailyStat yesterdayStat = getYesterdayStat(playerId);
        DailyStat emaStat = getEMAStat(playerId);

        NbtCompound todayNbt = new NbtCompound();
        todayStat.writeToNbt(todayNbt);
        dailyStatNbt.put(TODAY_DAILY_STAT_NBT_KEY, todayNbt);

        NbtCompound yesterdayNbt = new NbtCompound();
        yesterdayStat.writeToNbt(yesterdayNbt);
        dailyStatNbt.put(YESTERDAY_DAILY_STAT_NBT_KEY, yesterdayNbt);

        NbtCompound emaNbt = new NbtCompound();
        emaStat.writeToNbt(emaNbt);
        dailyStatNbt.put(MOVING_AVERAGE_DAILY_STAT_NBT_KEY, emaNbt);

        nbt.put(DAILY_STAT_NBT_KEY, dailyStatNbt);
    }

    public static void readFromNbt(UUID playerId, NbtCompound nbt) {
        if (!nbt.contains(DAILY_STAT_NBT_KEY)) return;

        NbtCompound dailyStatNbt = nbt.getCompound(DAILY_STAT_NBT_KEY);

        if (dailyStatNbt.contains(TODAY_DAILY_STAT_NBT_KEY)) {
            DailyStat todayStat = new DailyStat();
            todayStat.readFromNbt(dailyStatNbt.getCompound(TODAY_DAILY_STAT_NBT_KEY));
            todayStats.put(playerId, todayStat);
        }

        if (dailyStatNbt.contains(YESTERDAY_DAILY_STAT_NBT_KEY)) {
            DailyStat yesterdayStat = new DailyStat();
            yesterdayStat.readFromNbt(dailyStatNbt.getCompound(YESTERDAY_DAILY_STAT_NBT_KEY));
            yesterdayStats.put(playerId, yesterdayStat);
        }

        if (dailyStatNbt.contains(MOVING_AVERAGE_DAILY_STAT_NBT_KEY)) {
            DailyStat emaStat = new DailyStat();
            emaStat.readFromNbt(dailyStatNbt.getCompound(MOVING_AVERAGE_DAILY_STAT_NBT_KEY));
            EMAStats.put(playerId, emaStat);
        }
    }
}
