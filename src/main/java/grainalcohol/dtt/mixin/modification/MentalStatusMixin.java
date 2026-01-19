package grainalcohol.dtt.mixin.modification;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.util.MiscUtil;
import net.depression.mental.MentalStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(MentalStatus.class)
public abstract class MentalStatusMixin {
    @Shadow public abstract void mentalHurt(double value);
    @Shadow @Final public ConcurrentHashMap<String, Double> PTSD;
    @Shadow @Final private ConcurrentHashMap<String, Long> PTSDTimeBuffer;
    @Shadow @Final private ConcurrentHashMap<String, Double> PTSDValueBuffer;
    @Shadow @Final public HashSet<String> playerPTSDSet;

    @Inject(method = "tick", at = @At("HEAD"))
    private void clearPTSDData(CallbackInfo ci) {
        Set<String> blacklist = DTTConfig.getInstance().getServerConfig().universalPTSDBlackList;
        MiscUtil.cleanMap(PTSD, blacklist::contains);
        MiscUtil.cleanMap(PTSDTimeBuffer, blacklist::contains);
        MiscUtil.cleanMap(PTSDValueBuffer, blacklist::contains);
        MiscUtil.cleanSet(playerPTSDSet, blacklist::contains);
    }

    @Inject(method = "mentalHurt(Ljava/lang/String;D)V", at = @At("HEAD"), cancellable = true)
    private void blockPTSDData(String id, double amount, CallbackInfo ci) {
        if (id.equals("player") || id.equals("minecraft:player")) {
            return;
        }
        if (DTTConfig.getInstance().getServerConfig().universalPTSDBlackList.contains(id)) {
            DTTMod.LOGGER.info("Blocked mental health value hurt by damage source but string: {}", id);
            mentalHurt(amount);
            ci.cancel();
        }
    }

    @Inject(method = "triggerHurt", at = @At("HEAD"), cancellable = true)
    private void blockPTSDTrigger(String id, CallbackInfo ci) {
        if (id.equals("player") || id.equals("minecraft:player")) {
            return;
        }
        if (DTTConfig.getInstance().getServerConfig().universalPTSDBlackList.contains(id)) {
            DTTMod.LOGGER.info("Blocked trigger ptsd from hurt by damage type source but string: {}", id);
            ci.cancel();
        }
    }
}
