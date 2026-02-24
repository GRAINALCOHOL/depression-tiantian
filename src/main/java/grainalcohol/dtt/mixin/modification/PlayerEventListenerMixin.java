package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.sugar.Local;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.listener.PlayerEventListener;
import net.depression.mental.MentalStatus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEventListener.class)
public class PlayerEventListenerMixin {
    @Inject(
            method = "onAttackEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/mental/MentalIllness;trigMentalFatigue()V"
            )
    )
    private static void beforeTrigMentalFatigue(PlayerEntity player, World level, Entity entity, Hand hand, EntityHitResult entityHitResult, CallbackInfoReturnable<EventResult> cir, @Local(name = "mentalStatus")MentalStatus mentalStatus) {
        if (!DTTConfig.getInstance().getServerConfig().combat_config.easier_combat_state) {
            return;
        }

        if (mentalStatus.emotionValue < -2.0) {
            mentalStatus.combatCountdown = 10;
        }
    }
}
