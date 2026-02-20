package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.TopicWeightCalculator;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class AnimalBredTopic extends StatTopic {
    public AnimalBredTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        ContextAttribute animalBred = getDefaultContextAttribute().toBuilder()
                .translationKey("animal_bred")
                .weight(TopicWeightCalculator.calculateWeight(player.getUuid(), DTTDailyStat.ANIMAL_BRED))
                .feelingCompatibility(Feeling.WARM, 0.2)
                .feelingCompatibility(Feeling.FAILED, -0.1)
                .build();

        return Optional.of(animalBred);
    }
}
