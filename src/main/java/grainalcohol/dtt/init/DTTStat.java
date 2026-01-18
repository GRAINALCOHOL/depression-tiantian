package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;

public class DTTStat {
    public static final Identifier MONSTER_KILLED = DTTMod.id("monster_killed");
    public static final Identifier PET_BRED = DTTMod.id("pet_bred");
    public static final Identifier ANIMAL_TAMED = DTTMod.id("animal_tamed");
    public static final Identifier RAID_LOSS = DTTMod.id("raid_loss");

    private static void register(Identifier resourceLocation, StatFormatter formatter) {
        Registry.register(Registries.CUSTOM_STAT, resourceLocation.getPath(), resourceLocation);
        Stats.CUSTOM.getOrCreateStat(resourceLocation, formatter);
    }

    public static void init() {
        register(MONSTER_KILLED, StatFormatter.DEFAULT);
        register(PET_BRED, StatFormatter.DEFAULT);
        register(ANIMAL_TAMED, StatFormatter.DEFAULT);
        register(RAID_LOSS, StatFormatter.DEFAULT);
    }
}
