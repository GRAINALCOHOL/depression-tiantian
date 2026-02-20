package grainalcohol.dtt.diary.topic.v2;

import grainalcohol.dtt.init.DTTTopic;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TopicProducer {
    private final Logger LOGGER = LoggerFactory.getLogger(TopicProducer.class);
    private final boolean gentleMode;
    private final ServerPlayerEntity player;
    private final List<EssentialTopic> historicalEssentialTopics;

    // 用于总结感受关键词
    private final List<ContextAttribute> allContextAttributes = new ArrayList<>();

    // 用于日记生成
    private final ContextAttribute WEATHER_TOPIC_ATTRIBUTE;
    private final Queue<ContextAttribute> essentialContextAttributes = new ArrayDeque<>();

    public TopicProducer(ServerPlayerEntity player, boolean gentleMode) {
        this.gentleMode = gentleMode;
        this.player = player;
        this.historicalEssentialTopics = TopicManager.getHistoricalTopics(player.getUuid());
        ContextAttribute weatherAttribute = DTTTopic.WEATHER_TOPIC.getAttribute(player, gentleMode)
                .orElseThrow(() -> new IllegalStateException("Weather topic context attribute not found for player " + player.getName()));
        this.allContextAttributes.add(weatherAttribute);
        this.WEATHER_TOPIC_ATTRIBUTE = weatherAttribute;

        initTopics();
    }

    private void initTopics() {
        // stat topics
        // 不去重，不剔除，用于总结感受关键词
        DTTRegistries.STAT_TOPIC_REGISTRY.stream()
                .map(statTopic -> statTopic.getAttribute(player, gentleMode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(allContextAttributes::add);

        // essential topics
        // 用于日记生成，剔除与昨天重复和极端负面的（根据配置）
        List<ContextAttribute> allEssentialTopics = DTTRegistries.ESSENTIAL_TOPIC_REGISTRY.stream()
                .filter(essentialTopic -> !shouldExcludeDueToRepetitive(essentialTopic))
                .map(essentialTopic -> essentialTopic.getAttribute(player, gentleMode))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(contextAttribute -> !shouldExcludeDueToNegativity(contextAttribute))
                .peek(allContextAttributes::add)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(allEssentialTopics);
        allContextAttributes.addAll(allEssentialTopics);
    }

    /**
     * 根据昨天的历史话题来判断这个话题是否应该被视为重复。如果这个话题配置了shouldAvoidRepetitionFromYesterday字段，
     * 并且昨天的历史话题中存在相同标识符的话题，那么就认为它是重复的。
     * @param essentialTopic 要检查的话题
     * @return true表示应该剔除，false表示不应该剔除
     */
    private boolean shouldExcludeDueToRepetitive(EssentialTopic essentialTopic) {
        if (!essentialTopic.shouldAvoidRepetitionFromYesterday()) {
            return false;
        }

        for (EssentialTopic yesterdayEssentialTopic : getHistoricalEssentialTopics()) {
            if (yesterdayEssentialTopic.getIdentifier().equals(essentialTopic.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据配置和属性的极端负面性来决定是否剔除这个属性。
     * @param contextAttribute 要检查的上下文属性
     * @return true表示应该剔除，false表示不应该剔除
     */
    private boolean shouldExcludeDueToNegativity(ContextAttribute contextAttribute) {
        return gentleMode && contextAttribute.isExtremeNegativity();
    }

    /**
     * 用于组装translationKey，获取下一个话题的上下文属性，可能返回null。
     * @return 下一个话题的上下文属性，如果没有更多话题可用，则返回null
     */
    @Nullable
    public ContextAttribute pollNextEssential() {
        if (isEmpty()) {
            LOGGER.debug("No more essential topics available for player {}, returning null", player.getName());
            return null;
        }
        ContextAttribute attribute = getEssentialContextAttributes().poll();
        if (attribute == null) {
            LOGGER.debug("Essential context attribute is null for player {}, returning null", player.getName());
            return null;
        }
        TopicManager.recordTopic(player.getUuid(), DTTRegistries.ESSENTIAL_TOPIC_REGISTRY.get(attribute.getSourceTopicIdentifier()));
        return attribute;
    }

    public boolean isEmpty() {
        return getEssentialContextAttributes().isEmpty();
    }

    public ContextAttribute getWeatherTopicAttribute() {
        return WEATHER_TOPIC_ATTRIBUTE;
    }

    public List<ContextAttribute> getAllContextAttributes() {
        return allContextAttributes;
    }

    public Queue<ContextAttribute> getEssentialContextAttributes() {
        return essentialContextAttributes;
    }

    public List<EssentialTopic> getHistoricalEssentialTopics() {
        return historicalEssentialTopics;
    }
}
