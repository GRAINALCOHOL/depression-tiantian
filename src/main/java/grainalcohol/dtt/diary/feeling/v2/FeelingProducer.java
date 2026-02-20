package grainalcohol.dtt.diary.feeling.v2;

import grainalcohol.dtt.diary.topic.v2.ContextAttribute;

import java.util.*;

public class FeelingProducer {
    private final boolean gentleMode;
    private final List<ContextAttribute> allContextAttributes;
    private final List<FeelingScore> feelingScoresList = new ArrayList<>();

    public FeelingProducer(List<ContextAttribute> allContextAttributes, boolean gentleMode) {
        this.gentleMode = gentleMode;
        this.allContextAttributes = allContextAttributes;

        for (Feeling feeling : Feeling.values()) {
            feelingScoresList.add(calculateFeelingScore(feeling));
        }
        // 根据分数从高到低排序
        feelingScoresList.sort((f1, f2) -> Double.compare(f2.score(), f1.score()));
    }

    private FeelingScore calculateFeelingScore(Feeling feeling) {
        double totalScore = 0.0;
        for (ContextAttribute contextAttribute : allContextAttributes) {
            double weight = contextAttribute.getWeight();
            double compatibility = contextAttribute.getFeelingCompatibility(feeling);

            totalScore += weight * compatibility;
            if (gentleMode && feeling.isNegative()) {
                // 如果配置了让日记稍微更积极一些，适当降低负面感受的分数
                totalScore -= totalScore * 0.15;
            }
        }

        return new FeelingScore(feeling, totalScore);
    }

    public Feeling getFeeling(int index) {
        return feelingScoresList.get(index).feeling();
    }

    public Feeling getFeeling() {
        return feelingScoresList.get(0).feeling();
    }

    private record FeelingScore(Feeling feeling, double score) {}
}
