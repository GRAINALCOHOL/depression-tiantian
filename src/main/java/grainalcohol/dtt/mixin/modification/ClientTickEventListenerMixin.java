package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.config.DTTConfig;
import net.depression.listener.client.ClientTickEventListener;
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
    private static boolean modifyMentalTraitSelected(Operation<Boolean> original) {
        return DTTConfig.getInstance().getServerConfig().disableMentalTraitSelectScreen;
    }
}
