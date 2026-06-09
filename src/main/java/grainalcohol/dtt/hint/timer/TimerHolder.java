package grainalcohol.dtt.hint.timer;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class TimerHolder<K, T extends Timer> implements Iterable<Map.Entry<K, T>> {
    private final Map<K, T> timerMap = new ConcurrentHashMap<>();

    public void add(K key, T timer) {
        timerMap.put(key, timer);
    }

    public T get(K key) {
        return timerMap.get(key);
    }

    public T computeIfAbsent(K key, @NotNull Function<? super K, ? extends T> mappingFunction) {
        return timerMap.computeIfAbsent(key, mappingFunction);
    }

    public void tick(ServerPlayerEntity player) {
        forEach((key, timer) -> timer.tick(player));
    }

    public boolean isFinished(K key) {
        Timer timer = timerMap.get(key);
        return timer != null && timer.isFinished();
    }

    public void reset(K key) {
        Timer timer = timerMap.get(key);
        if (timer != null) timer.reset();
    }

    public void remove(K key) {
        timerMap.remove(key);
    }

    public void removeIf(@NotNull Predicate<T> condition) {
        timerMap.entrySet().removeIf(entry -> condition.test(entry.getValue()));
    }

    public void removeIfFinished() {
        removeIf(Timer::isFinished);
    }

    public T getOrDefault(K key, T defaultTimer) {
        return timerMap.getOrDefault(key, defaultTimer);
    }

    @Nullable
    public Set<Map.Entry<K, T>> entrySet() {
        return timerMap.entrySet();
    }

    @Nullable
    public Set<K> keySet() {
        return timerMap.keySet();
    }

    public boolean isEmpty() {
        return timerMap.isEmpty();
    }

    public boolean containsKey(K key) {
        return timerMap.containsKey(key);
    }

    public Collection<T> values() {
        return timerMap.values();
    }

    @Override
    public @NotNull Iterator<Map.Entry<K, T>> iterator() {
        return timerMap.entrySet().iterator();
    }

    public void forEach(@NotNull BiConsumer<? super K , ? super T> consumer) {
        timerMap.forEach(consumer);
    }
}
