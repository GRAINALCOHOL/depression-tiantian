package grainalcohol.dtt.config;

import grainalcohol.dtt.mixin.event.ClientMentalIllnessMixin;
import grainalcohol.dtt.mixin.modification.ClientActionbarHintMixin;

public class ClientConfig {
    public MessageVariantConfig message_variant_config = new MessageVariantConfig();
    public MessageDisplayConfig message_display_config = new MessageDisplayConfig();
    public VisualConfig visual_config = new VisualConfig();

    /**
     * 文案变体相关配置，用于增加提示文案的多样性，避免过于单调和重复
     */
    public static class MessageVariantConfig {
        /**
         * 默认3，闭眼提示文案变体数量，设置为0或以下则不启用
         * @see ClientMentalIllnessMixin
         */
        public int close_eyes_message_variant_count = 3;
        /**
         * 默认3，精神疲劳提示文案变体数量，设置为0或以下则不启用
         * @see ClientActionbarHintMixin
         */
        public int mental_fatigue_message_variant_count = 3;
    }

    /**
     * 提示消息显示相关配置
     */
    public static class MessageDisplayConfig {
        /**
         * 默认true，是否启用增强的击杀实体提示消息，即根据情绪值显示不同的文案
         * @see ClientActionbarHintMixin
         */
        public boolean enhanced_kill_entity_message = true;
        /**
         * depression原版为1200，默认4800，单位为tick，客户端每隔多少tick显示一次情绪恢复提示<br>
         * 设置1200以下会被视为1200，避免过于频繁
         * @see ClientActionbarHintMixin
         */
        public int heal_message_interval_ticks = 4800; // 4分钟
        /**
         * 默认true，是否启用增强的PTSD形成提示消息
         */
        public boolean enhanced_ptsd_formation_message = true;
        /**
         * 默认true，是否启用增强的PTSD消散提示消息
         */
        public boolean enhanced_ptsd_dispersal_message = true;
        /**
         * 默认true，是否启用增强的PTSD缓解提示消息
         */
        public boolean enhanced_ptsd_remission_message = true;
    }

    /**
     * 视觉效果相关配置
     * @see net.depression.client
     */
    public static class VisualConfig {
        /**
         * 默认60，单位为tick，触发闭眼效果后，客户端延迟多少tick开始展示视觉效果
         * @see ClientMentalIllnessMixin
         */
        public int close_eye_delay_ticks = 60;
        /**
         * 默认false，是否禁用触发PTSD喘息时的屏幕摇晃效果
         * @see grainalcohol.dtt.mixin.modification.ClientPTSDManagerMixin
         */
        public boolean disable_panting_visual_animation = false;
    }
}
