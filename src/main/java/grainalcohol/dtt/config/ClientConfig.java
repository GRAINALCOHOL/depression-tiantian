package grainalcohol.dtt.config;

import grainalcohol.dtt.mixin.event.ClientMentalIllnessMixin;

public class ClientConfig {
    /**
     * 默认60，单位为tick，触发闭眼效果后，客户端延迟多少tick开始展示视觉效果
     * @see ClientMentalIllnessMixin
     */
    public int closeEyeDelayTicks = 60;
}
