package grainalcohol.dtt.diary.topic;

import java.util.*;

public class TopicManager {
    private static final Map<UUID, List<Topic>> historicalMajorImpactTopicListMap = new HashMap<>();

    public static List<Topic> getHistoricalMajorImpactTopicList(UUID uuid) {
        historicalMajorImpactTopicListMap.computeIfAbsent(uuid, k -> new ArrayList<>());
        return historicalMajorImpactTopicListMap.get(uuid);
    }

    public static void putHistoricalMajorImpactTopicList(UUID uuid, List<Topic> topicList) {
        historicalMajorImpactTopicListMap.put(uuid, topicList);
    }
}
