package grainalcohol.dtt.registry;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatKey;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class DTTRegistries {
    public static final RegistryKey<Registry<DailyStatKey<Boolean>>> BOOLEAN_STAT_KEY_REGISTRYKEY = RegistryKey.ofRegistry(DTTMod.id("boolean_stat_key"));
    public static final RegistryKey<Registry<DailyStatKey<Integer>>> NUMBER_STAT_KEY_REGISTRYKEY = RegistryKey.ofRegistry(DTTMod.id("number_stat_key"));
    public static final RegistryKey<Registry<EssentialTopic>> ESSENTIAL_TOPIC_REGISTRYKEY = RegistryKey.ofRegistry(DTTMod.id("essential_topic"));
    public static final RegistryKey<Registry<StatTopic>> STAT_TOPIC_REGISTRYKEY = RegistryKey.ofRegistry(DTTMod.id("stat_topic"));

    public static final Registry<DailyStatKey<Boolean>> BOOLEAN_STAT_KEY_REGISTRY;
    public static final Registry<DailyStatKey<Integer>> NUMBER_STAT_KEY_REGISTRY;
    public static final Registry<EssentialTopic> ESSENTIAL_TOPIC_REGISTRY;
    public static final Registry<StatTopic> STAT_TOPIC_REGISTRY;

    static {
        BOOLEAN_STAT_KEY_REGISTRY = FabricRegistryBuilder.createSimple(BOOLEAN_STAT_KEY_REGISTRYKEY).buildAndRegister();
        NUMBER_STAT_KEY_REGISTRY = FabricRegistryBuilder.createSimple(NUMBER_STAT_KEY_REGISTRYKEY).buildAndRegister();
        ESSENTIAL_TOPIC_REGISTRY = FabricRegistryBuilder.createSimple(ESSENTIAL_TOPIC_REGISTRYKEY).buildAndRegister();
        STAT_TOPIC_REGISTRY = FabricRegistryBuilder.createSimple(STAT_TOPIC_REGISTRYKEY).buildAndRegister();
    }
}
