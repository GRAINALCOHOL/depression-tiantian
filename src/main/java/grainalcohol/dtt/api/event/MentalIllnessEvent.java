package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import grainalcohol.dtt.mental.MentalIllnessStatus;
import net.depression.effect.SleepinessEffect;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MentalIllnessEvent {
    /**
     * 患病情况变化事件
     * @see MentalIllnessChanged#onMentalIllnessChanged(ServerPlayerEntity, MentalIllnessStatus, MentalIllnessStatus)
     */
    Event<MentalIllnessChanged> MENTAL_HEALTH_CHANGED_EVENT = EventFactory.createLoop();

    /**
     * 闭眼事件，{@code causedBySleepinessStatusEffect}指本次闭眼是否由困倦状态效果触发
     * 客户端的视觉效果会在触发后延迟开始
     * @see CloseEyesEvent#onCloseEyes(ServerPlayerEntity, boolean)
     * @see SleepinessEffect 困倦状态效果
     */
    Event<CloseEyesEvent> CLOSE_EYES_EVENT = EventFactory.createLoop();
    /**
     * 睁眼事件
     * @see OpenEyesEvent#onOpenEyes(ServerPlayerEntity)
     */
    Event<OpenEyesEvent> OPEN_EYES_EVENT = EventFactory.createLoop();
    /**
     * 失眠事件
     * @see InsomniaEvent#onInsomniaHappened(ServerPlayerEntity)
     */
    Event<InsomniaEvent> INSOMNIA_EVENT = EventFactory.createLoop();

    @FunctionalInterface
    interface MentalIllnessChanged {
        void onMentalIllnessChanged(ServerPlayerEntity player, MentalIllnessStatus lastTickStatus, MentalIllnessStatus currentStatus);
    }

    @FunctionalInterface
    interface CloseEyesEvent {
        void onCloseEyes(ServerPlayerEntity player, boolean causedBySleepinessStatusEffect);
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
