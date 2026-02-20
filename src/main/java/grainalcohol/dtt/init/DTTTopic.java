package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.diary.topic.v2.topics.*;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DTTTopic {
    // 天气话题不注册，需要特殊处理
    public static final WeatherTopic WEATHER_TOPIC = new WeatherTopic(DTTMod.id("weather"));
    // 统计类话题
    public static final StatTopic DAMAGE_TAKEN = new DamageTakenTopic(DTTMod.id("damage_taken"));
    public static final StatTopic MONSTER_KILL = new MonsterKilledTopic(DTTMod.id("monster_killed"));
    public static final StatTopic ANIMAL_BRED = new AnimalBredTopic(DTTMod.id("animal_bred"));
    public static final StatTopic DISTANCE_MOVED = new DistanceMovedTopic(DTTMod.id("distance_moved"));
    public static final StatTopic TRADED = new TradedTopic(DTTMod.id("traded"));
    public static final StatTopic POTION_BREWED = new PotionBrewedTopic(DTTMod.id("potion_brewed"));
    public static final StatTopic FLOWER_POTTED = new FlowerPottedTopic(DTTMod.id("flower_potted"));
    public static final StatTopic ATE = new AteTopic(DTTMod.id("ate"));
    // 重要话题
    public static final EssentialTopic ENDER_DRAGON_KILLED = new EnderDragonKilledTopic(DTTMod.id("ender_dragon_killed"));
    public static final EssentialTopic PET = new PetTopic(DTTMod.id("pet"));
    public static final EssentialTopic RAID = new RaidTopic(DTTMod.id("raid"));

    private static void registerStat(Identifier identifier, StatTopic statTopic) {
        Registry.register(DTTRegistries.STAT_TOPIC_REGISTRY, identifier, statTopic);
    }

    private static void registerEssential(Identifier identifier, EssentialTopic essentialTopic) {
        Registry.register(DTTRegistries.ESSENTIAL_TOPIC_REGISTRY, identifier, essentialTopic);
    }

    public static void init() {
        registerStat(DAMAGE_TAKEN.getIdentifier(), DAMAGE_TAKEN);
        registerStat(MONSTER_KILL.getIdentifier(), MONSTER_KILL);
        registerStat(ANIMAL_BRED.getIdentifier(), ANIMAL_BRED);
        registerStat(DISTANCE_MOVED.getIdentifier(), DISTANCE_MOVED);
        registerStat(TRADED.getIdentifier(), TRADED);
        registerStat(POTION_BREWED.getIdentifier(), POTION_BREWED);
        registerStat(FLOWER_POTTED.getIdentifier(), FLOWER_POTTED);
        registerStat(ATE.getIdentifier(), ATE);

        registerEssential(ENDER_DRAGON_KILLED.getIdentifier(), ENDER_DRAGON_KILLED);
        registerEssential(PET.getIdentifier(), PET);
        registerEssential(RAID.getIdentifier(), RAID);
    }
}
