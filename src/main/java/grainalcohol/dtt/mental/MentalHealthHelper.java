package grainalcohol.dtt.mental;

import grainalcohol.dtt.util.MathUtil;
import net.depression.mental.MentalStatus;
import net.minecraft.text.Text;

import java.util.Random;

public class MentalHealthHelper {
    private static final Random RANDOM = new Random();

    public static final double MIN_MENTAL_HEALTH_VALUE = 0.0;
    public static final double MAX_MENTAL_HEALTH_VALUE = 100.0;

    public static final double BETWEEN_HEALTHY_AND_MILD = 70.0;
    public static final double BETWEEN_MILD_AND_MODERATE = 40.0;
    public static final double BETWEEN_MODERATE_AND_MAJOR = 20.0;

    public static double getMentalHealthRate(double mentalHealthValue) {
        if (mentalHealthValue < MIN_MENTAL_HEALTH_VALUE) {
            return 0.0;
        } else if (mentalHealthValue > MAX_MENTAL_HEALTH_VALUE) {
            return 1.0;
        } else {
            return (mentalHealthValue / MAX_MENTAL_HEALTH_VALUE);
        }
    }

    public static Text getAssessmentText(MentalHealthStatus mentalHealthStatus) {
        return Text.translatable("mental.assessment.dtt." + mentalHealthStatus.getName());
    }

    public static boolean shouldTriggerAnorexia(MentalStatus mentalStatus, boolean isMania) {
        return shouldTriggerAnorexia(MentalIllnessStatus.from(mentalStatus), isMania);
    }

    public static boolean shouldTriggerAnorexia(MentalIllnessStatus mentalIllnessStatus, boolean isMania) {
        return switch (mentalIllnessStatus) {
            case NONE,HEALTHY -> false;
            case MILD_DEPRESSION -> MathUtil.chance(RANDOM, 0.05);
            case MODERATE_DEPRESSION -> MathUtil.chance(RANDOM, 0.25);
            case MAJOR_DEPRESSIVE_DISORDER -> MathUtil.chance(RANDOM, 0.8);
            case BIPOLAR_DISORDER -> {
                if (isMania) {
                    yield true;
                } else {
                    yield MathUtil.chance(RANDOM, 0.8);
                }
            }
        };
    }
}
