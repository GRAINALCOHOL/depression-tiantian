package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.StatTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class FlowerPottedTopic extends StatTopic {
    public FlowerPottedTopic(Identifier identifier) {
        super(identifier);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        boolean condition = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.FLOWER_POTTED);

        ContextAttribute flowerPotted = getDefaultContextAttribute().toBuilder()
                .translationKey("flower_potted")
                .weight(2.0)
                .feelingCompatibility(Feeling.WARM, 0.2)
                .build();

        return condition ? Optional.of(flowerPotted) : Optional.empty();
    }
}
