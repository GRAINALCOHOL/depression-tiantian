package grainalcohol.dtt.diary;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.diary.feeling.Feeling;
import grainalcohol.dtt.diary.feeling.FeelingProducer;
import grainalcohol.dtt.diary.topic.Topic;
import grainalcohol.dtt.diary.topic.TopicProducer;
import grainalcohol.dtt.diary.topic.TopicType;
import grainalcohol.dtt.mental.MentalHealthStatus;
import grainalcohol.dtt.util.MathUtil;
import grainalcohol.dtt.util.StringUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// TODO: 考虑昨日感受对今日的影响
public class DiaryContentHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiaryContentHandler.class);
    private static final Random RANDOM = new Random();
    private final MentalHealthStatus mentalHealthStatus;
    private final TopicProducer topicProducer;

    private final Feeling feeling;
    private final boolean hasCured; // 康复
    private final boolean hasWorsened; // 恶化

    private final Queue<Topic> essentialTopicQueue;
    private final Queue<Topic> majorImpactTopicQueue;

    private final List<String> usedTranslationKeys;

    public DiaryContentHandler(ServerPlayerEntity player) {
        this.mentalHealthStatus = MentalHealthStatus.from(player);
        this.topicProducer = new TopicProducer(player);

        this.feeling = new FeelingProducer(getMentalHealthStatus(), getTopicProducer()).getResult();

        this.hasCured = DailyStatManager.getTodayResult(player.getUuid(), DailyStat::isHasCured);
        this.hasWorsened = DailyStatManager.getTodayResult(player.getUuid(), DailyStat::isHasWorsened);

        this.essentialTopicQueue = new LinkedList<>(getTopicProducer().getTopicTypeList(TopicType.ESSENTIAL));
        this.majorImpactTopicQueue = new LinkedList<>(getTopicProducer().getTopicTypeList(TopicType.MAJOR_IMPACT));

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
            mentalHealthChangeKey = generateTranslationKey(DiaryParagraph.HAS_CURED, variantCount);
        }
        if (isHasWorsened()) {
            mentalHealthChangeKey = generateTranslationKey(DiaryParagraph.HAS_WORSENED, variantCount);
        }

        String manicInsertionKey = generateTranslationKey(DiaryParagraph.MANIC_INSERTION, variantCount);

        return // 开头段落
                "    " + generateTranslationKey(DiaryParagraph.OPENING, variantCount)

                        // 正文段落
                        + "\n    " + generateTranslationKey(DiaryParagraph.BODY_ESSENTIAL, variantCount) // 话题1，也就是天气话题（通常来说不会为空）
                        + (!(isHasCured() && isHasWorsened()) ? mentalHealthChangeKey : "") // 一天内同时出现康复与恶化则不记录
                        + generateTranslationKey(DiaryParagraph.BODY_ESSENTIAL, variantCount) // 话题2
                        + "\n    " + generateTranslationKey(DiaryParagraph.BODY_NORMAL, variantCount) // 通用文案
                        + generateTranslationKey(DiaryParagraph.BODY_MAJOR_IMPACT, variantCount) // 话题3
                        + (shouldAppendManicContent() ? manicInsertionKey : "") // 躁狂时额外的内容，相当于另外一段通用文案

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
     * 生成指定段落的translationKey，{@link DiaryParagraph#BODY_ESSENTIAL}和{@link DiaryParagraph#BODY_MAJOR_IMPACT}会消费指定话题队列中的话题<br>
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
     * 生成指定段落的translationKey，{@link DiaryParagraph#BODY_ESSENTIAL}和{@link DiaryParagraph#BODY_MAJOR_IMPACT}会消费指定话题队列中的话题<br>
     * @param paragraph 指定的段落
     * @return 生成的translationKey，会使用单引号包装，生成失败返回空字符串
     */
    public String generateTranslationKey(DiaryParagraph paragraph) {
        return generateTranslationKey(paragraph, true);
    }

    /**
     * 生成指定段落的translationKey，{@link DiaryParagraph#BODY_ESSENTIAL}和{@link DiaryParagraph#BODY_MAJOR_IMPACT}会消费指定话题队列中的话题<br>
     * @param paragraph 指定的段落
     * @param enableWarp 是否使用单引号包装首尾（原因是depression客户端以单引号为边界处理本地化）
     * @return 生成的translationKey，生成失败返回空字符串
     */
    public String generateTranslationKey(DiaryParagraph paragraph, boolean enableWarp) {
        boolean gentleMode = DTTConfig.getInstance().getServerConfig().diaryConfig.gentle_mode;
        String result = switch (paragraph) {
            case MANIC_INSERTION -> composeTranslationKey(
                    // 躁狂额外内容与感受相关
                    getPrefixTranslationKey(),
                    paragraph.getName(),
                    getFeeling().getName()
            );
            case NO_DIARY, HAS_CURED, HAS_WORSENED -> composeTranslationKey(
                    // 这些段落与心理健康状态相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getName()
            );
            case OPENING, CLOSING, BODY_NORMAL -> composeTranslationKey(
                    // 这些段落与心理健康状态和感受相关
                    getPrefixTranslationKey(),
                    getMentalHealthStatus().getName(),
                    paragraph.getName(),
                    getFeeling().getName()
            );
            case BODY_ESSENTIAL -> {
                if (getEssentialTopicQueue().isEmpty()) {
                    LOGGER.warn("Essential topics queue is empty, returning empty string");
                    yield "";
                }

                Topic topic = getEssentialTopicQueue().poll();
                if (topic == null) {
                    LOGGER.warn("Polled essential topic is null, returning empty string");
                    yield "";
                }
                if (gentleMode && topic.isShouldExcludedDueToNegativity()) {
                    LOGGER.info("Skipping essential topic '{}' due to negativity, polling next topic", topic.getName());
                    yield generateTranslationKey(paragraph, enableWarp);
                }
                yield composeTranslationKey(
                        // 重要话题段落与心理健康状态、话题内容和感受相关
                        getPrefixTranslationKey(),
                        getMentalHealthStatus().getName(),
                        paragraph.getName(),
                        topic.getName(),
                        getFeeling().getName()
                );
            }
            case BODY_MAJOR_IMPACT -> {
                if (getMajorImpactTopicQueue().isEmpty()) {
                    LOGGER.warn("Major impact topics queue is empty, returning empty string");
                    yield "";
                }

                Topic topic = getMajorImpactTopicQueue().poll();
                if (topic == null) {
                    LOGGER.warn("Polled major impact topic is null, returning empty string");
                    yield "";
                }
                if (gentleMode && topic.isShouldExcludedDueToNegativity()) {
                    LOGGER.info("Skipping major impact topic '{}' due to negativity, polling next topic", topic.getName());
                    yield generateTranslationKey(paragraph, enableWarp);
                }
                yield composeTranslationKey(
                        // 重大影响话题段落与心理健康状态、话题内容和感受相关
                        getPrefixTranslationKey(),
                        getMentalHealthStatus().getName(),
                        paragraph.getName(),
                        topic.getName(),
                        getFeeling().getName()
                );
            }
        };
        if (result == null || result.isEmpty()) return "";
        if (!enableWarp) return result;
        return StringUtil.warp(result);
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

    public Queue<Topic> getEssentialTopicQueue() {
        return essentialTopicQueue;
    }

    public Queue<Topic> getMajorImpactTopicQueue() {
        return majorImpactTopicQueue;
    }

    public List<String> getUsedTranslationKeys() {
        return usedTranslationKeys;
    }
}
