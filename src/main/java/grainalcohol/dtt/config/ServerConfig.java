package grainalcohol.dtt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import grainalcohol.dtt.diary.DiaryContentHandler;
import grainalcohol.dtt.diary.DiaryParagraph;
import grainalcohol.dtt.diary.feeling.FeelingProducer;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.mixin.modification.ClientTickEventListenerMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

public class ServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG = new File(DTTConfig.CONFIG_DIR, "server.json");

    /**
     * 默认true，启用后使用增强的日记生成器
     * @see DiaryContentHandler
     */
    public boolean useEnhancedDiaryGenerator = true;

    /**
     * 默认false，启用后禁用精神特质选择界面
     * @see ClientTickEventListenerMixin
     */
    public boolean disableMentalTraitSelectScreen = false;

    /**
     * 默认0.5，指数移动平均（EMA）因子，范围0.0到1.0
     * 越接近1.0，表示对最新数据的重视程度越高
     * @see TopicWeightCalculator
     */
    public double EMAFactor = 0.5;

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
     * 默认false，启用后战斗状态下不会因为病情或困意闭眼/打盹
     */
    public boolean saferCombat = false;

    /**
     * 默认5，战斗状态下被阻止闭眼的最大次数，超过后强制闭眼/打盹一次，设置为-1则不限制闭眼次数
     */
    public int maximumPreventCloseEyesCount = 5;

    public String commentForDamageType = "首先，我要在这屎山上拉一坨新的，没有人能阻止我！" +
            "然后，如果你想阻止某些伤害类型触发PTSD，就把你需要的伤害类型的id的“不带命名空间前缀，使用首字母小写驼峰命名法”版本添加进去，而且大概率只能使用原版的伤害类型，" +
            "比如：minecraft:generic_kill 应该写成 genericKill。" + "再然后，如果你想阻止某些实体触发PTSD，就把它们完整的实体id添加进去，" +
            "比如：minecraft:zombie，这里不需要改。" + "最后，如果你想阻止某些声音事件映射触发PTSD，就把它们的映射目标添加进去，就是depression/damagesource-sound-mao.toml里面的内容。" +
            "最后的最后，“player”伤害类型和实体id是永远无法被阻止的。";
    public Set<String> universalPTSDBlackList = Set.of(
            "genericKill",
            "minecraft:polar_bear"
    );

    public static ServerConfig load() {
        try {
            if (!DTTConfig.CONFIG_DIR.exists()) {
                Files.createDirectories(DTTConfig.CONFIG_DIR.toPath());
            }

            if (CONFIG.exists()) {
                try (FileReader reader = new FileReader(CONFIG)) {
                    return GSON.fromJson(reader, ServerConfig.class);
                } catch (IOException e) {
                    LOGGER.error("Failed to load server config file", e);
                }
            }

            ServerConfig config = new ServerConfig();
            config.save();
            return config;
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
            return new ServerConfig();
        }
    }

    public void save() {
        try {
            if (!DTTConfig.CONFIG_DIR.exists()) {
                Files.createDirectories(DTTConfig.CONFIG_DIR.toPath());
            }

            try (FileWriter writer = new FileWriter(CONFIG)) {
                GSON.toJson(this, writer);
            } catch (IOException e) {
                LOGGER.error("Failed to save server config file", e);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
    }
}
