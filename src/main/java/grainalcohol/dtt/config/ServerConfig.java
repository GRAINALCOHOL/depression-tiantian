package grainalcohol.dtt.config;

import grainalcohol.dtt.diary.DiaryContentHandler;
import grainalcohol.dtt.diary.DiaryParagraph;
import grainalcohol.dtt.diary.feeling.FeelingProducer;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.mixin.PlayerEntityMixin;
import grainalcohol.dtt.mixin.client.MouseMixin;
import grainalcohol.dtt.mixin.modification.ClientTickEventListenerMixin;
import grainalcohol.dtt.mixin.modification.MentalHealthScaleItemMixin;
import grainalcohol.dtt.mixin.modification.PlayerEventListenerMixin;
import grainalcohol.dtt.mixin.modification.VillageAdditionsMixin;

import java.util.Set;

public class ServerConfig {
    public VillagerConfig villagerConfig = new VillagerConfig();
    public DiaryConfig diaryConfig = new DiaryConfig();
    public CombatConfig combatConfig = new CombatConfig();
    public CommonConfig commonConfig = new CommonConfig();
    public ItemConfig itemConfig = new ItemConfig();

    public static class VillagerConfig {
        /**
         * 默认true，启用后心理医生（下简称村民）的商品价格会随供需关系浮动
         * @see VillageAdditionsMixin
         */
        public boolean enablePriceFloating = true;

        /**
         * 村民售卖精神健康量表的所需等级
         * 精神健康量表的基础价格
         * 精神健康量表的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfMentalHealthScale = 1;
        public int basePriceOfMentalHealthScale = 6;
        public int maxUsesOfMentalHealthScale = 8;

        /**
         * 村民售卖轻度抗抑郁药片的所需等级
         * 轻度抗抑郁药片的基础价格
         * 轻度抗抑郁药片的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfMildDepressionTablet = 1;
        public int basePriceOfMildDepressionTablet = 20;
        public int maxUsesOfMildDepressionTablet = 10;

        /**
         * 村民售卖安眠药片的所需等级
         * 安眠药片的基础价格
         * 安眠药片的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfInsomniaTablet = 2;
        public int basePriceOfInsomniaTablet = 30;
        public int maxUsesOfInsomniaTablet = 10;

        /**
         * 村民售卖中度抗抑郁药片的所需等级
         * 中度抗抑郁药片的基础价格
         * 中度抗抑郁药片的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfModerateDepressionTablet = 2;
        public int basePriceOfModerateDepressionTablet = 30;
        public int maxUsesOfModerateDepressionTablet = 10;

        /**
         * 村民售卖重度抗抑郁胶囊的所需等级
         * 重度抗抑郁胶囊的基础价格
         * 重度抗抑郁胶囊的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfMDDCapsule = 3;
        public int basePriceOfMDDCapsule = 40;
        public int maxUsesOfMDDCapsule = 10;

        /**
         * 村民售卖抗躁狂药片的所需等级
         * 抗躁狂药片的基础价格
         * 抗躁狂药片的最大库存量
         * @see VillageAdditionsMixin
         */
        public int lowestLevelOfManiaTablet = 3;
        public int basePriceOfManiaTablet = 40;
        public int maxUsesOfManiaTablet = 10;
    }

    public static class DiaryConfig {
        /**
         * 默认true，启用后使用增强的日记生成器
         * @see DiaryContentHandler
         */
        public boolean useEnhancedDiaryGenerator = true;

        /**
         * 默认3，日记本地化时查找的变体数量
         * @see DiaryContentHandler#findRandomVariant(String, int)
         */
        public int diaryTranslationKeyVariantCount = 3;

        /**
         * 默认false，启用后日记内容 <b>可能</b> 会变得更积极<br>
         * 其一：是如果消极感受得分高于积极感受得分，则将消极感受得分压缩20%<br>
         * 其二：是在生成日记时排除被标记为“由于消极属性应该被排除”的话题
         * @see FeelingProducer
         * @see DiaryContentHandler#generateTranslationKey(DiaryParagraph, boolean)
         */
        public boolean makeDiarySlightlyMorePositive = false;

        /**
         * 默认0.5，指数移动平均（EMA）因子，范围0.0到1.0
         * 越接近1.0，表示对最新数据的重视程度越高
         * @see TopicWeightCalculator
         */
        public double EMAFactor = 0.5;
    }

    public static class ItemConfig {
        /**
         * 默认true，启用后使用精神健康量表将不再有冷却时间
         * @see MentalHealthScaleItemMixin
         */
        public boolean disableMentalHealthScaleCooldown = false;
        /**
         * 默认true，启用后精神健康量表回复的文案将更“合理”
         * @see MentalHealthScaleItemMixin
         */
        public boolean enhancedMentalHealthScaleAction = true;
        /**
         * 默认true，启用后精神健康量表在使用后会消失
         * @see MentalHealthScaleItemMixin
         */
        public boolean makeMentalHealthScalesDisposable = true;
    }

    public static class CombatConfig {
        /**
         * 默认false，启用后战斗状态下不会因为病情或困意闭眼/打盹
         */
        public boolean saferCombat = false;

        /**
         * 默认false，启用后无论情绪值如何，攻击或受击都将进入战斗状态
         * @see PlayerEventListenerMixin
         * @see PlayerEntityMixin
         */
        public boolean easierCombatState = false;
    }

    public static class CommonConfig {
        /**
         * 默认false，启用后禁用精神特质选择界面
         * @see ClientTickEventListenerMixin
         */
        public boolean disableMentalTraitSelectScreen = false;

        /**
         * 默认true，启用后战斗状态下如果处于紧张性木僵状态则强制固定视角方向
         * @see MouseMixin
         */
        public boolean shouldFixedFaceDirectionWhenCatatonicStupor = true;

        /**
         * 默认5，战斗状态下被阻止闭眼的最大次数，超过后强制闭眼/打盹一次，设置为-1则不限制闭眼次数
         */
        public int maximumPreventCloseEyesCount = 5;

        public String commentForBlackList = "首先，我要在这屎山上拉一坨新的，没有人能阻止我！" +
                "然后，如果你想阻止某些伤害类型触发PTSD，就把你需要的伤害类型的id的“不带命名空间前缀，使用首字母小写驼峰命名法”版本添加进去，而且大概率只能使用原版的伤害类型，" +
                "比如：minecraft:generic_kill 应该写成 genericKill。" + "再然后，如果你想阻止某些实体触发PTSD，就把它们完整的实体id添加进去，" +
                "比如：minecraft:zombie，这里不需要改。" + "最后，如果你想阻止某些声音事件映射触发PTSD，就把它们的映射目标添加进去，就是config/depression/damagesource-sound-map.toml里面的内容。" +
                "最后的最后，“player”伤害类型和实体id是永远无法被阻止的。";
        public Set<String> universalPTSDBlackList = Set.of(
                "genericKill"
        );
    }
}
