package grainalcohol.dtt.hint;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class SendableManager<S extends Sendable> implements Iterable<Map.Entry<Identifier, S>> {
    private final Map<Identifier, S> sendableMap = new ConcurrentHashMap<>();

    /**
     * 不会替换已存在的实例，如果需要替换请使用 replace 方法
     * @param sendable sendable 实例
     */
    public void register(S sendable) {
        if (!containsKey(sendable.getIdentifier())) {
            sendableMap.put(sendable.getIdentifier(), sendable);
        }
    }

    public void replace(S sendable) {
        sendableMap.replace(sendable.getIdentifier(), sendable);
    }

    public S get(Identifier identifier) {
        return sendableMap.get(identifier);
    }

    public boolean containsKey(Identifier identifier) {
        return sendableMap.containsKey(identifier);
    }

    public void remove(Identifier identifier) {
        sendableMap.remove(identifier);
    }

    public Set<Map.Entry<Identifier, S>> entrySet() {
        return sendableMap.entrySet();
    }

    public Set<Identifier> keySet() {
        return sendableMap.keySet();
    }

    public Collection<S> values() {
        return sendableMap.values();
    }

    @Override
    public @NotNull Iterator<Map.Entry<Identifier, S>> iterator() {
        return sendableMap.entrySet().iterator();
    }

    public void forEach(@NotNull BiConsumer<Identifier, S> action) {
        sendableMap.forEach(action);
    }
}
