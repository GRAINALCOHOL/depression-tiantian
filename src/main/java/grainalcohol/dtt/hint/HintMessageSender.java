package grainalcohol.dtt.hint;

import grainalcohol.dtt.util.StringUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Consumer;

/**
 * <h1>HintMessageSender</h1>
 * <p>用于发送提示消息，自动处理重要性、发送与否、发送时机、变体选择和队列阻塞</p>
 *
 * <h2>触发消息</h2>
 * <pre>{@code
 * if (someCondition()) {
 *     // 触发一个提示消息
 *     HintMessageSender.trigger(player, MyRegistries.MY_SUBSCRIPTION_MESSAGE);
 * }
 * }</pre>
 * <pre>{@code
 * int someValue;
 * if (someValue > 0) {
 *     // 触发一个提示消息，并附加上下文数据 someValue
 *     HintMessageSender.trigger(player, MyRegistries.MY_SUBSCRIPTION_MESSAGE, HintMessageContext.of(someValue));
 * }
 * }</pre>
 *
 * <h2>绕过冷却发送消息</h2>
 * <pre>{@code
 * int someValue;
 * if (someCondition()) {
 *     // 立即发送或创建一个提示消息任务，并附加上下文数据 someValue
 *     HintMessageSender.send(player, MyRegistries.MY_MESSAGE, HintMessageContext.of(someValue));
 * }
 * }</pre>
 */
public class HintMessageSender {
    private static final Random RANDOM = new Random();

    /**
     * 触发一个提示消息，程序自动处理是否发送和发送时机
     * @param player 要发送的目标
     * @param subscriptionHintMessage 要触发的消息
     */
    public static void trigger(ServerPlayerEntity player, SubscriptionHintMessage subscriptionHintMessage) {
        trigger(player, subscriptionHintMessage, null);
    }

    /**
     * 触发一个提示消息，程序自动处理是否发送和发送时机，可以填入上下文数据
     * @param player 要发送的目标
     * @param subscriptionHintMessage 要触发的消息
     * @param context 可选的上下文数据，将填入本地化后的占位符
     */
    public static void trigger(ServerPlayerEntity player, SubscriptionHintMessage subscriptionHintMessage, @Nullable HintMessageContext<?> context) {
        if (subscriptionHintMessage.isImportant()) immediately(player, subscriptionHintMessage, context);
        else addPendingTask(player, subscriptionHintMessage, context);
    }

    /**
     * 绕过冷却时间机制，立刻创建一个提示消息任务，程序自动处理发送时机和声明周期
     * @param player 要发送的目标
     * @param hintMessage 要发送的消息
     */
    public static void send(ServerPlayerEntity player, HintMessage hintMessage) {
        send(player, hintMessage, null);
    }

    /**
     * 绕过冷却时间机制，立刻创建一个提示消息任务，程序自动处理发送时机和声明周期
     * @param player 要发送的目标
     * @param sendable 要发送的消息
     */
    public static void send(ServerPlayerEntity player, Sendable sendable) {
        send(player, sendable, null);
    }

    /**
     * 绕过冷却时间机制，立刻创建一个提示消息任务，程序自动处理发送时机和声明周期，可以填入上下文数据
     * @param player 要发送的目标
     * @param sendable 要发送的消息
     * @param context 可选的上下文数据，将填入本地化后的占位符
     */
    public static void send(ServerPlayerEntity player, Sendable sendable, @Nullable HintMessageContext<?> context) {
        if (sendable.isImportant()) immediately(player, sendable, context);
        else send(player, new HintMessageTask(sendable, context));
    }

    /**
     * 绕过冷却时间机制，立刻创建一个提示消息任务，程序自动处理发送时机和声明周期
     * @param player 要发送的目标
     * @param task 要发送的消息任务
     */
    public static void send(ServerPlayerEntity player, HintMessageTask task) {
        sendMessage(player, task);
    }

    protected static void sendMessage(ServerPlayerEntity player, Sendable sendable) {
        if (sendable.isImportant()) {
            immediately(player, sendable);
            HintMessageManager.blockQueue(player.getUuid());
        } else {
            addPendingTask(player, new HintMessageTask(sendable));
        }
    }

    protected static void immediately(ServerPlayerEntity player, Sendable sendable, @Nullable HintMessageContext<?> context) {
        sendHintMessage(player, sendable, context);
    }

    protected static void immediately(ServerPlayerEntity player, Sendable sendable) {
        sendHintMessage(player, sendable);
    }

    protected static void addPendingTask(ServerPlayerEntity player, HintMessageTask task) {
        HintMessageManager.addPendingHintMessageTask(player.getUuid(), task);
    }

    protected static void addPendingTask(ServerPlayerEntity player, Sendable sendable, @Nullable HintMessageContext<?> context) {
        HintMessageManager.addPendingHintMessageTask(player.getUuid(), sendable, context);
    }

    protected static void sendHintMessage(ServerPlayerEntity player, Sendable sendable, @Nullable HintMessageContext<?> context) {
        sendHintMessage(player, sendable.getTranslationKey(), sendable.getVariantCount(), sendable.getAfterSend(), context);
    }

    protected static void sendHintMessage(ServerPlayerEntity player, Sendable sendable) {
        sendHintMessage(player, sendable.getTranslationKey(), sendable.getVariantCount(), sendable.getAfterSend(), null);
    }

    protected static void sendHintMessage(
            ServerPlayerEntity player, String translationKey, int variantCount,
            @Nullable Consumer<ServerPlayerEntity> afterSend, @Nullable HintMessageContext<?> context
    ) {
        if (context == null || context.get() == null) {
            sendToPlayer(player, translationKey, variantCount, afterSend);
        } else {
            sendToPlayer(player, translationKey, variantCount, afterSend, context.get().toString());
        }
    }

    private static void sendToPlayer(
            ServerPlayerEntity player, String translationKey, int variantCount,
            @Nullable Consumer<ServerPlayerEntity> afterSend
    ) {
        player.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(
                translationKey, variantCount, RANDOM
        )), true);
        if (afterSend != null) afterSend.accept(player);
    }

    private static void sendToPlayer(
            ServerPlayerEntity player, String translationKey, int variantCount,
            @Nullable Consumer<ServerPlayerEntity> afterSend, @NotNull String contextString
    ) {
        player.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(
                translationKey, variantCount, RANDOM
        ), contextString), true);
        if (afterSend != null) afterSend.accept(player);
    }
}
