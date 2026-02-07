package grainalcohol.dtt.diary.dailystat;

import grainalcohol.dtt.config.DTTConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DailyStatManager {
    private static final double EMAFactor = MathHelper.clamp(DTTConfig.getInstance().getServerConfig().diaryConfig.ema_factor, 0.0, 1.0);

    private static final Map<UUID, DailyStat> dailyStat = new ConcurrentHashMap<>();
    private static final Map<UUID, DailyStat> yesterdayDailyStat = new ConcurrentHashMap<>();
    private static final Map<UUID, DailyStat> movingAverageDailyStat = new ConcurrentHashMap<>();

    public static void updateDailyStat(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();

        // 日期更新时：
        DailyStat todayStat = getTodayDailyStat(playerId);
        if (todayStat != null) {
            // 将当前的数据转存到昨天里面
            yesterdayDailyStat.put(playerId, todayStat);

            // 将当前的数据用于计算移动平均值
            DailyStat previousAverage = movingAverageDailyStat.get(playerId);
            if (previousAverage == null) {
                previousAverage = new DailyStat();
            }

            // EMA因子越大，新数据越重要，过去的数据越不重要
            DailyStat exponentialMovingAverage = new DailyStat(todayStat);
            exponentialMovingAverage.multiply(EMAFactor);
            previousAverage.multiply(1 - EMAFactor);

            exponentialMovingAverage.merge(previousAverage);
            movingAverageDailyStat.put(playerId, exponentialMovingAverage);
        }
        dailyStat.remove(playerId);
    }

    public static DailyStat getTodayDailyStat(UUID uuid) {
        dailyStat.computeIfAbsent(uuid, k -> new DailyStat());
        return dailyStat.get(uuid);
    }

    public static DailyStat getYesterdayDailyStat(UUID uuid) {
        yesterdayDailyStat.computeIfAbsent(uuid, k -> new DailyStat());
        return yesterdayDailyStat.get(uuid);
    }

    public static DailyStat getMovingAverageDailyStat(UUID uuid) {
        movingAverageDailyStat.computeIfAbsent(uuid, k -> new DailyStat());
        return movingAverageDailyStat.get(uuid);
    }

    public static void setTodayDailyStat(UUID uuid, DailyStat today) {
        dailyStat.put(uuid, today);
    }

    public static void setYesterdayDailyStat(UUID uuid, DailyStat yesterday) {
        yesterdayDailyStat.put(uuid, yesterday);
    }

    public static void setMovingAverageDailyStat(UUID uuid, DailyStat movingAverage) {
        movingAverageDailyStat.put(uuid, movingAverage);
    }
}
