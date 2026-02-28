package grainalcohol.dtt.config;

import com.google.gson.annotations.SerializedName;
import grainalcohol.dtt.mixin.event.ClientMentalIllnessMixin;
import grainalcohol.dtt.mixin.modification.ClientActionbarHintMixin;

public class ClientConfig {
    @SerializedName("message_variant_config")
    public MessageVariantConfig messageVariantConfig = new MessageVariantConfig();
    @SerializedName("message_display_config")
    public MessageDisplayConfig messageDisplayConfig = new MessageDisplayConfig();
    @SerializedName("visual_config")
    public VisualConfig visualConfig = new VisualConfig();

    /**
     * 文案变体相关配置，用于增加提示文案的多样性，避免过于单调和重复
     */
    public static class MessageVariantConfig {
        /**
         * 默认3，闭眼提示文案变体数量，设置为0或以下则不启用
         * @see ClientMentalIllnessMixin
         */
        @SerializedName("close_eyes_message_variant_count")
        public int closeEyesMessageVariantCount = 3;
        /**
         * 默认3，精神疲劳提示文案变体数量，设置为0或以下则不启用
         * @see ClientActionbarHintMixin
         */
        @SerializedName("mental_fatigue_message_variant_count")
        public int mentalFatigueMessageVariantCount = 3;
    }

    /**
     * 提示消息显示相关配置
     */
    public static class MessageDisplayConfig {
        /**
         * 默认true，是否启用增强的击杀实体提示消息，即根据情绪值显示不同的文案
         * @see ClientActionbarHintMixin
         */
        @SerializedName("enhanced_kill_entity_message")
        public boolean enhancedKillEntityMessage = true;
        /**
         * depression原版为1200，默认4800，单位为tick，客户端每隔多少tick显示一次情绪恢复提示<br>
         * 设置1200以下会被视为1200，避免过于频繁
         * @see ClientActionbarHintMixin
         */
        @SerializedName("heal_message_interval_ticks")
        public int healMessageIntervalTicks = 4800; // 4分钟
        /**
         * 默认true，是否启用增强的PTSD形成提示消息
         */
        @SerializedName("enhanced_ptsd_formation_message")
        public boolean enhancedPTSDFormationMessage = true;
        /**
         * 默认true，是否启用增强的PTSD消散提示消息
         */
        @SerializedName("enhanced_ptsd_dispersal_message")
        public boolean enhancedPTSDDispersalMessage = true;
        /**
         * 默认true，是否启用增强的PTSD缓解提示消息
         */
        @SerializedName("enhanced_ptsd_remission_message")
        public boolean enhancedPTSDRemissionMessage = true;
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
        @SerializedName("close_eye_delay_ticks")
        public int closeEyeDelayTicks = 60;
        /**
         * 默认false，是否禁用触发PTSD喘息时的屏幕摇晃效果
         * @see grainalcohol.dtt.mixin.modification.ClientPTSDManagerMixin
         */
        @SerializedName("disable_panting_visual_animation")
        public boolean disablePantingVisualAnimation = false;
    }
}
