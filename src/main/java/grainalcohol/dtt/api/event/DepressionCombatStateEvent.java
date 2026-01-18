package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;

public interface DepressionCombatStateEvent {
    /**
     * 玩家进入战斗状态事件
     * @see EnterCombatState#onEnterCombatState(ServerPlayerEntity)
     */
    Event<EnterCombatState> ENTER_COMBAT_STATE_EVENT = EventFactory.createLoop();
    /**
     * 玩家退出战斗状态事件
     * @see ExitCombatState#onExitCombatState(ServerPlayerEntity)
     */
    Event<ExitCombatState> EXIT_COMBAT_STATE_EVENT = EventFactory.createLoop();
    /**
     * 玩家进入禅定状态事件
     * @see EnterZenState#onEnterZenState(ServerBossBar, ServerPlayerEntity)
     */
    Event<EnterZenState> ENTER_ZEN_STATE_EVENT = EventFactory.createLoop();
    /**
     * 玩家退出禅定状态事件
     * @see ExitZenState#onExitZenState(ServerBossBar, ServerPlayerEntity)
     */
    Event<ExitZenState> EXIT_ZEN_STATE_EVENT = EventFactory.createLoop();

    @FunctionalInterface
    interface EnterCombatState {
        void onEnterCombatState(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ExitCombatState {
        void onExitCombatState(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface EnterZenState {
        void onEnterZenState(ServerBossBar event, ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ExitZenState {
        void onExitZenState(ServerBossBar event, ServerPlayerEntity player);
    }
}
