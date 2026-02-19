package grainalcohol.dtt.diary.topic.v2.topics;

import grainalcohol.dtt.diary.feeling.v2.Feeling;
import grainalcohol.dtt.diary.topic.v2.ContextAttribute;
import grainalcohol.dtt.diary.topic.v2.EssentialTopic;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class EnderDragonKilledTopic extends EssentialTopic {
    private final ContextAttribute ENDER_DRAGON_KILLED = ContextAttribute.Builder
            .builder("ender_dragon_killed", getIdentifier())
            .weight(getDefaultWeight())
            .build();

    public EnderDragonKilledTopic(Identifier identifier) {
        super(identifier, true);
    }

    @Override
    public Optional<ContextAttribute> getAttribute(ServerPlayerEntity player) {
        ContextAttribute ret = ENDER_DRAGON_KILLED.toBuilder()
                .weight(5.0)
                .feelingCompatibility(Feeling.SUCCESSFUL, 0.6)
                .feelingCompatibility(Feeling.FAILED, -0.1)
                .build();

        return Optional.of(ret);
    }
}
