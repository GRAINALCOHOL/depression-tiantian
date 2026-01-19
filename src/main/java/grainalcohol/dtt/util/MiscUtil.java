package grainalcohol.dtt.util;

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
}
