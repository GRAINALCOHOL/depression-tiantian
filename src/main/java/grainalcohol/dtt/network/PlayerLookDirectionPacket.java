package grainalcohol.dtt.network;

import dev.architectury.networking.NetworkManager;
import grainalcohol.dtt.api.internal.PlayerLookDirectionController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.function.Supplier;

public class PlayerLookDirectionPacket {
    private final boolean shouldFix;

    public PlayerLookDirectionPacket(boolean shouldFix) {
        this.shouldFix = shouldFix;
    }

    // 编码
    public void encode(PacketByteBuf buf) {
        buf.writeBoolean(shouldFix);
    }

    // 解码
    public PlayerLookDirectionPacket (PacketByteBuf buf) {
        this.shouldFix = buf.readBoolean();
    }

    // 应用
    public void apply(Supplier<NetworkManager.PacketContext> contextSupplier) {
        contextSupplier.get().queue(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player != null) {
                if (client.player instanceof PlayerLookDirectionController controller) {
                    controller.dtt$setShouldFixFaceDirection(shouldFix);
                }
            }
        });
    }
}
