package grainalcohol.dtt.util;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StringUtil {
    private static final Object LOCK = new Object();
    public static String warp(String str, String warper) {
        if (str == null || warper == null) return null;
        return warper + str + warper;
    }

    public static String warp(String str) {
        return warp(str, "'");
    }

    public static @NotNull String findTranslationKeyVariant(@NotNull String baseTranslationKey, int variantCount, Random random) {
        return findTranslationKeyVariant(baseTranslationKey, variantCount, random, null);
    }

    /**
     * 在指定的变体数量内随机选取一个已定义的变体translationKey，
     * 若无则返回原始translationKey，若usedKeys不为null会自动处理重复情况<br>
     * 所有变体全部用过的话就返回原始translationKey，线程不安全<br>
     * <br>
     * 以"diary.dmm.healthy.warm"为例，设置variantCount = 3<br>
     * 你应该这样定义变体：<br>
     * <pre>{@code
     *  {
     *      "diary.dmm.healthy.warm": "原始文本",
     *      "diary.dmm.healthy.warm.1": "变体文本 1",
     *      "diary.dmm.healthy.warm.2": "变体文本 2",
     *      "diary.dmm.healthy.warm.3": "变体文本 3"
     *  }
     * }</pre>
     * 若其中有定义的变体，则随机返回一个（包含原始translationKey），若无则返回原始的translationKey（即"diary.dmm.healthy.warm"）<br>
     * @param baseTranslationKey 原始translationKey
     * @param variantCount 变体数量，取值范围为[1, variantCount]
     * @param random 用于随机选取的Random实例
     * @param usedKeys 已使用的translationKey集合，若不为null则会避免重复使用，若为null则不检查重复
     * @return 随机选取的变体translationKey或原始translationKey
     */
    public static @NotNull String findTranslationKeyVariant(
            @NotNull String baseTranslationKey, int variantCount,
            Random random, @Nullable Collection<String> usedKeys
    ) {
        if (variantCount < 1) {
            if (usedKeys != null) usedKeys.add(baseTranslationKey);
            return baseTranslationKey;
        }

        List<String> candidateKeys = new ArrayList<>();
        for (int i = 1; i <= variantCount; i++) {
            // 在 [1, variantCount] 内选取的整数作为后缀的额外键值
            String tempKey = baseTranslationKey + "." + i;
            if (isTranslationKeyDefined(tempKey)) {
                // 翻译结果与原始键值不同说明有定义
                if (usedKeys != null && usedKeys.contains(tempKey)) {
                    continue;
                }
                // 未被使用过即可添加到候选列表
                candidateKeys.add(tempKey);
            }
        }
        if (usedKeys == null || !usedKeys.contains(baseTranslationKey)) {
            // 未提供已使用列表 或 未使用过原始键值时添加原始键值作为候选
            candidateKeys.add(baseTranslationKey);
        }

        if (candidateKeys.isEmpty()) {
            return baseTranslationKey;
        }

        String resultKey = candidateKeys.get(random.nextInt(candidateKeys.size()));
        if (usedKeys != null) usedKeys.add(resultKey);
        return resultKey;
    }

    /**
     * 检查给定的translationKey是否有定义（即翻译结果与原始键值不同）<br>
     * @param translationKey 要检查的translationKey
     * @return translationKey是否有定义
     */
    public static boolean isTranslationKeyDefined(@NotNull String translationKey) {
        Text translated = Text.translatable(translationKey);
        return !translated.getString().equals(translationKey);
    }
}
