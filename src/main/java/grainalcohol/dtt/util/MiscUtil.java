package grainalcohol.dtt.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class MiscUtil {
    public static <K, V> void cleanMap(Map<K, V> map, Predicate<K> keyPredicate) {
        map.keySet().removeIf(keyPredicate);
    }

    public static <T> void cleanSet(Set<T> set, Predicate<T> predicate) {
        set.removeIf(predicate);
    }

    /**
     * 有副作用，调用时需要自己维护一个cooldownTicks变量，成功发送消息后重置cooldownTicks为totalCooldownTicks
     * @param player 接收消息的玩家
     * @param baseTranslationKey 基础翻译键，实际使用时会根据variantCount随机选择一个变体
     * @param variantCount 变体数量
     * @param cooldownTicks 剩余冷却时间，单位为tick，每调用一次会减少1，直到冷却时间降到0或以下才会发送消息
     */
    public static void sendOverlayMessageWithCooldown(
            ServerPlayerEntity player, String baseTranslationKey,
            int variantCount, int cooldownTicks, int totalCooldownTicks
    ) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }
        player.sendMessage(Text.translatable(StringUtil.findTranslationKeyVariant(baseTranslationKey, variantCount)), true);
        cooldownTicks = totalCooldownTicks;
    }
}
