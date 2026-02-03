package grainalcohol.dtt.api.internal;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 服务端的眼睛状态标记接口，起点是depression服务端发送数据包，终点是客户端标记重置为false。
 * 注：闭眼特效会在短暂延迟后开始。
 * @see net.depression.mental.MentalIllness#tick(ServerPlayerEntity) depression发送数据包位置
 * @see grainalcohol.dtt.mixin.ServerPlayerEntityMixin
 * @see grainalcohol.dtt.network.OpenEyesEventPacket
 * @see net.depression.client.ClientMentalIllness#isCloseEye
 * @see grainalcohol.dtt.init.DTTListener#dttAPIEventInit()
 */
public interface EyesStatusFlagController {
    boolean dtt$getIsEyesClosedFlag();
    void dtt$setIsEyesClosedFlag(boolean isClosed);
}
