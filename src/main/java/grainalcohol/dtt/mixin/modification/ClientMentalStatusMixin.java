package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.depression.client.ClientMentalIllness;
import net.depression.client.ClientMentalStatus;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientMentalStatus.class)
public class ClientMentalStatusMixin {
    @Shadow
    public ClientMentalIllness mentalIllness;

    @Redirect(
            method = "receiveMentalHealthPacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"
            )
    )
    private void onReceiveMentalHealthPacket(InGameHud instance, Text message, boolean tinted) {
        // 只是尝试一下
        instance.getChatHud().addMessage(message);
    }

    // 创造模式似乎不会让玩家进入战斗状态
    // 使用指令修改情绪值不会立刻更新属性修饰

    @Inject(method = "renderHud", at = @At("TAIL"))
    private void onRenderHud(DrawContext context, float v, CallbackInfo ci) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity player = minecraftClient.player;

        // 创造模式也渲染患病效果
        if (player != null && player.isCreative()) {
            Window window = minecraftClient.getWindow();
            this.mentalIllness.render(context, window.getScaledWidth(), window.getScaledHeight());
        }
    }
}
