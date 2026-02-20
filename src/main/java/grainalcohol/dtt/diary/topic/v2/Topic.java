package grainalcohol.dtt.diary.topic.v2;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public abstract class Topic {
    private final ContextAttribute defaultContextAttribute;
    private final Identifier identifier;
    private final boolean avoidRepetitionFromYesterday;

    public Topic(Identifier identifier, boolean avoidRepetitionFromYesterday) {
        this.identifier = identifier;
        this.avoidRepetitionFromYesterday = avoidRepetitionFromYesterday;
        defaultContextAttribute = ContextAttribute.Builder
                .builder(getIdentifier().getPath(), getIdentifier())
                .weight(getDefaultWeight())
                .build();
    }

    public ContextAttribute getDefaultContextAttribute() {
        return defaultContextAttribute;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public boolean shouldAvoidRepetitionFromYesterday() {
        return avoidRepetitionFromYesterday;
    }

    public abstract double getDefaultWeight();

    public abstract Optional<ContextAttribute> getAttribute(ServerPlayerEntity player, boolean gentleMode);
}
