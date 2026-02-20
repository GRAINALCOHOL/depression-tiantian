package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class TradedTopic extends StatTopic {
    public TradedTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute traded = getDefaultContextAttribute().toBuilder()
                .translationKey("traded")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.TRADED_COUNT))
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.6)
                .build();

        return Optional.of(traded);
    }
}
