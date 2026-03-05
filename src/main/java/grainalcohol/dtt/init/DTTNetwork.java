package grainalcohol.dtt.init;

import dev.architectury.networking.NetworkChannel;
import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.network.OpenEyesEventPacket;
import grainalcohol.dtt.network.ServerConfigPacket;

public class DTTNetwork {
    public static final NetworkChannel CHANNEL = NetworkChannel.create(DTTMod.id("networking_channel"));

    public static void init() {
        // 睁眼事件包
        CHANNEL.register(OpenEyesEventPacket.class, OpenEyesEventPacket::encode,
                OpenEyesEventPacket::new, OpenEyesEventPacket::apply);
        // 客户端读取服务端配置选项包
        CHANNEL.register(ServerConfigPacket.class, ServerConfigPacket::encode,
                ServerConfigPacket::new, ServerConfigPacket::apply);
    }
}
