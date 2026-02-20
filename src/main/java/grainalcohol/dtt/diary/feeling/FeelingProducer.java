package grainalcohol.dtt.diary.feeling;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.diary.topic.Topic;
import grainalcohol.dtt.diary.topic.TopicProducer;
import grainalcohol.dtt.diary.topic.TopicType;
import grainalcohol.dtt.mental.MentalHealthStatus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class FeelingProducer {
    /**
     * TODO：<br>
     * 0. 去给Topic类新增对每种感受/感受的“符合度”<br>
     * 1. 使用{@code statTopicList}生成 Feeling 枚举<br>
     * 2. 权重是统计符合度时的乘数，包含符号<br>
     * 3. 增加感受值影响话题得分效率的功能
     * 4. 可选的次要感受
     */
    private final MentalHealthStatus mentalHealthStatus;
    private final TopicProducer topicProducer;
    private final List<Feeling> feelingList;
    private final Map<Feeling, Double> scoresMap;

    public FeelingProducer(MentalHealthStatus mentalHealthStatus, TopicProducer topicProducer) {
        this.mentalHealthStatus = mentalHealthStatus;
        this.topicProducer = topicProducer;
        this.feelingList = new ArrayList<>();
        this.scoresMap = new EnumMap<>(Feeling.class);
        for (Feeling feeling : Feeling.values()) {
            getScoresMap().put(feeling, 0.0);
        }

        FeelingScoreProcess();
        // 根据得分生成、排序感受关键词
        generateFeelings();
    }

    private void generateFeelings() {
        for (Feeling positiveFeeling : Feeling.positiveFeelings()) {
            // 只需遍历积极感受，消极感受通过对立面处理
            Feeling negativeFeeling = positiveFeeling.getOppositeFeeling();

            double positiveScore = getScoresMap().get(positiveFeeling);
            double negativeScore = getScoresMap().get(negativeFeeling);

            if (positiveScore <= 0 && negativeScore <= 0) {
                // 都是非正分，跳过
                continue;
            }

            if (DTTConfig.getInstance().getServerConfig().diaryConfig.gentle_mode && negativeScore > positiveScore) {
                // 使日记稍微积极一点点
                negativeScore *= 0.8;
            }

            // 选择得分更高的感受加入列表
            getFeelingList().add(positiveScore > negativeScore ? positiveFeeling : negativeFeeling);
        }
        // 降序排序
        getFeelingList().sort((f1, f2) -> getScoresMap().get(f2).compareTo(getScoresMap().get(f1)));
        // 截取前两个
//        if (getFeelingList().size() > 2) {
//            getFeelingList().subList(2, getFeelingList().size()).clear();
//        }
    }

    private void FeelingScoreProcess() {
        // 计算并储存每种感受的得分
        // 话题负权重会同时少量地增长对立感受的得分
        // 抑郁状态会略微降低积极感受的得分效率，并略微提升消极感受的得分效率（略微），随严重情况提升而逐渐明显
        // 躁狂状态会明显提升积极感受的得分效率，并显降低消极感受的得分效率（明显）

        // 先循环Topic再循环Feeling，可能是个小优化
        // essential
        for (Topic topic : getTopicProducer().getTopicTypeMap().get(TopicType.ESSENTIAL)) {
            calculateScore(topic);
        }
        // stat
        for (Topic topic : getTopicProducer().getTopicTypeMap().get(TopicType.STAT)) {
            calculateScore(topic);
        }
        // major impact
        for (Topic topic : getTopicProducer().getTopicTypeMap().get(TopicType.MAJOR_IMPACT)) {
            calculateScore(topic);
        }
    }

    private void calculateScore(Topic topic) {
        double weight = topic.getWeight();
        if (weight == 0.0) return;
        for (Feeling feeling : Feeling.values()) {
            Feeling oppositeFeeling = feeling.getOppositeFeeling();

            // 符合度
            double compatibility = topic.getFeelingCompatibility(feeling);
            if (compatibility == 0.0) continue;

            // 得分 = 话题权重 * 符合度
            double score = Math.abs(weight) * compatibility;

            // 负权重时会同时少量增加对立感受的得分
            // 即使权重为负也并不代表该话题不存在，只是没有之前的数据那样强烈，因此不能忽略其符合的感受得分
            if (weight < 0) {
                scoreAdder(oppositeFeeling, score * 0.2); // 20%
                score *= 0.8; // 80%
            }

            // 应用心理健康状态对感受得分的影响
            score = handleMentalHealthInfluence(score, feeling);
            // 累加得分
            scoreAdder(feeling, score);
        }
    }

    private double handleMentalHealthInfluence(double score, Feeling feeling) {
        boolean isPositiveFeeling = feeling.isPositiveFeeling();
        MentalHealthStatus status = getMentalHealthStatus();
        double baseMultiplier = status.getFeelingInfluenceMultiplier();

        if (status.isNormal()) {
            return score;
        }

        if (status.isDepressed())  {
            // 抑郁
            score *= isPositiveFeeling ?
                    1.0 - baseMultiplier : // 略微降低积极感受得分效率
                    1.0 + baseMultiplier; // 略微提升消极感受得分效率
        } else if (status.isMania()) {
            // 躁狂
            score *= isPositiveFeeling ?
                    1.0 + baseMultiplier : // 明显提升积极感受得分效率
                    1.0 - baseMultiplier; // 明显降低消极感受得分效率
        }

        return score;
    }

    private void scoreAdder(Feeling feeling, double score) {
        getScoresMap().put(feeling, getScoresMap().get(feeling) + score);
    }

    private TopicProducer getTopicProducer() {
        return topicProducer;
    }

    private Map<Feeling, Double> getScoresMap() {
        return scoresMap;
    }

    private MentalHealthStatus getMentalHealthStatus() {
        return mentalHealthStatus;
    }

    public List<Feeling> getFeelingList() {
        return feelingList;
    }

    public Feeling getResult() {
        Feeling result = getFeelingList().get(0);
        return result == null ? Feeling.HOPEFUL : result;
    }
}
