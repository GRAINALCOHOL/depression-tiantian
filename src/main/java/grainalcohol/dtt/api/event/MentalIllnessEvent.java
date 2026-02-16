package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import grainalcohol.dtt.mental.MentalIllnessStatus;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MentalIllnessEvent {
    /**
     * 患病情况变化事件
     * @see MentalIllnessChangedEvent#onMentalIllnessChanged(ServerPlayerEntity, MentalIllnessStatus, MentalIllnessStatus)
     */
    Event<MentalIllnessChangedEvent> MENTAL_HEALTH_CHANGED_EVENT = EventFactory.createLoop();

    @FunctionalInterface
    interface MentalIllnessChangedEvent {
        void onMentalIllnessChanged(ServerPlayerEntity player, MentalIllnessStatus lastTickStatus, MentalIllnessStatus currentStatus);
    }
}
