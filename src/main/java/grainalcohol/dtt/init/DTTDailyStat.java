package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.diary.dailystat.v2.DailyStatKey;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.registry.Registry;

public class DTTDailyStat {
    // number stat keys
    public static final DailyStatKey<Integer> TRADED_COUNT = DailyStatKey.createNumberKey(DTTMod.id("traded_count"));
    public static final DailyStatKey<Integer> DISTANCE_MOVED = DailyStatKey.createNumberKey(DTTMod.id("distance_moved"));
    public static final DailyStatKey<Integer> DAMAGE_TAKEN = DailyStatKey.createNumberKey(DTTMod.id("damage_taken"));
    public static final DailyStatKey<Integer> MONSTER_KILLED = DailyStatKey.createNumberKey(DTTMod.id("monster_killed"));
    public static final DailyStatKey<Integer> POTION_BREWED = DailyStatKey.createNumberKey(DTTMod.id("potion_brewed"));
    public static final DailyStatKey<Integer> ANIMAL_BRED = DailyStatKey.createNumberKey(DTTMod.id("animal_bred"));
    // boolean stat keys
    public static final DailyStatKey<Boolean> ATE = DailyStatKey.createBooleanKey(DTTMod.id("ate"));
    public static final DailyStatKey<Boolean> PET_BRED = DailyStatKey.createBooleanKey(DTTMod.id("pet_bred"));
    public static final DailyStatKey<Boolean> PET_DIED = DailyStatKey.createBooleanKey(DTTMod.id("pet_died"));
    public static final DailyStatKey<Boolean> FLOWER_POTTED = DailyStatKey.createBooleanKey(DTTMod.id("flower_potted"));
    public static final DailyStatKey<Boolean> RAINED = DailyStatKey.createBooleanKey(DTTMod.id("rained"));
    public static final DailyStatKey<Boolean> ENDER_DRAGON_KILLED = DailyStatKey.createBooleanKey(DTTMod.id("ender_dragon_killed"));
    public static final DailyStatKey<Boolean> RAID_WON = DailyStatKey.createBooleanKey(DTTMod.id("raid_won"));
    public static final DailyStatKey<Boolean> RAID_FAILED = DailyStatKey.createBooleanKey(DTTMod.id("raid_failed"));
    public static final DailyStatKey<Boolean> DIED = DailyStatKey.createBooleanKey(DTTMod.id("died"));
    // mental (boolean) stat keys
    public static final DailyStatKey<Boolean> CURED = DailyStatKey.createBooleanKey(DTTMod.id("cured"));
    public static final DailyStatKey<Boolean> WORSENED = DailyStatKey.createBooleanKey(DTTMod.id("worsened"));

    private static void registerBooleanStatKey(DailyStatKey<Boolean> key) {
        Registry.register(DTTRegistries.BOOLEAN_STAT_KEY_REGISTRY, key.getIdentifier(), key);
    }

    private static void registerNumberStatKey(DailyStatKey<Integer> key) {
        Registry.register(DTTRegistries.NUMBER_STAT_KEY_REGISTRY, key.getIdentifier(), key);
    }

    public static void init() {
        registerNumberStatKey(TRADED_COUNT);
        registerNumberStatKey(DISTANCE_MOVED);
        registerNumberStatKey(DAMAGE_TAKEN);
        registerNumberStatKey(MONSTER_KILLED);
        registerNumberStatKey(POTION_BREWED);
        registerNumberStatKey(ANIMAL_BRED);

        registerBooleanStatKey(ATE);
        registerBooleanStatKey(PET_BRED);
        registerBooleanStatKey(PET_DIED);
        registerBooleanStatKey(FLOWER_POTTED);
        registerBooleanStatKey(RAINED);
        registerBooleanStatKey(ENDER_DRAGON_KILLED);
        registerBooleanStatKey(RAID_WON);
        registerBooleanStatKey(RAID_FAILED);
        registerBooleanStatKey(DIED);
        registerBooleanStatKey(CURED);
        registerBooleanStatKey(WORSENED);
    }
}
