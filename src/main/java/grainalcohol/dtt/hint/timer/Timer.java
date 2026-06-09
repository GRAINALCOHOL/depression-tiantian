package grainalcohol.dtt.hint.timer;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class Timer {
    @NotNull
    private final Predicate<ServerPlayerEntity> condition;
    private int ticks;
    private final int time;
    private final TimeUnit timeUnit;
    private final int extraTime;
    private final TimeUnit extraTimeUnit;

    protected Timer(Builder builder) {
        this.time = builder.time;
        this.timeUnit = builder.timeUnit;
        this.extraTime = builder.extraTime;
        this.extraTimeUnit = builder.extraTimeUnit;
        this.ticks = getTime() * getTimeUnit().getDurationTicks() + builder.extraTime * builder.extraTimeUnit.getDurationTicks();
        this.condition = builder.condition;

    }

    public static Timer of(int time, TimeUnit timeUnit) {
        return new Builder(time, timeUnit).build();
    }

    public void tick(ServerPlayerEntity player) {
        if (ticks > 0 && condition.test(player)) {
            ticks--;
        }
    }

    public boolean isFinished() {
        return ticks <= 0;
    }

    public int getTicks() {
        return ticks;
    }

    public void reset() {
        setTicks(getTime() * getTimeUnit().getDurationTicks());
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    public int getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Timer copy() {
        return this.toBuilder().build();
    }

    public Timer withCondition(@NotNull Predicate<ServerPlayerEntity> condition) {
        return this.toBuilder()
                .condition(condition)
                .build();
    }

    public Builder toBuilder() {
        return new Builder(getTime(), getTimeUnit())
                .extraTime(getExtraTime(), getExtraTimeUnit())
                .condition(getCondition());
    }

    public int getExtraTime() {
        return extraTime;
    }

    public TimeUnit getExtraTimeUnit() {
        return extraTimeUnit;
    }

    @Override
    public String toString() {
        return "Timer{" + getTicks() + "/" + getTime() * getTimeUnit().getDurationTicks() + "}";
    }

    protected @NotNull Predicate<ServerPlayerEntity> getCondition() {
        return condition;
    }

    public static class Builder {
        private int time;
        private TimeUnit timeUnit;
        private int extraTime = 0;
        private TimeUnit extraTimeUnit = TimeUnit.TICK;
        @NotNull
        private Predicate<ServerPlayerEntity> condition = player -> true;

        protected Builder(int time, TimeUnit timeUnit) {
            this.time = time;
            this.timeUnit = timeUnit;
        }

        public static Builder builder(int time, TimeUnit timeUnit) {
            return new Builder(time, timeUnit);
        }

        public Builder timeTicks(int timeTicks) {
            return time(timeTicks, TimeUnit.TICK);
        }

        public Builder time(int time, TimeUnit timeUnit) {
            this.time = time;
            this.timeUnit = timeUnit;
            return this;
        }

        public Builder extraTicks(int extraTicks) {
            return extraTime(extraTicks, TimeUnit.TICK);
        }

        public Builder extraTime(int extraTime, TimeUnit extraTimeUnit) {
            this.extraTime = extraTime;
            this.extraTimeUnit = extraTimeUnit;
            return this;
        }

        public Builder condition(@NotNull Predicate<ServerPlayerEntity> condition) {
            this.condition = condition;
            return this;
        }

        public Timer build() {
            return new Timer(this);
        }
    }
}
