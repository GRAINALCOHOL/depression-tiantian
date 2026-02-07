package grainalcohol.dtt.diary.topic;

import grainalcohol.dtt.diary.feeling.Feeling;
import grainalcohol.dtt.util.MathUtil;
import net.minecraft.util.math.MathHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// TODO：不要用null表示话题不应该被应用了，考虑使用一个布尔值来表示话题无效
// TODO：另外我想用Builder模式
public class Topic {
    private final String name;
    private double weight; // 决定这个话题会不会被采纳，权重太低会被后续处理自动剔除
    private final TopicType type;
    private final boolean avoidRepetition;
    private boolean shouldExcludedDueToNegativity = false; // 因为消极属性被剔除，不影响该话题参与感受得分计算
    private final Map<Feeling, Double> feelingCompatibilityMap; // 话题与各种感受的符合度

    private Topic(TopicType type, String name, double weight, boolean avoidRepetition) {
        this.type = type;
        this.name = name;
        this.weight = weight;
        this.avoidRepetition = avoidRepetition;
        this.feelingCompatibilityMap = new EnumMap<>(Feeling.class);
        for (Feeling feeling : Feeling.values()) {
            this.feelingCompatibilityMap.put(feeling, 0.0);
        }
    }

    public static Topic of(TopicType type, String name, double weight, boolean avoidRepetition) {
        return new Topic(type, name, weight, avoidRepetition);
    }

    public static Topic of(TopicType type, String name) {
        return of(type, name, type.getDefaultWeight(), false);
    }

    public static Topic of(TopicType type, String name, double weight) {
        return of(type, name, weight, false);
    }

    public static Topic of(TopicType type, String name, boolean avoidRepetition) {
        return of(type, name, type.getDefaultWeight(), avoidRepetition);
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    public TopicType getType() {
        return type;
    }

    public boolean shouldAvoidRepetition() {
        return avoidRepetition;
    }

    public Map<Feeling, Double> getFeelingCompatibilityMap() {
        return feelingCompatibilityMap;
    }

    public void setFeelingCompatibility(Feeling feeling, double feelingCompatibility) {
        feelingCompatibility = MathHelper.clamp(feelingCompatibility, 0, 1);
        getFeelingCompatibilityMap().put(feeling, feelingCompatibility);
    }

    public double getFeelingCompatibility(Feeling feeling) {
        return getFeelingCompatibilityMap().get(feeling);
    }

    public boolean isShouldExcludedDueToNegativity() {
        return shouldExcludedDueToNegativity;
    }

    public void setShouldExcludedDueToNegativity(boolean shouldExcludedDueToNegativity) {
        this.shouldExcludedDueToNegativity = shouldExcludedDueToNegativity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return Objects.equals(getName(), topic.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
