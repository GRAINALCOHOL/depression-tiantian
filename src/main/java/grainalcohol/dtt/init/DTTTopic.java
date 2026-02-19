package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.diary.topic.v2.topics.EnderDragonKilledTopic;
import grainalcohol.dtt.diary.topic.v2.topics.MonsterKilledTopic;
import grainalcohol.dtt.diary.topic.v2.topics.WeatherTopic;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DTTTopic {
    // 天气话题不注册，需要特殊处理
    public static final WeatherTopic WEATHER_TOPIC = new WeatherTopic(DTTMod.id("weather"));
    // 统计类话题
    public static final StatTopic MONSTER_KILL = new MonsterKilledTopic(DTTMod.id("monster_killed"));
    // 重要话题
    public static final EssentialTopic ENDER_DRAGON_KILLED = new EnderDragonKilledTopic(DTTMod.id("ender_dragon_killed"));

    private static void registerStat(Identifier identifier, StatTopic statTopic) {
        Registry.register(DTTRegistries.STAT_TOPIC_REGISTRY, identifier, statTopic);
    }

    private static void registerEssential(Identifier identifier, EssentialTopic essentialTopic) {
        Registry.register(DTTRegistries.ESSENTIAL_TOPIC_REGISTRY, identifier, essentialTopic);
    }

    public static void init() {
        registerStat(MONSTER_KILL.getIdentifier(), MONSTER_KILL);
        registerEssential(ENDER_DRAGON_KILLED.getIdentifier(), ENDER_DRAGON_KILLED);
    }
}
