package grainalcohol.dtt.diary.topic;

import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.diary.feeling.Feeling;
import net.depression.item.diary.ConditionComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 顺序生成话题的方法是{@code generateTopicProcess()}
 * @see TopicProducer
 */
public class Topics {
    // TODO：改为注册表模式方便扩展
    // essential
    public static final Function<ServerPlayerEntity, Topic> weather = player -> {
        boolean hasRain = player.getWorld().hasRain(player.getBlockPos().withY(319));
        // +2
        Topic topic = hasRain ? Topic.of(TopicType.ESSENTIAL, "weather_rain") : Topic.of(TopicType.ESSENTIAL, "weather_clear");
        // 下雨会让人感到孤独
        topic.setFeelingCompatibility(hasRain ? Feeling.LONELY : Feeling.WARM, 0.6);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> petDied = player -> {
        DailyStat dailyStat = DailyStatManager.getTodayDailyStat(player.getUuid());
        // +1
        Topic topic = dailyStat.isHasPetDied() ? Topic.of(TopicType.ESSENTIAL, "pet_died", true) : null;
        // 宠物死亡会让人感到孤独、虚无和失败
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.LONELY, 0.6);
            topic.setFeelingCompatibility(Feeling.NIHILISTIC, 0.2);
            topic.setFeelingCompatibility(Feeling.FAILED, 0.8);
            topic.setShouldExcludedDueToNegativity(true);
        }
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> petBred = player -> {
        DailyStat dailyStat = DailyStatManager.getTodayDailyStat(player.getUuid());
        // +1
        Topic topic = dailyStat.isHasPetBred() ? Topic.of(TopicType.ESSENTIAL, "pet_bred") : null;
        // 繁殖宠物会让人感到温暖和成功
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.WARM, 0.6);
            topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 0.8);
        }
        return topic;
    };

    //stat
    public static final Function<ServerPlayerEntity, Topic> animalBred = player -> {
        String name = new ConditionComponent(Stats.ANIMALS_BRED, 1, "animal_bred").get(player);
        Topic topic = !name.isEmpty() ? Topic.of(TopicType.STAT, name) : null;
        // 喂养动物会让人感到温暖和有希望
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.WARM, 0.2);
            topic.setFeelingCompatibility(Feeling.HOPEFUL, 0.4);
        }
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> monsterKilled = player -> {
        Topic topic = createStatTopic(player, DailyStat::getMonsterKilled, "monster_killed");
        // 杀死怪物会让人感到成功
        topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 0.2);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> distanceMoved = player -> {
        Topic topic = createStatTopic(player, DailyStat::getDistanceMoved, "distance_moved");
        // 行走距离这个太抽象了，也比较吃环境，就设置一点点符合度吧
        topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 0.05);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> damageTaken = player -> {
        Topic topic = createStatTopic(player, DailyStat::getDamageTaken, "damage_taken");
        // 受到伤害会让人感到失败
        topic.setFeelingCompatibility(Feeling.FAILED, 0.2);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> traded = player -> {
        Topic topic = createStatTopic(player, DailyStat::getTradedCount, "traded");
        // 交易会让人感到成功
        topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 0.1);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> brewed = player -> {
        Topic topic = createStatTopic(player, DailyStat::getBrewedCount, "brewed");
        // 酿造药水会让人感到有希望
        topic.setFeelingCompatibility(Feeling.HOPEFUL, 0.1);
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> flowerPotted = player -> {
        Topic topic = createBooleanStatTopic(player, DailyStat::isHasFlowerPotted, "flower_potted");
        // 种花会让人感到温暖和有希望
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.WARM, 0.6);
            topic.setFeelingCompatibility(Feeling.HOPEFUL, 0.4);
        }
        return topic;
    };

    public static final Function<ServerPlayerEntity, Topic> ate = player -> {
        Topic topic = createBooleanStatTopic(player, DailyStat::isHasAte, "ate");
        // 进食会让人感到温暖和有希望
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.WARM, 0.2);
        }
        return topic;
    };

    // major impact
    public static final Function<ServerPlayerEntity, Topic> enderDragonKilled = player -> {
        int dragonKilled = player.getStatHandler().getStat(Stats.KILLED, EntityType.ENDER_DRAGON);
        // +1
        Topic topic = dragonKilled > 0 ? Topic.of(TopicType.MAJOR_IMPACT, "ender_dragon_killed", true) : null;
        // 击杀末影龙会让人感到成功
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.HOPEFUL, 0.6);
            topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 1.0);
        }
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> raidFailed = player -> {
        Topic topic = createBooleanStatTopic(player, TopicType.MAJOR_IMPACT, DailyStat::isHasRaidFailed, "raid_failed");
        // 袭击失败会让人感到失败和虚无
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.FAILED, 0.6);
            topic.setFeelingCompatibility(Feeling.NIHILISTIC, 0.2);
            topic.setShouldExcludedDueToNegativity(true);
        }
        return topic;
    };
    public static final Function<ServerPlayerEntity, Topic> raidWon = player -> {
        Topic topic = createBooleanStatTopic(player, TopicType.MAJOR_IMPACT, DailyStat::isHasRaidWon, "raid_won");
        // 赢得袭击会让人感到成功和有希望
        if (topic != null) {
            topic.setFeelingCompatibility(Feeling.SUCCESSFUL, 0.6);
            topic.setFeelingCompatibility(Feeling.HOPEFUL, 0.2);
        }
        return topic;
    };

    private static Topic createStatTopic(
            ServerPlayerEntity player,
            Function<DailyStat, Integer> dataExtractor,
            String name
    ) {
        return createStatTopic(player, TopicType.STAT, dataExtractor, name);
    }

    private static Topic createStatTopic(
            ServerPlayerEntity player,
            TopicType topicType,
            Function<DailyStat, Integer> dataExtractor,
            String name
    ) {
        int today = dataExtractor.apply(DailyStatManager.getTodayDailyStat(player.getUuid()));
        int yesterday = dataExtractor.apply(DailyStatManager.getYesterdayDailyStat(player.getUuid()));
        int ema = dataExtractor.apply(DailyStatManager.getMovingAverageDailyStat(player.getUuid()));

        double weight = TopicWeightCalculator.calculateWeight(today, yesterday, ema);

        return Topic.of(topicType, name, weight, false);
    }

    @Nullable
    private static Topic createBooleanStatTopic(
            ServerPlayerEntity player,
            Function<DailyStat, Boolean> dataExtractor,
            String name
    ) {
        return createBooleanStatTopic(player, TopicType.STAT, dataExtractor, name);
    }

    @Nullable
    private static Topic createBooleanStatTopic(
            ServerPlayerEntity player,
            TopicType topicType,
            Function<DailyStat, Boolean> dataExtractor,
            String name
    ) {
        boolean isHappened = dataExtractor.apply(DailyStatManager.getTodayDailyStat(player.getUuid()));

        // 布尔类型话题无法计算权重，统一设为5.0
        return isHappened ? Topic.of(topicType, name, 5.0, false) : null;
    }
}
