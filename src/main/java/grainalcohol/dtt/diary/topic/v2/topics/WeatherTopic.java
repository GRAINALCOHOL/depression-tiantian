package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.v2.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.init.DTTDailyStat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class WeatherTopic extends EssentialTopic {
    public WeatherTopic(Identifier identifier) {
        super(identifier, false);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode) {
        boolean hasRained = DailyStatManager.getTodayStat(player.getUuid()).getBooleanStat(DTTDailyStat.RAINED);

        ContextAttribute weatherRain = getDefaultContextAttribute().toBuilder()
                .translationKey("weather_rain")
                .feelingCompatibility(Feeling.NIHILISTIC, 0.2)
                .build();
        ContextAttribute weatherClear = getDefaultContextAttribute().toBuilder()
                .translationKey("weather_clear")
                .feelingCompatibility(Feeling.WARM, 0.2)
                .build();

        if (hasRained) {
            return Optional.of(weatherRain);
        } else {
            return Optional.of(weatherClear);
        }
    }
}
