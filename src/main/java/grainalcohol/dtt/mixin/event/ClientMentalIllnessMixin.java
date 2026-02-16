package grainalcohol.dtt.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.init.DTTNetwork;
import grainalcohol.dtt.network.OpenEyesEventPacket;
import net.depression.client.ClientMentalIllness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientMentalIllness.class)
public class ClientMentalIllnessMixin {
    @Shadow public boolean isCloseEye;

    @WrapOperation(
            method = "receiveCloseEyePacket",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/depression/client/ClientMentalIllness;startCloseEyeTime:J",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void onSetStartCloseEyeTime(ClientMentalIllness instance, long originValue, Operation<Void> original) {
        // 根据配置文件设置延迟时间
        int delayTicks = DTTConfig.getInstance().getClientConfig().visualConfig.close_eye_delay_ticks;
        if (MinecraftClient.getInstance().world != null) {
            if (delayTicks <= 0) {
                delayTicks = 60;
            }
            original.call(instance, MinecraftClient.getInstance().world.getTime() + delayTicks);
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/depression/client/ClientMentalIllness;isCloseEye:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void onOpenEyes(DrawContext context, int x, int y, CallbackInfo ci) {
        // 赋值为false后表示开始睁眼动作
        if (!isCloseEye) DTTNetwork.CHANNEL.sendToServer(new OpenEyesEventPacket());
    }
}
