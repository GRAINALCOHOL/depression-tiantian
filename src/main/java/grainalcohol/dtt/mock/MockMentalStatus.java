package grainalcohol.dtt.mock;

import grainalcohol.dtt.mixin.accessor.BoredomMapAccessor;
import net.depression.mental.MentalStatus;
import net.depression.util.Tools;

import java.util.concurrent.ConcurrentHashMap;

/**
 * <h1>MockMentalStatus</h1>
 * 用于模拟精神状态（Mental Status）的类。<br>
 * 该类复制了一个精神状态的快照，并提供了模拟精神治疗效果的方法。<br>
 * @author grainalcohol
 * @since 2026-02-09
 */
public class MockMentalStatus {
    private double emotionValue;
    private double mentalHealthValue;
    private int mentalHealthId;
    private final ConcurrentHashMap<String, Integer> boredom;

    private MockMentalStatus(MentalStatus mentalStatus) {
        this.boredom = new ConcurrentHashMap<>();

        this.emotionValue = mentalStatus.emotionValue;
        this.mentalHealthValue = mentalStatus.mentalHealthValue;
        this.mentalHealthId = mentalStatus.getMentalHealthId();
        this.boredom.putAll(((BoredomMapAccessor) mentalStatus).getBoredomMap());
    }

    public static MockMentalStatus copyOf(MentalStatus mentalStatus) {
        return new MockMentalStatus(mentalStatus);
    }

    public double mockMentalHeal(double value) {
        if (this.getMentalHealthId() == 4) {
            return 0.0;
        } else {
            double toReturn = value * getMentalHealthValue() / 100.0;
            this.emotionValue += toReturn;
            this.emotionValue = Math.min(20.0, this.emotionValue);
            return toReturn;
        }
    }

    public double mockMentalHeal(String string, double value, int count) {
        Integer i = this.boredom.get(string);
        if (i == null) {
            i = 0;
        }

        this.boredom.put(string, i + count);
        return mockMentalHeal((Tools.getHarmonic(i + count) - Tools.getHarmonic(i)) * value);
    }

    public double mockMentalHeal(String string, double value) {
        if (this.boredom.containsKey(string)){
            this.boredom.put(string, this.boredom.get(string) + 1);
        } else {
            this.boredom.put(string, 2);
        }

        value /= (double) this.boredom.get(string) / 2.0;
        return mockMentalHeal(value);
    }

    public void setEmotionValue(double emotionValue) {
        this.emotionValue = emotionValue;
    }

    public double getEmotionValue() {
        return emotionValue;
    }

    public double getMentalHealthValue() {
        return mentalHealthValue;
    }

    public void setMentalHealthValue(double mentalHealthValue) {
        this.mentalHealthValue = mentalHealthValue;
    }

    public int getMentalHealthId() {
        return mentalHealthId;
    }

    public void setMentalHealthId(int mentalHealthId) {
        this.mentalHealthId = mentalHealthId;
    }
}
