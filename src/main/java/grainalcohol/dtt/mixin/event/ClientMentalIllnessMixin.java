package grainalcohol.dtt.mixin.event;

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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientMentalIllness.class)
public class ClientMentalIllnessMixin {
    @Shadow public boolean isCloseEye;

    @Redirect(
            method = "receiveCloseEyePacket",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/depression/client/ClientMentalIllness;startCloseEyeTime:J",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void redirectSetStartCloseEyeTime(ClientMentalIllness instance, long originValue) {
        // 根据配置文件设置延迟时间
        int delayTicks = DTTConfig.getInstance().getClientConfig().closeEyeDelayTicks;
        if (MinecraftClient.getInstance().world != null) {
            instance.startCloseEyeTime = MinecraftClient.getInstance().world.getTime() + delayTicks;
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
