package grainalcohol.dtt.diary.topic.v2;

import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.*;

public class TopicManager {
    private static final Map<UUID, List<EssentialTopic>> historicalEssentialTopicsMap = new HashMap<>();
    public static final String HISTORICAL_ESSENTIAL_TOPICS_KEY = "HistoricalEssentialTopics";

    public static void recordTopic(UUID playerUUID, EssentialTopic topic) {
        historicalEssentialTopicsMap.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(topic);
    }

    public static List<EssentialTopic> getHistoricalTopics(UUID playerUUID) {
        historicalEssentialTopicsMap.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        return historicalEssentialTopicsMap.get(playerUUID);
    }

    public static void writeToNbt(UUID playerUUID, NbtCompound nbt) {
        List<EssentialTopic> historicalEssentialTopics = getHistoricalTopics(playerUUID);
        if (historicalEssentialTopics.isEmpty()) return;

        NbtList nbtList = new NbtList();
        for (EssentialTopic essentialTopic: historicalEssentialTopics) {
            nbtList.add(NbtString.of(essentialTopic.getIdentifier().toString()));
        }
        nbt.put(HISTORICAL_ESSENTIAL_TOPICS_KEY, nbtList);
    }

    public static void readFromNbt(UUID playerUUID, NbtCompound nbt) {
        if (!nbt.contains(HISTORICAL_ESSENTIAL_TOPICS_KEY)) return;

        NbtList nbtList = nbt.getList(HISTORICAL_ESSENTIAL_TOPICS_KEY, NbtElement.STRING_TYPE);
        List<EssentialTopic> toAddList = new ArrayList<>();
        for (NbtElement nbtElement : nbtList) {
            String idStr = nbtElement.asString();
            Identifier identifier = Identifier.tryParse(idStr);
            if (identifier == null) continue;

            EssentialTopic essentialTopic = DTTRegistries.ESSENTIAL_TOPIC_REGISTRY.get(identifier);
            if (essentialTopic != null) {
                toAddList.add(essentialTopic);
            }
        }
        historicalEssentialTopicsMap.put(playerUUID, toAddList);
    }
}
