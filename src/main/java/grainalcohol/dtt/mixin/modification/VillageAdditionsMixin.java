package grainalcohol.dtt.mixin.modification;

import grainalcohol.dtt.config.DTTConfig;
import grainalcohol.dtt.config.ServerConfig;
import net.depression.fabric.world.FabricVillageAdditions;
import net.depression.item.ModItems;
import net.depression.world.VillageAdditions;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FabricVillageAdditions.class)
public class VillageAdditionsMixin {
    @Inject(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/depression/fabric/world/FabricVillageAdditions;registerPoi(Lnet/minecraft/registry/RegistryKey;Ldev/architectury/registry/registries/RegistrySupplier;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private static void afterRegisterPoi(CallbackInfo ci) {
        ServerConfig.VillagerConfig villagerConfig = DTTConfig.getInstance().getServerConfig().villager_config;

        float priceMultiplier = villagerConfig.enable_price_floating ? 1.0F : 0.0F;

        // mental health scale
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_mental_health_scale,
                villagerConfig.base_price_of_mental_health_scale,
                new ItemStack(ModItems.MENTAL_HEALTH_SCALE.get(), 1),
                villagerConfig.max_uses_of_mental_health_scale,
                5, priceMultiplier
        );
        // mild depression tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_mild_depression_tablet,
                villagerConfig.base_price_of_mild_depression_tablet,
                new ItemStack(ModItems.MILD_DEPRESSION_TABLET.get(), 6),
                villagerConfig.max_uses_of_mild_depression_tablet,
                5, priceMultiplier
        );
        // insomnia tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_insomnia_tablet,
                villagerConfig.base_price_of_insomnia_tablet,
                new ItemStack(ModItems.INSOMNIA_TABLET.get(), 6),
                villagerConfig.max_uses_of_insomnia_tablet,
                20, priceMultiplier
        );
        // moderate depression tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_moderate_depression_tablet,
                villagerConfig.base_price_of_moderate_depression_tablet,
                new ItemStack(ModItems.MODERATE_DEPRESSION_TABLET.get(), 6),
                villagerConfig.max_uses_of_moderate_depression_tablet,
                20, priceMultiplier
        );
        // MDD capsule
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_mdd_capsule,
                villagerConfig.base_price_of_mdd_capsule,
                new ItemStack(ModItems.MDD_CAPSULE.get(), 6),
                villagerConfig.max_uses_of_mdd_capsule,
                40, priceMultiplier
        );
        // mania tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowest_level_of_mania_tablet,
                villagerConfig.base_price_of_mania_tablet,
                new ItemStack(ModItems.MANIA_TABLET.get(), 6),
                villagerConfig.max_uses_of_mania_tablet,
                40, priceMultiplier
        );

        ci.cancel();
    }

    @Unique
    private static void registerPsychologistTradeOffers(int lowestLevel, int emeraldCost, ItemStack sellItem, int maxUses, int merchantExperience, float priceMultiplier) {
        TradeOfferHelper.registerVillagerOffers(
                VillageAdditions.PSYCHOLOGIST.get(), lowestLevel,
                factories -> factories.add((entity, randomSource) -> new TradeOffer(
                        new ItemStack(Items.EMERALD, emeraldCost), sellItem,
                        maxUses, merchantExperience, priceMultiplier)
                )
        );

    }
}
