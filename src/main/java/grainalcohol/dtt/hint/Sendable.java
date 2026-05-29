package grainalcohol.dtt.hint;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface Sendable {
    /**
     * 获取唯一标识符
     * @return 实例的唯一标识符
     */
    Identifier getIdentifier();

    /**
     * 获取用于本地化的翻译键
     * @return 用于本地化的翻译键
     */
    String getTranslationKey();

    /**
     * 获取变体数量，用于随机化本地化文本
     * @return 变体数量
     * @see grainalcohol.dtt.util.StringUtil#findTranslationKeyVariant(String, int)
     */
    int getVariantCount();

    /**
     * 是否立即发送，而不是在消息队列中排队
     * @return 为 true则立即发送；为 false则添加到消息队列中排队
     */
    boolean isImportant();

    /**
     * 发送消息给玩家
     * @param player 接收消息的玩家
     */
    void send(ServerPlayerEntity player);

    /**
     * 发送后执行的操作，为 null 则不执行任何操作
     * @return 操作什么
     */
    @Nullable
    Consumer<ServerPlayerEntity> getAfterSend();
}
