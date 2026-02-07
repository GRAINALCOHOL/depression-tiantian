package grainalcohol.dtt.network;

import grainalcohol.dtt.api.event.MentalIllnessEvent;
import grainalcohol.dtt.api.event.SymptomEvent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenEyesEventPacket {
    public OpenEyesEventPacket() {}

    // 编码
    public void encode(PacketByteBuf buf) {}

    // 解码
    public OpenEyesEventPacket(PacketByteBuf buf) {}

    // 应用
    public void apply(java.util.function.Supplier<dev.architectury.networking.NetworkManager.PacketContext> context) {
        if (context.get().getPlayer() instanceof ServerPlayerEntity serverPlayerEntity) {
            SymptomEvent.OPEN_EYES_EVENT.invoker().onOpenEyes(serverPlayerEntity);
        }
    }
}
