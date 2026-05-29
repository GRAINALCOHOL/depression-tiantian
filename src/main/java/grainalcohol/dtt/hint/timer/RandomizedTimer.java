package grainalcohol.dtt.hint.timer;

import grainalcohol.dtt.util.MathUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Predicate;

public class RandomizedTimer extends Timer {
    private static final Random RANDOM = new Random();
    private final int randomBound;
    private final TimeUnit randomBoundUnit;

    public RandomizedTimer(Builder builder) {
        super(builder);
        this.randomBound = builder.randomBound;
        this.randomBoundUnit = builder.randomBoundUnit;
    }

    @Override
    public RandomizedTimer.Builder toBuilder() {
        return RandomizedTimer.Builder.builder(getTime(), getTimeUnit())
                .extraTime(getExtraTime(), getExtraTimeUnit())
                .randomBound(getRandomBound(), getRandomBoundUnit())
                .condition(getCondition());
    }

    @Override
    public Timer copy() {
        return toBuilder().build();
    }

    @Override
    public void reset() {
        super.setTicks(getTime() * getTimeUnit().getDurationTicks()
                + MathUtil.inRange(RANDOM, randomBound) * randomBoundUnit.getDurationTicks()
        );
    }

    @Override
    public Timer withCondition(@NotNull Predicate<ServerPlayerEntity> condition) {
        return toBuilder().condition(condition).build();
    }

    public int getRandomBound() {
        return randomBound;
    }

    public TimeUnit getRandomBoundUnit() {
        return randomBoundUnit;
    }

    public static class Builder extends Timer.Builder {
        private int randomBound = 0;
        private TimeUnit randomBoundUnit = TimeUnit.TICK;

        public Builder(int time, TimeUnit timeUnit) {
            super(time, timeUnit);
        }

        public static RandomizedTimer.Builder builder(int time, TimeUnit timeUnit) {
            return new Builder(time, timeUnit);
        }

        @Override
        public RandomizedTimer.Builder timeTicks(int timeTicks) {
            return time(timeTicks, TimeUnit.TICK);
        }

        @Override
        public RandomizedTimer.Builder time(int time, TimeUnit timeUnit) {
            super.time(time, timeUnit);
            return this;
        }

        @Override
        public RandomizedTimer.Builder extraTicks(int extraTicks) {
            return extraTime(extraTicks, TimeUnit.TICK);
        }

        @Override
        public RandomizedTimer.Builder extraTime(int extraTime, TimeUnit extraTimeUnit) {
            super.extraTime(extraTime, extraTimeUnit);
            return this;
        }

        @Override
        public RandomizedTimer.Builder condition(@NotNull Predicate<ServerPlayerEntity> condition) {
            super.condition(condition);
            return this;
        }

        public RandomizedTimer.Builder randomBoundTicks(int randomBoundTicks) {
            return randomBound(randomBoundTicks, TimeUnit.TICK);
        }

        public RandomizedTimer.Builder randomBound(int randomBound, TimeUnit randomBoundUnit) {
            this.randomBound = randomBound;
            this.randomBoundUnit = randomBoundUnit;
            return this;
        }

        @Override
        public RandomizedTimer build() {
            return new RandomizedTimer(this);
        }
    }
}
