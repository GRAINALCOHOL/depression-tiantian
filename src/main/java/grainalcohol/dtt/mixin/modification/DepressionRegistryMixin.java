package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.server.Registry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Registry.class)
public class DepressionRegistryMixin {
    @WrapMethod(method = "isPending")
    private static boolean wrapIsPending(PlayerEntity player, Operation<Boolean> original) {
        return DTTConfig.getInstance().getServerConfig().disableMentalTraitSelectScreen;
    }
}
