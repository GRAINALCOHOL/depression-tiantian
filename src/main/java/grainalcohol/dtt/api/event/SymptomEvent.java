package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import grainalcohol.dtt.mental.MentalHealthStatus;
import grainalcohol.dtt.mixin.ItemStackMixin;
import grainalcohol.dtt.mixin.LivingEntityMixin;
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
     * @see MentalFatigueEvent#onMentalFatigueTriggered(ServerPlayerEntity)
     */
    Event<MentalFatigueEvent> MENTAL_FATIGUE_EVENT = EventFactory.createLoop();


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
        void onMentalFatigueTriggered(ServerPlayerEntity player);
    }
}
