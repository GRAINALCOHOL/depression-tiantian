package grainalcohol.dtt.hint;

import net.minecraft.util.Identifier;

public abstract class SimpleHintMessage extends HintMessage {
    public SimpleHintMessage(Identifier identifier, boolean isImportant, int variantCount) {
        super(identifier, isImportant, variantCount);
    }

    @Override
    public boolean autoSend() {
        return true;
    }
}
