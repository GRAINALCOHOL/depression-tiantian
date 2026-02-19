package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.dailystat.DailyStat;
import grainalcohol.dtt.diary.dailystat.DailyStatManager;
import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class WeatherTopic extends EssentialTopic {
    private final ContextAttribute CLEAR = ContextAttribute.Builder
            .builder("weather_clear", getIdentifier())
            .weight(getDefaultWeight())
            .build();
    private final ContextAttribute RAIN = ContextAttribute.Builder
            .builder("weather_rain", getIdentifier())
            .weight(getDefaultWeight())
            .build();

    public WeatherTopic(Identifier identifier) {
        super(identifier, false);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player) {
        boolean hasRained = DailyStatManager.getTodayResult(player.getUuid(), DailyStat::isHasRained);

        if (hasRained) {
            return Optional.of(RAIN.toBuilder()
                    .feelingCompatibility(Feeling.NIHILISTIC, 0.2)
                    .build()
            );
        } else {
            return Optional.of(CLEAR.toBuilder()
                    .feelingCompatibility(Feeling.WARM, 0.2)
                    .build()
            );
        }
    }
}
