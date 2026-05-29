package grainalcohol.dtt.hint;

// TODO: 实现多参数
public class HintMessageContext<T> {
    private final T data;

    private HintMessageContext(T data) {
        this.data = data;
    }

    public static <T> HintMessageContext<T> of(T data) {
        return new HintMessageContext<>(data);
    }

    public T get() {
        return data;
    }

    @Override
    public String toString() {
        return get().toString();
    }
}
