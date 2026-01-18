package grainalcohol.dtt.mixin;

import dev.architectury.networking.NetworkManager;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.DiaryContentHandler;
import io.netty.buffer.Unpooled;
import net.depression.mental.MentalStatus;
import net.depression.network.DiaryUpdatePacket;
import net.depression.server.Registry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiaryUpdatePacket.class)
public class DiaryUpdatePacketMixin {
    @Inject(method = "sendToPlayer", at = @At("HEAD"), cancellable = true)
    private static void sendToPlayer(ServerPlayerEntity player, CallbackInfo ci) {
        if (!DTTConfig.getInstance().getServerConfig().useEnhancedDiaryGenerator) {
            return;
        }

        PacketByteBuf buf = new PacketByteBuf (Unpooled.buffer());
        DiaryContentHandler diaryContentHandler = new DiaryContentHandler(player);

        MentalStatus mentalStatus = Registry.mentalStatus.get(player.getUuid());
        if (mentalStatus == null) {
            Registry.mentalStatus.put(player.getUuid(), new MentalStatus(player));
        }

        buf.writeCharSequence(diaryContentHandler.completeTranslationKeyProcess(), DiaryUpdatePacket.charset);
        NetworkManager.sendToPlayer(player, DiaryUpdatePacket.DIARY_UPDATE_PACKET, buf);

        ci.cancel();
    }
}