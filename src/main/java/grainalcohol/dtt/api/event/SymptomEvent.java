package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.mental.MentalHealthStatus;
import grainalcohol.dtt.mixin.ItemStackMixin;
import grainalcohol.dtt.mixin.LivingEntityMixin;
import net.depression.effect.SleepinessEffect;
import net.minecraft.server.network.ServerPlayerEntity;

public interface SymptomEvent {
    /**
     * 触发厌食症事件，逻辑位于尝试进食前（use方法头部），该事件可以被取消
     * @see ItemStackMixin
     */
    Event<AnorexiaTriggeredEvent> ANOREXIA_TRIGGERED = EventFactory.createEventResult();
    /**
     * 打断进食事件，该事件可以被取消
     * @see LivingEntityMixin
     */
    Event<InterruptEatingEvent> INTERRUPT_EATING = EventFactory.createEventResult();
    /**
     * 精神疲劳触发事件
     * @see grainalcohol.dtt.mixin.event.MentalIllnessMixin
     * @see MentalFatigueEvent#onMentalFatigueTriggered(ServerPlayerEntity)
     */
    Event<MentalFatigueEvent> MENTAL_FATIGUE_EVENT = EventFactory.createEventResult();

    /**
     * 闭眼事件，{@code causedBySleepinessStatusEffect}指本次闭眼是否由困倦状态效果触发
     * 客户端的视觉效果会在触发后延迟开始
     * @see grainalcohol.dtt.mixin.event.MentalIllnessMixin
     * @see CloseEyesEvent#onCloseEyes(ServerPlayerEntity, boolean)
     * @see SleepinessEffect 困倦状态效果
     */
    Event<CloseEyesEvent> CLOSE_EYES_EVENT = EventFactory.createEventResult();
    /**
     * 简化的闭眼事件，不区分闭眼原因，提供给不关心闭眼原因的功能
     * @see grainalcohol.dtt.mixin.event.CloseEysPacketMixin
     */
    Event<SimpleCloseEyesEvent> SIMPLE_CLOSE_EYES_EVENT = EventFactory.createEventResult();
    /**
     * 睁眼事件
     * @see grainalcohol.dtt.network.OpenEyesEventPacket
     * @see OpenEyesEvent#onOpenEyes(ServerPlayerEntity)
     */
    Event<OpenEyesEvent> OPEN_EYES_EVENT = EventFactory.createLoop();
    /**
     * 失眠事件，指玩家因失眠而无法入睡
     * @see grainalcohol.dtt.mixin.event.MentalIllnessMixin
     * @see InsomniaEvent#onInsomniaHappened(ServerPlayerEntity)
     */
    Event<InsomniaEvent> INSOMNIA_EVENT = EventFactory.createLoop();

    @FunctionalInterface
    interface AnorexiaTriggeredEvent {
        EventResult onAnorexiaTriggered(ServerPlayerEntity player, MentalHealthStatus mentalHealthStatus);
    }

    @FunctionalInterface
    interface InterruptEatingEvent {
        EventResult onInterruptEating(ServerPlayerEntity player, MentalHealthStatus mentalHealthStatus);
    }

    @FunctionalInterface
    interface MentalFatigueEvent {
        EventResult onMentalFatigueTriggered(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface CloseEyesEvent {
        EventResult onCloseEyes(ServerPlayerEntity player, boolean causedBySleepinessStatusEffect);
    }

    @FunctionalInterface
    interface SimpleCloseEyesEvent {
        EventResult onCloseEyes(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface OpenEyesEvent {
        void onOpenEyes(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface InsomniaEvent {
        void onInsomniaHappened(ServerPlayerEntity player);
    }
}
