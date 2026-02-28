package grainalcohol.dtt.mixin.modification;

import com.bawnorton.mixinsquared.TargetHandler;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JukeboxBlockEntity.class, priority = 1500)
public class JukeboxBlockEntityMixinMixin {
    @TargetHandler(
            mixin = "net.depression.mixin.emotion.JukeboxBlockEntityMixin",
            name = "tick"
    )
    @ModifyConstant(
            method = "@MixinSquared:Handler",
            constant = @Constant(longValue = 20L)
    )
    private long modifyJukeboxMentalHealInterval(long constant) {
        return DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyJukeboxIntervalTicks;
    }

    @TargetHandler(
            mixin = "net.depression.mixin.emotion.JukeboxBlockEntityMixin",
            name = "tick"
    )
    @Inject(
            method = "@MixinSquared:Handler",
            at = @At("HEAD")
    )
    private void injectJukeboxTick(World level, BlockPos blockPos, BlockState blockState, CallbackInfo originalCi, CallbackInfo ci) {
        if (DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearbyJukeboxMode != ServerConfig.NearbyAnythingHealMode.EVERYONE) {
            ci.cancel();
        }
    }
}
