package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PTSDEvent {
    /**
     * PTSD发作事件
     * @see PTSDTriggeredEvent#onPTSDTriggered(ServerPlayerEntity, int, double)
     */
    Event<PTSDTriggeredEvent> PTSD_TRIGGERED_EVENT = EventFactory.createEventResult();

    @FunctionalInterface
    interface PTSDTriggeredEvent {
        /**
         * @param onsetLevel PTSD发作等级，分为5个等级，取值范围[0, 4]。
         * @param distance 触发PTSD的实体与玩家之间的距离，单位为方块（米）。
         */
        EventResult onPTSDTriggered(ServerPlayerEntity player, int onsetLevel, double distance);
    }
}
