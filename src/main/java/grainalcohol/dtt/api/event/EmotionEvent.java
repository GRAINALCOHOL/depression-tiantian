package grainalcohol.dtt.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EmotionEvent {
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
     * @see EnterZenState#onEnterZenState(ServerPlayerEntity)
     */
    Event<EnterZenState> ENTER_ZEN_STATE_EVENT = EventFactory.createLoop();
    /**
     * 玩家退出禅定状态事件
     * @see ExitZenState#onExitZenState(ServerPlayerEntity)
     */
    Event<ExitZenState> EXIT_ZEN_STATE_EVENT = EventFactory.createLoop();
    /**
     * 玩家进入了一个BossBar通知范围，该事件可以被取消
     * @see ZenStateAddition#onZenStateChange(ServerBossBar, ServerPlayerEntity)
     */
    Event<ZenStateAddition> ZEN_STATE_ADDITION_EVENT = EventFactory.createEventResult();
    /**
     * 玩家离开了一个BossBar通知范围，该事件可以被取消
     * @see ZenStateRemoval#onZenStateChange(ServerBossBar, ServerPlayerEntity)
     */
    Event<ZenStateRemoval> ZEN_STATE_REMOVAL_EVENT = EventFactory.createEventResult();

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
        void onEnterZenState(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ExitZenState {
        void onExitZenState(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ZenStateAddition {
        EventResult onZenStateChange(ServerBossBar bossBar, ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface ZenStateRemoval {
        EventResult onZenStateChange(ServerBossBar bossBar, ServerPlayerEntity player);
    }
}
