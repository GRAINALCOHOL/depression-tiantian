package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class RaidTopic extends EssentialTopic {
    public RaidTopic(Identifier identifier) {
        super(identifier, false);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        boolean hasRaidFailed = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.RAID_FAILED);
        boolean hasRaidWon = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.RAID_WON);

        ContextAttribute raidFailed = getDefaultContextAttribute().toBuilder()
                .translationKey("raid_failed")
                .extremeNegativity()
                .feelingCompatibility(Feeling.FAILED, 0.6)
                .feelingCompatibility(Feeling.NIHILISTIC, 0.05)
                .build();

        ContextAttribute raidWon = getDefaultContextAttribute().toBuilder()
                .translationKey("raid_won")
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.6)
                .feelingCompatibility(Feeling.FAILED, -0.1)
                .build();

        // 优先展示突袭失败
        if (hasRaidFailed && !gentleMode) {
            return Optional.of(raidFailed);
        }

        return hasRaidWon ? Optional.of(raidWon) : Optional.empty();
    }
}
