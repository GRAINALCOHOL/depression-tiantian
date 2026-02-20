package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class DistanceMovedTopic extends StatTopic {
    public DistanceMovedTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute distanceMoved = getDefaultContextAttribute().toBuilder()
                .translationKey("distance_moved")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.DISTANCE_MOVED))
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.05)
                .build();

        return Optional.of(distanceMoved);
    }
}
