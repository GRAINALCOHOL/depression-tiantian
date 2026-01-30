package grainalcohol.dtt.init;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.effect.AnorexiaEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class DTTStatusEffect {
    public static final StatusEffect ANOREXIA = new AnorexiaEffect();

    public static void init() {
        Registry.register(Registries.STATUS_EFFECT, DTTMod.id("anorexia"), ANOREXIA);
    }
}
