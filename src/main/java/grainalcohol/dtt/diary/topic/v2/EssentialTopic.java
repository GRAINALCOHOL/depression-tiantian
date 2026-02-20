package grainalcohol.dtt.diary.topic.v2;

import net.minecraft.util.Identifier;

public abstract class EssentialTopic extends Topic {
    public EssentialTopic(Identifier identifier, boolean avoidRepetitionFromYesterday) {
        super(identifier, avoidRepetitionFromYesterday);
    }

     @Override
    public double getDefaultWeight() {
        return 8.0;
    }
}
