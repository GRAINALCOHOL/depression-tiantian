package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class MonsterKilledTopic extends StatTopic {
    public MonsterKilledTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute monsterKilled = getDefaultContextAttributeBuilder()
                .translationKey("killed_monster")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.MONSTER_KILLED))
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.6)
                .feelingCompatibility(Feeling.WARM, 0.05)
                .build();

        return Optional.of(monsterKilled);
    }
}
