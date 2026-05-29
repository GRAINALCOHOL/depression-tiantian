package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.RandomizedTimer;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.hint.timer.TimerHolder;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO: 序列化
public class HintMessageManager {
    // 待触发消息订阅列表，每tick尝试触发
//    private static final Map<UUID, TimerHolder<SubscriptionTask, Timer>> hintMessageSubscriptionMap = new ConcurrentHashMap<>();
    // 消息实例列表，玩家 UUID -> SendableManager (消息 ID -> 消息实例)，所有消息实例都在这里维护，其它位置使用Identifier引用即可
    private static final Map<UUID, SendableManager<HintMessage>> hintMessageInstanceMap = new HashMap<>();

    // 待发送消息轮询计时器列表
    private static final TimerHolder<UUID, Timer> pollingTimerHolder = new TimerHolder<>();
    // 待发送消息队列列表（非立即发送的消息最终都会来到这里），内部维护了生命周期计时器
    private static final Map<UUID, Queue<HintMessageTask>> pendingHintMessageTaskQueueMap = new ConcurrentHashMap<>();
    // 被暂停发送队列的玩家列表（阻塞期间不发送消息，直到下次轮询时尝试解除）
    private static final Set<UUID> blockedPendingQueues = new HashSet<>();

    // 默认的待发送消息轮询计时器（发送节奏控制）
    private static final RandomizedTimer DEFAULT_TIMER = RandomizedTimer.Builder
            .builder(10, TimeUnit.SECOND)
            .randomBound(5, TimeUnit.SECOND)
            .build();

    public static void init(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();

        for (HintMessage hintMessage : DTTRegistries.HINT_MESSAGE_REGISTRY) {
            hintMessageInstanceMap.computeIfAbsent(playerUuid, k -> new SendableManager<>()).register(hintMessage);
        }
        pollingTimerHolder.add(playerUuid, DEFAULT_TIMER.copy());
        pendingHintMessageTaskQueueMap.put(playerUuid, new ArrayDeque<>());
    }

    public static void tick(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        // tick 所有实例的内部计时器
        // 发送逻辑由 HintMessage 内部控制
        hintMessageInstanceMap.get(playerId).values()
                .forEach(hintMessage -> hintMessage.tick(player));

        // 处理过期机制，移除过期任务
        var queue = pendingHintMessageTaskQueueMap.get(playerId);
        if (queue != null) {
            queue.forEach(task -> task.tick(player));
            queue.removeIf(HintMessageTask::isExpired);
        }

        // 处理所有已订阅的待触发消息
//        handleSubscribedMessages(player);

        // tick 轮询计时器
        Timer timer = pollingTimerHolder.getOrDefault(playerId, null);
        if (timer == null) {
            System.out.println("未找到轮询计时器，玩家：" + player.getName().getString());
            return;
        }

        timer.tick(player);

        if (timer.isFinished()) {
            System.out.println("轮询待发送消息，玩家：" + player.getName().getString() + "，当前队列：" + pendingHintMessageTaskQueueMap.get(playerId));
            if (blockedPendingQueues.contains(playerId)) {
                timer.reset();
                unblockQueue(playerId);
                return;
            }

            sendNext(player);
            timer.reset();
        }
    }

//    private static void handleSubscribedMessages(ServerPlayerEntity player) {
//        // 获取订阅列表
//        var hintMessageHolder = hintMessageSubscriptionMap.get(player.getUuid());
//        if (hintMessageHolder == null || hintMessageHolder.isEmpty()) return;
//
//        // tick 所有待触发的计时器，处理过期机制
//        hintMessageHolder.tick(player);
//        hintMessageHolder.removeIfFinished();
//
//        Set<SubscriptionTask> tasks = hintMessageHolder.keySet();
//        if (tasks == null || tasks.isEmpty()) return;
//
//        // 触发所有待触发的 HintMessage
//        for (SubscriptionTask subscriptionTask : tasks) {
//            // 找到对应的实例
//            HintMessage hintMessage = findHintMessageInstance(player.getUuid(), subscriptionTask.getIdentifier());
//            if (hintMessage != null) {
//                hintMessage.trigger(player, subscriptionTask.getHintMessageContext());
//                hintMessageHolder.remove(subscriptionTask);
//            }
//        }
//    }

    private static void sendNext(ServerPlayerEntity player) {
        var taskQueue = pendingHintMessageTaskQueueMap.get(player.getUuid());
        if (taskQueue == null || taskQueue.isEmpty()) return;

        taskQueue.poll().send(player);
    }

    @Nullable
    public static HintMessage findHintMessageInstance(UUID playerId, HintMessage hintMessage) {
        return findHintMessageInstance(playerId, hintMessage.getIdentifier());
    }

    @Nullable
    protected static HintMessage findHintMessageInstance(UUID playerId, Identifier identifier) {
        return hintMessageInstanceMap.get(playerId).get(identifier);
    }

    public static void addPendingHintMessageTask(UUID playerId, Sendable sendable, @Nullable HintMessageContext<?> context) {
        addPendingHintMessageTask(playerId, new HintMessageTask(sendable, context));
    }

    public static void addPendingHintMessageTask(UUID playerId, HintMessageTask hintMessageTask) {
        if (pendingHintMessageTaskQueueMap.get(playerId).contains(hintMessageTask)) return;

        pendingHintMessageTaskQueueMap.computeIfAbsent(playerId, k -> new ArrayDeque<>()).add(hintMessageTask);
        System.out.println("当前待发送队列：" + pendingHintMessageTaskQueueMap.get(playerId));
    }

    public static void blockQueue(UUID playerId) {
        blockedPendingQueues.add(playerId);
    }

    public static void unblockQueue(UUID playerId) {
        blockedPendingQueues.remove(playerId);
    }
}
