package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class MonsterKilledTopic extends StatTopic {
    private final ContextAttribute KILLED_MONSTER = ContextAttribute.Builder
            .builder("killed_monster", getIdentifier())
            .weight(getDefaultWeight())
            .build();

    public MonsterKilledTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player) {
        ContextAttribute ret = KILLED_MONSTER.toBuilder()
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DailyStat::getMonsterKilled))
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.1)
                .feelingCompatibility(Feeling.WARM, 0.05)
                .build();

        return Optional.of(ret);
    }
}
