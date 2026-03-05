package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.client.DTTServerConfigCache;
import net.depression.listener.client.ClientTickEventListener;
import net.depression.mental.MentalStatus;
import net.depression.network.MentalTraitPacket;
import net.minecraft.client.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientTickEventListener.class)
public class ClientTickEventListenerMixin {
    @WrapOperation(
            method = "onClientLevelTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/depression/client/ClientMentalStatus;isMentalTraitSelected:Z",
                    opcode = Opcodes.GETSTATIC
            )
    )
    private static boolean disableMentalTraitSelectedScreen(Operation<Boolean> original) {
        if (DTTServerConfigCache.disableMentalTraitSelectScreen) {
            MentalTraitPacket.sendToServer(MentalStatus.DEFAULT_MENTAL_TRAIT == null ? "normal" : MentalStatus.DEFAULT_MENTAL_TRAIT);
            return true;
        }

        return original.call();
    }

    @WrapOperation(
            method = "onClientLevelTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Keyboard;onKey(JIIII)V"
            )
    )
    private static void saferCombatForCatatonicStupor(Keyboard keyboard, long window, int key, int scancode, int action, int modifiers, Operation<Void> original) {
        if (!DTTServerConfigCache.saferCatatonicStupor) {
            original.call(keyboard, window, key, scancode, action, modifiers);
        }
    }
}
