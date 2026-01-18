package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface MentalHealthEvent {
    /**
     * （患双相时）进入躁狂相事件，所以进入之前一定是抑郁相
     * @see EnterManicPhase#onEnterManicPhase(ServerPlayerEntity)
     */
    Event<EnterManicPhase> ENTER_MANIC_PHASE_EVENT = EventFactory.createLoop();
    /**
     * （患双相时）退出躁狂相事件，所以退出之后一定是抑郁相
     * @see ExitManicPhase#onExitManicPhase(ServerPlayerEntity)
     */
    Event<ExitManicPhase> EXIT_MANIC_PHASE_EVENT = EventFactory.createLoop();

    @FunctionalInterface
    interface EnterManicPhase {
        void onEnterManicPhase(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ExitManicPhase {
        void onExitManicPhase(ServerPlayerEntity player);
    }
}
