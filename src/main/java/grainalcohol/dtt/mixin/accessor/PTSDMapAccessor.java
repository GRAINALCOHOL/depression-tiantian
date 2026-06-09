package grainalcohol.dtt.mixin.accessor;

import net.depression.mental.PTSDManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.ConcurrentHashMap;

@Mixin(PTSDManager.class)
public interface PTSDMapAccessor {
    @Accessor("PTSD")
    ConcurrentHashMap<String, Double> getPTSD();
}
