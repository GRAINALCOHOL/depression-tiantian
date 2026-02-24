package grainalcohol.dtt.mixin.modification;

import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.util.MiscUtil;
import net.depression.mental.PTSDManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PTSDManager.class)
public class PTSDManagerMixin {
    @Shadow @Final private ConcurrentHashMap<String, Double> PTSD;
    @Shadow @Final private ConcurrentHashMap<String, Integer> contactValue;
    @Shadow @Final private ConcurrentHashMap<String, Integer> remainingValue;
    @Shadow @Final private ConcurrentHashMap<String, ArrayDeque<Entity>> entities;
    @Shadow @Final public LinkedHashSet<String> phonismId;
    @Shadow @Final public LinkedHashSet<String> photismId;

    @Inject(method = "tick", at = @At("HEAD"))
    private void clearPTSDData(ServerPlayerEntity player, CallbackInfo ci) {
        Set<String> blacklist = DTTConfig.getInstance().getServerConfig().common_config.universal_ptsd_black_list;
        MiscUtil.cleanMap(this.PTSD, blacklist::contains);
        MiscUtil.cleanMap(this.contactValue, blacklist::contains);
        MiscUtil.cleanMap(this.remainingValue, blacklist::contains);
        MiscUtil.cleanMap(this.entities, blacklist::contains);
        MiscUtil.cleanSet(this.phonismId, blacklist::contains);
        MiscUtil.cleanSet(this.photismId, blacklist::contains);
    }

    @Inject(method = "trigger(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    private void blockStringPTSDTrigger(String id, CallbackInfo ci) {
        if (id.equals("player") || id.equals("minecraft:player")) {
            return;
        }
        if (DTTConfig.getInstance().getServerConfig().common_config.universal_ptsd_black_list.contains(id)) {
            DTTMod.LOGGER.info("Blocked PTSD trigger from damage type or sound event mapping: {}", id);
            ci.cancel();
        }
    }

    @Inject(method = "trigger(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void blockEntityPTSDTrigger(Entity entity, CallbackInfo ci) {
        String id = entity.getSavedEntityId();
        if (id == null) {
            return;
        }
        if (id.equals("player") || id.equals("minecraft:player")) {
            return;
        }
        if (DTTConfig.getInstance().getServerConfig().common_config.universal_ptsd_black_list.contains(id)) {
            DTTMod.LOGGER.info("Blocked PTSD trigger from entity: {}", id);
            ci.cancel();
        }
    }
}
