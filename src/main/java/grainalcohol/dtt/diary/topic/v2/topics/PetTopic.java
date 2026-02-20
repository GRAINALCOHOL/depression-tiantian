package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PetTopic extends EssentialTopic {
    public PetTopic(Identifier identifier) {
        super(identifier, false);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        boolean hasPetDied = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.PET_DIED);
        boolean hasPetBred = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.PET_BRED);

        ContextAttribute petDied = getDefaultContextAttribute().toBuilder()
                .translationKey("pet_died")
                .extremeNegativity()
                .feelingCompatibility(Feeling.FAILED, 0.6)
                .feelingCompatibility(Feeling.NIHILISTIC, 0.1)
                .build();

        ContextAttribute petBred = getDefaultContextAttribute().toBuilder()
                .translationKey("pet_bred")
                .feelingCompatibility(Feeling.WARM, 0.4)
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.1)
                .build();

        // 优先展示宠物死亡
        if (hasPetDied && !gentleMode) {
            return Optional.of(petDied);
        }

        return hasPetBred ? Optional.of(petBred) : Optional.empty();
    }
}
