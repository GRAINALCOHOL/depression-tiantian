package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class EnderDragonKilledTopic extends EssentialTopic {
    public EnderDragonKilledTopic(Identifier identifier) {
        super(identifier, true);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        boolean condition = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.ENDER_DRAGON_KILLED);

        ContextAttribute enderDragonKilled = getDefaultContextAttribute().toBuilder()
                .translationKey("ender_dragon_killed")
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.6)
                .feelingCompatibility(Feeling.FAILED, -0.2)
                .build();

        return condition ? Optional.of(enderDragonKilled) : Optional.empty();
    }
}
