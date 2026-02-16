package grainalcohol.dtt.mixin.modification;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import grainalcohol.dtt.DTTMod;
import grainalcohol.dtt.api.internal.BlockBreakMentalHealCooldownController;
import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import grainalcohol.dtt.mock.MockMentalStatus;
import grainalcohol.dtt.util.MiscUtil;
import net.depression.mental.MentalStatus;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Mixin(MentalStatus.class)
public abstract class MentalStatusMixin implements BlockBreakMentalHealCooldownController {
    @Unique private int dtt$blockBreakHealCooldownTicks = 0;
    @Unique private double dtt$maxNearbyHealAmount = 0;

    @Shadow public abstract void mentalHurt(double value);
    @Shadow @Final public ConcurrentHashMap<String, Double> PTSD;
    @Shadow @Final private ConcurrentHashMap<String, Long> PTSDTimeBuffer;
    @Shadow @Final private ConcurrentHashMap<String, Double> PTSDValueBuffer;
    @Shadow @Final public HashSet<String> playerPTSDSet;
    @Shadow public long tickCount;
    @Shadow private ServerPlayerEntity player;
    @Shadow public abstract double mentalHeal(double value);

    @Unique
    @Override
    public int dtt$getCooldownTicks() {
        return this.dtt$blockBreakHealCooldownTicks;
    }

    @Unique
    @Override
    public void dtt$setCooldownTicks(int ticks) {
        this.dtt$blockBreakHealCooldownTicks = ticks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        if (this.dtt$blockBreakHealCooldownTicks > 0) {
            this.dtt$blockBreakHealCooldownTicks--;
        }
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/ExecutorService;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;"
            )
    )
    private Future<?> wrapDetectNearbyHealBlockCalled(ExecutorService executor, Runnable runnable, Operation<Future<?>> original) {
        if (this.tickCount % DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearby_block_interval_ticks == 0) {
            return original.call(executor, runnable);
        }
        return CompletableFuture.completedFuture(null);
    }

    @WrapOperation(
            method = "detectNearbyHealBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/mental/MentalStatus;mentalHeal(Ljava/lang/String;D)D"
            )
    )
    private double redirectDetectNearbyHealBlockMentalHeal(MentalStatus mentalStatus, String id, double value, Operation<Double> original) {
        ServerConfig.NearbyMultipleBlocksHealMode nearbyMultipleBlocksHealMode = DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearby_block_mode;
        if (nearbyMultipleBlocksHealMode.equals(ServerConfig.NearbyMultipleBlocksHealMode.EVERYONE)) {
            // everyone模式：应用所有方块
            return original.call(mentalStatus, id, value);
        }

        if (value > dtt$maxNearbyHealAmount) {
            // max_only模式：仅应用回复量最大的那一个
            dtt$maxNearbyHealAmount = value;
        }
        MockMentalStatus mockMentalStatus = MockMentalStatus.copyOf(mentalStatus);
        return mockMentalStatus.mockMentalHeal(id, value);
    }

    @Inject(
            method = "detectNearbyHealBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/network/ActionbarHintPacket;sendNearbyBlockHealPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/text/Text;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onDetectNearbyHealBlockSendPacket(CallbackInfo ci) {
        ServerConfig.NearbyMultipleBlocksHealMode nearbyMultipleBlocksHealMode = DTTConfig.getInstance().getServerConfig().mentalHealConfig.nearby_block_mode;
        if (nearbyMultipleBlocksHealMode.equals(ServerConfig.NearbyMultipleBlocksHealMode.MAX_ONLY)) {
            this.mentalHeal(dtt$maxNearbyHealAmount);
        }
        dtt$maxNearbyHealAmount = 0;
        // nothing模式：什么都不做
    }

    @Inject(method = "viewDetect", at = @At("HEAD"), cancellable = true)
    private void onViewDetectHead(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        double distance = this.player.getPos().distanceTo(entity.getPos());
        if (distance > DTTConfig.getInstance().getServerConfig().commonConfig.max_distance_to_trigger_ptsd_by_sight) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(
            method = "mentalHeal(Ljava/lang/String;D)D",
            constant = @Constant(doubleValue = 2.0)
    )
    private double modifyPTSDHealThreshold(double original) {
        double boredomStrength = DTTConfig.getInstance().getServerConfig().commonConfig.boredom_strength;
        return 1 / boredomStrength; // 原数据为2.0，即(1 / 0.5)
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void clearPTSDData(CallbackInfo ci) {
        Set<String> blacklist = DTTConfig.getInstance().getServerConfig().commonConfig.universal_ptsd_black_list;
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
        if (DTTConfig.getInstance().getServerConfig().commonConfig.universal_ptsd_black_list.contains(id)) {
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
        if (DTTConfig.getInstance().getServerConfig().commonConfig.universal_ptsd_black_list.contains(id)) {
            DTTMod.LOGGER.info("Blocked trigger ptsd from hurt by damage type source but string: {}", id);
            ci.cancel();
        }
    }
}
