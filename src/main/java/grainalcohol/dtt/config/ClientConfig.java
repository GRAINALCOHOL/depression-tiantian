package grainalcohol.dtt.config;

import grainalcohol.dtt.mixin.event.ClientMentalIllnessMixin;

public class ClientConfig {
    /**
     * 默认60，单位为tick，触发闭眼效果后，客户端延迟多少tick开始展示视觉效果
     * @see ClientMentalIllnessMixin
     */
    public int close_eye_delay_ticks = 60;
    /**
     * 默认3，是否启用闭眼提示文案变体，设置为0或以下则不启用
     */
    public int close_eyes_message_variant_count = 3;
    /**
     * 默认3，是否启用精神疲劳提示文案变体，设置为0或以下则不启用
     */
    public int mental_fatigue_message_variant_count = 3;
}
