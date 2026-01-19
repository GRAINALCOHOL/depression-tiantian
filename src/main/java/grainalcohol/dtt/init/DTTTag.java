package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class DTTTag {
    public static final TagKey<Item> MENTAL_HEAL_FOODS = TagKey.of(RegistryKeys.ITEM, DTTMod.id("mental_heal_foods"));
}
