package grainalcohol.dtt.diary.topic;

public enum TopicType {
    // essential类型会在正文生成时被采纳，不会被去重
    ESSENTIAL(10.0),
    // stat类型不会在正文生成时被采纳，也不会被去重（因为要参与感受总结）
    STAT(1.0),
    // major impact类型会在正文生成时被采纳，但会被去重
    MAJOR_IMPACT(8.0)
    ;

    private final double defaultWeight;

    TopicType(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    /**
     * 权重范围总是在(-10,+10)内<br>
     * 详见{@see TopicWeightCalculator}
     * @return 默认权重
     */
    public double getDefaultWeight() {
        return defaultWeight;
    }
}
