package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PotionBrewedTopic extends StatTopic {
    public PotionBrewedTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute brewed = getDefaultContextAttribute().toBuilder()
                .translationKey("potion_brewed")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.POTION_BREWED))
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.2)
                .feelingCompatibility(Feeling.WARM, 0.01)
                .build();

        return Optional.of(brewed);
    }
}
