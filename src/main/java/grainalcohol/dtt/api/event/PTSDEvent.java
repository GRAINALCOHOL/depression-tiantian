package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.api.wrapper.PTSDLevel;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PTSDEvent {
    /**
     * PTSD发作事件，该事件可以被取消
     * @see PTSDTriggeredEvent#onPTSDTriggered(ServerPlayerEntity, PTSDLevel, double)
     */
    Event<PTSDTriggeredEvent> PTSD_TRIGGERED_EVENT = EventFactory.createEventResult();
    /**
     * PTSD幻视触发事件，该事件可以被取消
     * @see PTSDPhotismEvent#onPTSDPhotismTriggered(ServerPlayerEntity, String)
     */
    Event<PTSDPhotismEvent> PTSD_PHOTISM_EVENT = EventFactory.createEventResult();
    /**
     * PTSD消散事件
      * @see PTSDDisperseEvent#onPTSDDisperse(ServerPlayerEntity, String)
     */
    Event<PTSDDisperseEvent> PTSD_DISPERSE_EVENT = EventFactory.createLoop();
    /**
     * PTSD缓解事件
     * @see PTSDRemissionEvent#onPTSDRemission(ServerPlayerEntity, String, PTSDLevel)
     */
    Event<PTSDRemissionEvent> PTSD_REMISSION_EVENT = EventFactory.createLoop();
    /**
     * PTSD形成事件，该事件可以被取消
     * @see PTSDFormEvent#onPTSDFormed(ServerPlayerEntity, String, PTSDLevel)
     */
    Event<PTSDFormEvent> PTSD_FORM_EVENT = EventFactory.createEventResult();
    /**
     * PTSD等级变化事件
     * @see PTSDLevelChangedEvent#onPTSDLevelChanged(ServerPlayerEntity, String, PTSDLevel, PTSDLevel)
     */
    Event<PTSDLevelChangedEvent> PTSD_LEVEL_CHANGED_EVENT = EventFactory.createLoop();
    
    @FunctionalInterface
    interface PTSDTriggeredEvent {
        /**
         * PTSD发作事件，但是注意，取消该事件只能阻止服务器发送PTSD发作的相关数据包，
         * 无法阻止情绪值的扣除、幻觉触发和喘息症状。<br>
         * 吐槽：没招啊这个，它散着写在tick方法里面，我根本不知道怎么注入，加油吧。
         * @param onsetLevel PTSD发作等级，分为5个等级，取值范围[0, 4]。
         * @param distance 触发PTSD的实体与玩家之间的距离，单位为方块（米）。
         */
        EventResult onPTSDTriggered(ServerPlayerEntity player, PTSDLevel onsetLevel, double distance);
    }
    
    @FunctionalInterface
    interface PTSDPhotismEvent {
        /**
         * @param photismId 触发幻视的ID。
         * @see grainalcohol.dtt.mixin.event.PTSDManagerMixin
         */
        EventResult onPTSDPhotismTriggered(ServerPlayerEntity player, String photismId);
    }

    @FunctionalInterface
    interface PTSDDisperseEvent {
        /**
         * @param ptsdId 消散的PTSD的id。
         * @see grainalcohol.dtt.mixin.event.MentalStatusMixin
         */
        void onPTSDDisperse(ServerPlayerEntity player, String ptsdId);
    }

    @FunctionalInterface
    interface PTSDRemissionEvent {
        /**
         * @param ptsdId 缓解的PTSD的id。
         * @param currentLevel 缓解后的PTSD等级。
         * @see grainalcohol.dtt.mixin.event.PTSDManagerMixin
         */
        void onPTSDRemission(ServerPlayerEntity player, String ptsdId, PTSDLevel currentLevel);
    }

    @FunctionalInterface
    interface PTSDFormEvent {
        /**
         * @param formId 形成的PTSD的id。
         * @param currentLevel 形成后的PTSD等级。
         * @see grainalcohol.dtt.mixin.event.MentalStatusMixin
         */
        EventResult onPTSDFormed(ServerPlayerEntity player, String formId, PTSDLevel currentLevel);
    }

    @FunctionalInterface
    interface PTSDLevelChangedEvent {
        /**
         * PTSD等级改变
         * @param player PTSD等级改变的玩家
         * @param ptsdId PTSD的id
         * @param lastLevel 改变前的PTSD等级
         * @param currentLevel 改变后的PTSD等级
         */
        void onPTSDLevelChanged(ServerPlayerEntity player, String ptsdId, PTSDLevel lastLevel, PTSDLevel currentLevel);
    }
}
