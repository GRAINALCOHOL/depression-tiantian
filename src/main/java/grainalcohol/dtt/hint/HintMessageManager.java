package grainalcohol.dtt.hint;

import grainalcohol.dtt.hint.timer.RandomizedTimer;
import grainalcohol.dtt.hint.timer.TimeUnit;
import grainalcohol.dtt.hint.timer.Timer;
import grainalcohol.dtt.hint.timer.TimerHolder;
import grainalcohol.dtt.registry.DTTRegistries;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HintMessageManager {
    // 消息实例列表，玩家 UUID -> SendableManager (消息 ID -> 消息实例)，所有消息实例都在这里维护，其它位置使用Identifier引用即可
    private static final Map<UUID, SendableManager<HintMessageInstance>> hintMessageInstanceMap = new HashMap<>();
    // 待发送消息队列列表（非立即发送的消息最终都会来到这里），内部维护了生命周期计时器
    private static final Map<UUID, Queue<HintMessageTask>> pendingHintMessageTaskQueueMap = new ConcurrentHashMap<>();
    // 待发送消息轮询计时器列表
    private static final TimerHolder<UUID, Timer> pollingTimerHolder = new TimerHolder<>();
    // 被暂停发送队列的玩家列表（暂停期间不发送消息，直到下次轮询时尝试解除）
    private static final Set<UUID> blockedPendingQueues = new HashSet<>();

    // 默认的待发送消息轮询计时器（发送节奏控制）
    private static final RandomizedTimer DEFAULT_TIMER = RandomizedTimer.Builder
            .builder(10, TimeUnit.SECOND)
            .randomBound(5, TimeUnit.SECOND)
            .build();

    public static void onLogin(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();

        if (!hintMessageInstanceMap.containsKey(playerUuid)) {
            for (HintMessage hintMessage : DTTRegistries.GLOBAL_HINT_MESSAGE_REGISTRY) {
                hintMessageInstanceMap.computeIfAbsent(playerUuid, k -> new SendableManager<>()).register(hintMessage.createInstance());
            }
        }
        if (!pollingTimerHolder.containsKey(playerUuid)) pollingTimerHolder.add(playerUuid, DEFAULT_TIMER.copy());
        if (!pendingHintMessageTaskQueueMap.containsKey(playerUuid)) pendingHintMessageTaskQueueMap.put(playerUuid, new ArrayDeque<>());
    }

    // TODO: 如果没有这个可能会导致内存泄露
    public static void onLogout(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        hintMessageInstanceMap.remove(playerUuid);
        pendingHintMessageTaskQueueMap.remove(playerUuid);
        pollingTimerHolder.remove(playerUuid);
        blockedPendingQueues.remove(playerUuid);
    }

    public static void tick(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        // tick 所有实例的内部计时器
        // 发送逻辑由 HintMessageInstance 内部控制
        hintMessageInstanceMap.get(playerId).values()
                .forEach(instance -> instance.tick(player));

        // 处理过期机制，移除过期任务
        var queue = pendingHintMessageTaskQueueMap.get(playerId);
        if (queue != null && !queue.isEmpty()) {
            queue.forEach(task -> task.tick(player));
            queue.removeIf(HintMessageTask::isExpired);
        }

        pollingTimerHolder.tick(player);

        // 处理待发送队列
        if (pollingTimerHolder.isFinished(playerId)) {
            if (blockedPendingQueues.contains(playerId)) {
                // 解除阻塞状态
                unblockQueue(playerId);
                // 本次轮询不发送消息，直接重置计时器等待下一次轮询
                pollingTimerHolder.reset(playerId);
            } else {
                // 发送一个待发送消息（如果有的话）
                sendNext(player);
                // 发送后重置轮询计时器
                pollingTimerHolder.reset(playerId);
            }
        }
    }

    private static void sendNext(ServerPlayerEntity player) {
        var taskQueue = pendingHintMessageTaskQueueMap.get(player.getUuid());
        if (taskQueue == null || taskQueue.isEmpty()) return;

        taskQueue.poll().send(player);
    }

    public static void blockQueue(UUID playerId) {
        blockedPendingQueues.add(playerId);
    }

    public static void unblockQueue(UUID playerId) {
        blockedPendingQueues.remove(playerId);
    }

    public static HintMessageInstance findInstance(UUID playerId, HintMessage hintMessage) {
        return hintMessageInstanceMap.get(playerId).get(hintMessage.getIdentifier());
    }

    public static HintMessageInstance findInstance(UUID playerId, Identifier hintMessageId) {
        return hintMessageInstanceMap.get(playerId).get(hintMessageId);
    }

    public static void addPendingTask(UUID playerId, HintMessageTask task) {
        pendingHintMessageTaskQueueMap.get(playerId).add(task);
    }

    public static void writeToNbt(UUID playerId, NbtCompound nbt) {
        NbtList instanceList = new NbtList();
        for (var instance : hintMessageInstanceMap.get(playerId).values()) {
            instanceList.add(instance.toNbt());
        }
//        hintMessageInstanceMap.remove(playerId);
        nbt.put("HintMessageInstances", instanceList);

        NbtList pendingList = new NbtList();
        for (var task : pendingHintMessageTaskQueueMap.get(playerId)) {
            pendingList.add(task.toNbt());
        }
//        pendingHintMessageTaskQueueMap.remove(playerId);
        nbt.put("PendingHintMessageTasks", pendingList);

        nbt.putInt("PollingTimerTicks", pollingTimerHolder.get(playerId).getTicks());
        nbt.putBoolean("IsQueueBlocked", blockedPendingQueues.contains(playerId));
    }

    public static void readFromNbt(UUID playerId, NbtCompound nbt) {
        SendableManager<HintMessageInstance> instanceManager = new SendableManager<>();
        NbtList instanceList = nbt.getList("HintMessageInstances", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < instanceList.size(); i++) {
            instanceManager.register(HintMessageInstance.fromNbt(instanceList.getCompound(i)));
        }
        hintMessageInstanceMap.put(playerId, instanceManager);

        Queue<HintMessageTask> taskQueue = new ArrayDeque<>();
        NbtList pendingList = nbt.getList("PendingHintMessageTasks", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < pendingList.size(); i++) {
            taskQueue.add(HintMessageTask.fromNbt(pendingList.getCompound(i)));
        }
        pendingHintMessageTaskQueueMap.put(playerId, taskQueue);

        Timer pollingTimer = DEFAULT_TIMER.copy();
        pollingTimer.setTicks(nbt.getInt("PollingTimerTicks"));
        pollingTimerHolder.add(playerId, pollingTimer);

        if (nbt.getBoolean("IsQueueBlocked")) blockQueue(playerId);
    }
}
