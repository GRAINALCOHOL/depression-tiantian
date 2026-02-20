package grainalcohol.dtt.diary.topic;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatKey;

import java.util.UUID;
import java.util.function.Function;

public class TopicWeightCalculator {
    private static final double LOG_SCALE = 100.0; // 缩放因子
    private static final double MAX_WEIGHT_ROUND = 10.0;
    private static final double MIN_NORMALIZATION_VALUE = 10.0;

    public static double calculateWeight(UUID playerId, DailyStatKey<Integer> dailyStatKey) {
        int todayValue = DailyStatManager.getTodayStat(playerId).getNumberStat(dailyStatKey);
        int yesterdayValue = DailyStatManager.getYesterdayStat(playerId).getNumberStat(dailyStatKey);
        int emaValue = DailyStatManager.getEMAStat(playerId).getNumberStat(dailyStatKey);

        return calculateWeight(todayValue, yesterdayValue, emaValue);
    }

    public static double calculateWeight(UUID playerUUID, Function<grainalcohol.dtt.diary.dailystat.DailyStat, Integer> statExtractor) {
        int todayValue = grainalcohol.dtt.diary.dailystat.DailyStatManager.getTodayValue(playerUUID, statExtractor);
        int yesterdayValue = grainalcohol.dtt.diary.dailystat.DailyStatManager.getYesterdayValue(playerUUID, statExtractor);
        int emaValue = grainalcohol.dtt.diary.dailystat.DailyStatManager.getEMAValue(playerUUID, statExtractor);

        return calculateWeight(todayValue, yesterdayValue, emaValue);
    }

    /**
     * 计算话题权重
     * 基于当日统计的今天、昨天和EMA数据，综合考虑进步程度和绝对重要性
     * 权重可以为负，为负表示退步
     * 权重范围总是在(-10,+10)内
     */
    public static double calculateWeight(int todayValue, int yesterdayValue, int emaValue) {
        // 归一化基准
        double normalizationBase = Math.max(1.0, calculateNormalizationBase(todayValue, yesterdayValue, emaValue));

        // 归一化数据（避免除零）
        double normalizedToday = todayValue / normalizationBase;
        double normalizedYesterday = yesterdayValue / normalizationBase;
        double normalizedEMA = emaValue / normalizationBase;

        // 短期进步程度（基于昨天数据）
        double progressFromYesterday = calculateProgress(normalizedToday, normalizedYesterday);

        // 长期进步程度（基于EMA数据）
        double progressFromEMA = calculateProgress(normalizedToday, normalizedEMA);

        // 绝对重要性（今天的数据本身的重要性）
        double absoluteImportance = calculateAbsoluteImportance(normalizedToday);

        // 聚合权重 = 进步程度 + 绝对重要性
        // 进步程度占60%，绝对重要性占40%
        double rawWeight = (progressFromYesterday * 0.3 + progressFromEMA * 0.3) + (absoluteImportance * 0.4);

        return smoothClampWeight(rawWeight);
    }

    // 计算进步程度
    private static double calculateProgress(double today, double reference) {
        if (reference == 0) {
            // 如果参考值为0，进步就是今天的值本身
            return calculateLogWeight(today, 10.0);
        }

        // 计算相对变化率
        double change = (today - reference) / reference;

        // 使用对数函数平滑变化，允许负值
        // 除以较小倍率以强调结果
        return calculateLogWeight(Math.abs(change), 5.0) * Math.signum(change);
    }

    // 计算绝对重要性
    private static double calculateAbsoluteImportance(double todayValue) {
        return calculateLogWeight(todayValue, 20.0);
    }

    // 计算归一化基准
    private static double calculateNormalizationBase(int today, int yesterday, int ema) {
        // 取三个数据的最大值作为基准
        double maxValue = Math.max(today, Math.max(yesterday, ema));

        // 如果最大值太小，使用预设的最小基准
        if (maxValue < MIN_NORMALIZATION_VALUE) {
            return Math.max(maxValue, MIN_NORMALIZATION_VALUE); // 最小基准为10
        }

        return maxValue;
    }

    // 平滑映射结果范围
    private static double smoothClampWeight(double weight) {
        // S型函数参数：控制平滑程度
        double steepness = 0.12; // 值越小越平滑

        // 将权重映射到S型函数输入范围
        double scaledWeight = weight * steepness;

        // S型函数：将无限范围映射到(-1,1)
        double sigmoid = 2.0 / (1.0 + Math.exp(-scaledWeight)) - 1.0;

        // 缩放到(-10,+10)范围（默认）
        return sigmoid * MAX_WEIGHT_ROUND;
    }

    private static double calculateLogWeight(double value, double divisor) {
        return Math.log(value + 1) * LOG_SCALE / divisor;
    }
}
