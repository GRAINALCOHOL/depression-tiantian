package grainalcohol.dtt.diary.topic.v2;

import net.minecraft.util.Identifier;

public abstract class StatTopic extends Topic {
    public StatTopic(Identifier identifier) {
        super(identifier, false);
    }

    @Override
    public double getDefaultWeight() {
        return 0.0;
    }
}
