package grainalcohol.dtt.diary.topic;

import grainalcohol.dtt.api.wrapper.MentalHealthStatus;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class TopicProducer {
    private static final Random RANDOM = new Random();
    private final ServerPlayerEntity player;
    private final List<Topic> allTopicList;
    private final Map<TopicType, List<Topic>> topicTypeMap;

    /**
     * TODO：2025-12-06<br>
     * 1. 对player处理所有可能的话题{@code topicProcess}，自动分类存储<br>
     * 2. 对于特殊话题（非{@code TopicType.STAT}的），根据健康状态选取2~4个组装translation key，使用后剔除（有天气话题立刻处理天气话题）<br>
     * 3. 用于后续总结感受关键词{@code Feeling}<br>
     * 4. 根据健康状态分别选取2~4个关键词组装translation key，使用后剔除<br>
     * 5. 组装逻辑位于持有此依赖的{@code DiaryContentHandler}，此类需要自动剔除低质量话题，躁狂状态剔除标准降低<br>
     */

    public TopicProducer(ServerPlayerEntity player) {
        this.player = player;
        this.allTopicList = new ArrayList<>();
        this.topicTypeMap = new EnumMap<>(TopicType.class);

        for (TopicType type : TopicType.values()) {
            topicTypeMap.put(type, new ArrayList<>());
        }

        initTopicListProcess(); // 初始化+处理话题列表
    }

    private void initTopicListProcess() {
        // 生成话题
        generateTopicProcess();

        // 分类话题
        for (Topic topic : getAllTopicList()) {
            getTopicTypeMap().get(topic.getType()).add(topic);
        }

        // 筛选 & 剔除话题
        limitFinalTopicList();
    }

    private void limitFinalTopicList() {
        // Essential（max：4）
        List<Topic> tempEssential = new ArrayList<>(getTopicTypeMap().get(TopicType.ESSENTIAL)); // 副本防止数据污染
        int countEssential = Math.min(4, tempEssential.size());  // 最多选择4个，这里第一个永远是那个weather
        getTopicTypeMap().put(TopicType.ESSENTIAL, tempEssential.subList(0, countEssential));

        // Stat（max：6）
        List<Topic> tempStat = new ArrayList<>(getTopicTypeMap().get(TopicType.STAT)); // 副本防止数据污染
        tempStat.sort(Comparator.comparing(topic -> Math.abs(topic.getWeight()))); // 根据权重排序
        int countStat = Math.min(6, tempStat.size());  // 最多选择6个
        getTopicTypeMap().put(TopicType.STAT, tempStat.subList(0, countStat));

        // MajorImpact（max：4）
        List<Topic> tempMajorImpact = new ArrayList<>(getTopicTypeMap().get(TopicType.MAJOR_IMPACT)); // 副本防止数据污染
        int countMajorImpact = Math.min(4, tempMajorImpact.size());  // 最多选择4个
        List<Topic> result = tempMajorImpact.subList(0, countMajorImpact);
        getTopicTypeMap().put(TopicType.MAJOR_IMPACT, result);
        // 这里会覆盖存储被采纳的话题列表，生成了就意味着需要存储以供下次使用
        TopicManager.putHistoricalMajorImpactTopicList(player.getUuid(), result);
    }

    // TODO：改成注册表模式方便扩展，还有优先级设置
    private void generateTopicProcess() {
        // essential
        addTopic(handleTopic(Topics.weather));
        addTopic(handleTopic(Topics.petDied));
        addTopic(handleTopic(Topics.petBred));
        // stat
        addTopic(handleTopic(Topics.animalBred));
        addTopic(handleTopic(Topics.monsterKilled));
        addTopic(handleTopic(Topics.distanceMoved));
        addTopic(handleTopic(Topics.damageTaken));
        addTopic(handleTopic(Topics.traded));
        addTopic(handleTopic(Topics.brewed));
        addTopic(handleTopic(Topics.flowerPotted));
        addTopic(handleTopic(Topics.ate));
        // major impact
        addTopic(handleTopic(Topics.enderDragonKilled));
        addTopic(handleTopic(Topics.raidFailed));
        addTopic(handleTopic(Topics.raidWon));
    }

    private void addTopic(Topic targetTopic) {
        // 对major impact话题进行去重处理
        // 这里的重复指的是与上次（的重大影响类型话题）相比，重复直接丢掉不会存到今天的话题列表
        List<Topic> yesterdayMajorImpactTopicList = TopicManager.getHistoricalMajorImpactTopicList(player.getUuid()); // 只读
        MentalHealthStatus mentalHealthStatus = MentalHealthStatus.from(player);
        boolean isManic = mentalHealthStatus == MentalHealthStatus.MANIC_PHASE;

        if (targetTopic == null) {
            return;
        }

        if (yesterdayMajorImpactTopicList.isEmpty()) {
            getAllTopicList().add(targetTopic);
            return;
        }

        // 去重规则
        boolean willBeAdded = true;
        for (Topic statTopic : yesterdayMajorImpactTopicList) {
            boolean shouldAvoidRepetition = (
                    (targetTopic.getType().equals(statTopic.getType())) && // 类型相同
                            (targetTopic.getName().equals(statTopic.getName())) && // 内容相同
                            (targetTopic.shouldAvoidRepetition() || statTopic.shouldAvoidRepetition()) // 标记为避免重复
            );

            if (shouldAvoidRepetition) {
                willBeAdded = false;
                if (isManic && RANDOM.nextDouble() < 0.4) {
                    // 躁狂状态有40%的概率重复添加话题
                    willBeAdded = true;
                }
                // 避免不必要的循环
                break;
            }

        }

        if (willBeAdded) {
            // 先全存起来，后面再分类
            getAllTopicList().add(targetTopic);
        }
    }

    @Nullable
    private Topic handleTopic(Function<ServerPlayerEntity, Topic> function) {
        // 根据 Function 生成话题
        return function.apply(player);
    }

    private List<Topic> getAllTopicList() {
        return allTopicList;
    }

    public Map<TopicType, List<Topic>> getTopicTypeMap() {
        return topicTypeMap;
    }

    public List<Topic> getTopicTypeList(TopicType topicType) {
        return getTopicTypeMap().get(topicType);
    }
}
