package grainalcohol.dtt.util;

import java.util.Random;

public class MathUtil {
    /**
     * @return Whether the value is in the [min, max) range
     */
    public static boolean isBetween(int value, int min, int max) {
        return (value >= min && value < max);
    }

    public static boolean isBetween(double value, double min, double max) {
        return (value >= min && value < max);
    }

    public static boolean chance(Random random, double chance) {
        return random.nextDouble() < chance;
    }

    public static int inRange(Random random, int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static int dice(Random random, int num, int faces) {
        int result = 0;
        for(int i = 0; i < num; ++i) {
            result += random.nextInt(faces) + 1;
        }
        return result;
    }
}
