package grainalcohol.dtt.diary.v2;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.feeling.v2.FeelingProducer;
import grainalcohol.dtt.diary.topic.v2.TopicProducer;
import grainalcohol.dtt.mental.MentalHealthStatus;
import grainalcohol.dtt.util.MathUtil;
import grainalcohol.dtt.util.StringUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DiaryContentProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiaryContentProducer.class);
    private static final Random RANDOM = new Random();
    private final MentalHealthStatus mentalHealthStatus;

    private final TopicProducer topicProducer;
    private final FeelingProducer feelingProducer;

    private final Feeling feeling;
    private final boolean hasCured; // 康复
    private final boolean hasWorsened; // 恶化

    private final List<String> usedTranslationKeys;

    public DiaryContentProducer(ServerPlayerEntity player) {
        this.mentalHealthStatus = MentalHealthStatus.from(player);
        boolean gentleMode = DTTConfig.getInstance().getServerConfig().diaryConfig.gentle_mode;
        this.topicProducer = new TopicProducer(player, gentleMode);

        this.feelingProducer = new FeelingProducer(getTopicProducer().getAllContextAttributes(), gentleMode);
        this.feeling = feelingProducer.getFeeling();

        this.hasCured = DailyStatManager.getTodayResult(player.getUuid(), DailyStat::isHasCured);
        this.hasWorsened = DailyStatManager.getTodayResult(player.getUuid(), DailyStat::isHasWorsened);

        this.usedTranslationKeys = new ArrayList<>();
    }

    public String getPrefixTranslationKey() {
        return composeTranslationKey("diary", DTTMod.MOD_ID);
    }

    public String composeTranslationKey(String... translationKey) {
        return String.join(".", translationKey);
    }

    public String completeTranslationKeyProcess() {
        if (shouldSkipDiary()) {
            // 因抑郁情绪拒绝写日记
            return generateTranslationKey(DiaryParagraph.NO_DIARY);
        }
        int variantCount = DTTConfig.getInstance().getServerConfig().diaryConfig.diary_translation_key_variant_count;

        String mentalHealthChangeKey = "";
        if (isHasCured()) {
            mentalHealthChangeKey = generateTranslationKey(DiaryParagraph.CURED, variantCount);
        }
        if (isHasWorsened()) {
            mentalHealthChangeKey = generateTranslationKey(DiaryParagraph.WORSENED, variantCount);
        }

        String manicInsertionKey = generateTranslationKey(DiaryParagraph.MANIC_GENERAL, variantCount);

        return // 开头段落
                "    " + generateTranslationKey(DiaryParagraph.OPENING, variantCount)

                        // 正文段落
                        + "\n    " + generateTranslationKey(DiaryParagraph.WEATHER, variantCount) // 天气话题
                        + (!(isHasCured() && isHasWorsened()) ? mentalHealthChangeKey : "") // 一天内同时出现康复与恶化则不记录
                        + generateTranslationKey(DiaryParagraph.TOPIC, variantCount) // 话题1
                        + "\n    " + generateTranslationKey(DiaryParagraph.GENERAL, variantCount) // 通用段落
                        + generateTranslationKey(DiaryParagraph.TOPIC, variantCount) // 话题2
                        + (shouldAppendManicContent() ? manicInsertionKey : "") // 躁狂额外通用段落

                        // 结尾段落
                        + "\n    " + generateTranslationKey(DiaryParagraph.CLOSING, variantCount);
    }

    private boolean shouldSkipDiary() {
        return switch (getMentalHealthStatus()) {
            case MODERATE_DEPRESSION -> MathUtil.chance(RANDOM, 0.2);
            case MAJOR_DEPRESSION -> MathUtil.chance(RANDOM, 0.9);
            default -> false;
        };
    }

    /**
     * 根据心理健康状态决定是否附加额外内容<br>
     * @return 是否应该附加额外内容
     */
    private boolean shouldAppendContent() {
        return switch (getMentalHealthStatus()) {
            case HEALTHY, NONE -> MathUtil.chance(RANDOM, 0.4);
            case MILD_DEPRESSION -> MathUtil.chance(RANDOM, 0.2);
            case MODERATE_DEPRESSION, MAJOR_DEPRESSION -> MathUtil.chance(RANDOM, 0.1);
            case MANIC_PHASE -> true;
        };
    }

    private boolean shouldAppendManicContent() {
        if (getMentalHealthStatus() == MentalHealthStatus.MANIC_PHASE) {
            return MathUtil.chance(RANDOM, 0.8);
        }
        return false;
    }

    /**
     * 生成指定段落的translationKey，{@link DiaryParagraph#TOPIC}会消费指定话题队列中的话题
     * @param paragraph 指定的段落
     * @param variantCount 查找指定数量的变体
     * @see #findRandomVariant(String, int)
     * @return 生成的translationKey，会使用单引号包装，生成失败返回空字符串
     */
    public String generateTranslationKey(DiaryParagraph paragraph, int variantCount) {
        String result = generateTranslationKey(paragraph, false); // 不使用单引号包装
        if (result.isEmpty()) return "";

        result = findRandomVariant(result, variantCount);
        return StringUtil.warp(result);
    }

    /**
     * 生成指定段落的translationKey，{@link DiaryParagraph#TOPIC}会消费指定话题队列中的话题
     * @param paragraph 指定的段落
     * @return 生成的translationKey，会使用单引号包装，生成失败返回空字符串
     */
    public String generateTranslationKey(DiaryParagraph paragraph) {
        return generateTranslationKey(paragraph, true);
    }

    /**
     * 生成指定段落的translationKey，{@link DiaryParagraph#TOPIC}会消费指定话题队列中的话题
     * @param paragraph 指定的段落
     * @param enableWarp 是否使用单引号包装首尾（原因是depression客户端以单引号为边界处理本地化）
     * @return 生成的translationKey，生成失败返回空字符串
     */
    public String generateTranslationKey(DiaryParagraph paragraph, boolean enableWarp) {
        String result = switch (paragraph) {
            case MANIC_GENERAL -> composeTranslationKey(
                    // 躁狂额外段落仅与感受相关
                    getPrefixTranslationKey(),
                    paragraph.getTranslationKey(),
                    getFeeling().getTranslationKey()
            );
            case NO_DIARY, CURED, WORSENED -> composeTranslationKey(
                    // 这些段落仅与心理健康状态相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getTranslationKey()
            );
            case OPENING, CLOSING, GENERAL -> composeTranslationKey(
                    // 这些段落与心理健康状态和感受相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getTranslationKey(),
                    getFeeling().getTranslationKey()
            );
            case TOPIC -> {
                String topicTranslationKey = findTopicTranslationKey();
                if (topicTranslationKey == null || topicTranslationKey.isEmpty()) {
                    yield "";
                }
                yield  composeTranslationKey(
                    // 话题段落与心理健康状态、话题内容和感受相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getTranslationKey(),
                    topicTranslationKey,
                    getFeeling().getTranslationKey()
                );
            }
            case WEATHER -> composeTranslationKey(
                    // 天气话题与心理健康状态、话题内容和感受相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getTranslationKey(),
                    getTopicProducer().getWeatherTopicAttribute().getTranslationKey(),
                    getFeeling().getTranslationKey()
            );
        };
        if (result == null || result.isEmpty()) return "";
        if (!enableWarp) return result;
        return StringUtil.warp(result);
    }

    /**
     * 从话题生产器中获取下一个话题的translationKey并返回。
     * @see TopicProducer#pollNextEssential()
     * @return 话题的translationKey，获取失败返回空字符串
     */
    private String findTopicTranslationKey() {
        if (getTopicProducer().isEmpty()) {
            LOGGER.debug("TopicProducer is empty when trying to find topic translation key.");
            return "";
        }
        ContextAttribute contextAttribute = getTopicProducer().pollNextEssential();
        if (contextAttribute == null) {
            LOGGER.debug("Polled ContextAttribute is null when trying to find topic translation key.");
            return "";
        }

        return contextAttribute.getTranslationKey();
    }

    /**
     * 在指定的变体数量内随机选取一个已定义的变体translationKey
     * @see StringUtil#findTranslationKeyVariant(String, int, Random, Collection)
     */
    public String findRandomVariant(String translationKey, int variantCount) {
        return StringUtil.findTranslationKeyVariant(translationKey, variantCount, RANDOM, getUsedTranslationKeys());
    }

    public TopicProducer getTopicProducer() {
        return topicProducer;
    }

    public Feeling getFeeling() {
        return feeling;
    }

    public boolean isHasCured() {
        return hasCured;
    }

    public boolean isHasWorsened() {
        return hasWorsened;
    }

    public MentalHealthStatus getMentalHealthStatus() {
        return mentalHealthStatus;
    }

    public List<String> getUsedTranslationKeys() {
        return usedTranslationKeys;
    }
}
