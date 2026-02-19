package grainalcohol.dtt.diary.dailystat;

import grainalcohol.dtt.config.DTTConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

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

    /**
     * 获取今天的统计中的整数数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static int getTodayValue(UUID uuid, Function<DailyStat, Integer> dataExtractor) {
        DailyStat todayStat = getTodayDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 获取今天的统计中的布尔数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static boolean getTodayResult(UUID uuid, Function<DailyStat, Boolean> dataExtractor) {
        DailyStat todayStat = getTodayDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 判断今天的统计中，某个整数数据是否高于阈值
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @param threshold 阈值，只有当提取的数据大于等于该值时才返回true
     * @return 提取的数据值是否大于等于阈值
     */
    public static boolean getTodayResult(UUID uuid, Function<DailyStat, Integer> dataExtractor, int threshold) {
        DailyStat todayStat = getTodayDailyStat(uuid);
        return dataExtractor.apply(todayStat) >= threshold;
    }

    /**
     * 获取昨天的统计中的整数数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static int getYesterdayValue(UUID uuid, Function<DailyStat, Integer> dataExtractor) {
        DailyStat todayStat = getYesterdayDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 获取昨天的统计中的布尔数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static boolean getYesterdayResult(UUID uuid, Function<DailyStat, Boolean> dataExtractor) {
        DailyStat todayStat = getYesterdayDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 判断昨天的统计中，某个整数数据是否高于阈值
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @param threshold 阈值，只有当提取的数据大于等于该值时才返回true
     * @return 提取的数据值是否大于等于阈值
     */
    public static boolean getYesterdayResult(UUID uuid, Function<DailyStat, Integer> dataExtractor, int threshold) {
        DailyStat todayStat = getYesterdayDailyStat(uuid);
        return dataExtractor.apply(todayStat) >= threshold;
    }

    /**
     * 获取移动平均值统计中的整数数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static int getEMAValue(UUID uuid, Function<DailyStat, Integer> dataExtractor) {
        DailyStat todayStat = getMovingAverageDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 获取移动平均值统计中的布尔数据
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @return 提取的数据值
     */
    public static boolean getEMAResult(UUID uuid, Function<DailyStat, Boolean> dataExtractor) {
        DailyStat todayStat = getMovingAverageDailyStat(uuid);
        return dataExtractor.apply(todayStat);
    }

    /**
     * 判断移动平均值统计中，某个整数数据是否高于阈值
     * @param uuid 玩家UUID
     * @param dataExtractor 从DailyStat中提取何种数据
     * @param threshold 阈值，只有当提取的数据大于等于该值时才返回true
     * @return 提取的数据值是否大于等于阈值
     */
    public static boolean getEMAResult(UUID uuid, Function<DailyStat, Integer> dataExtractor, int threshold) {
        return getEMAValue(uuid, dataExtractor) >= threshold;
    }
}
