package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class DamageTakenTopic extends StatTopic {
    public DamageTakenTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute damageTaken = getDefaultContextAttribute().toBuilder()
                .translationKey("damage_taken")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.DAMAGE_TAKEN))
                .feelingCompatibility(Feeling.FAILED, 0.6)
                .feelingCompatibility(Feeling.NIHILISTIC, 0.02)
                .build();
        return Optional.of(damageTaken);
    }
}
