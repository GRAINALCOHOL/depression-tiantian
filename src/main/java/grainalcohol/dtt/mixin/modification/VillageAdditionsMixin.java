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
        ServerConfig.VillagerConfig villagerConfig = DTTConfig.getInstance().getServerConfig().villagerConfig;

        float priceMultiplier = villagerConfig.enablePriceFloating ? 1.0F : 0.0F;

        // mental health scale
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfMentalHealthScale,
                villagerConfig.basePriceOfMentalHealthScale,
                new ItemStack(ModItems.MENTAL_HEALTH_SCALE.get(), 1),
                villagerConfig.maxUsesOfMentalHealthScale,
                5, priceMultiplier
        );
        // mild depression tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfMildDepressionTablet,
                villagerConfig.basePriceOfMildDepressionTablet,
                new ItemStack(ModItems.MILD_DEPRESSION_TABLET.get(), 6),
                villagerConfig.maxUsesOfMildDepressionTablet,
                5, priceMultiplier
        );
        // insomnia tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfInsomniaTablet,
                villagerConfig.basePriceOfInsomniaTablet,
                new ItemStack(ModItems.INSOMNIA_TABLET.get(), 6),
                villagerConfig.maxUsesOfInsomniaTablet,
                20, priceMultiplier
        );
        // moderate depression tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfModerateDepressionTablet,
                villagerConfig.basePriceOfModerateDepressionTablet,
                new ItemStack(ModItems.MODERATE_DEPRESSION_TABLET.get(), 6),
                villagerConfig.maxUsesOfModerateDepressionTablet,
                20, priceMultiplier
        );
        // MDD capsule
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfMDDCapsule,
                villagerConfig.basePriceOfMDDCapsule,
                new ItemStack(ModItems.MDD_CAPSULE.get(), 6),
                villagerConfig.maxUsesOfMDDCapsule,
                40, priceMultiplier
        );
        // mania tablet
        registerPsychologistTradeOffers(
                villagerConfig.lowestLevelOfManiaTablet,
                villagerConfig.basePriceOfManiaTablet,
                new ItemStack(ModItems.MANIA_TABLET.get(), 6),
                villagerConfig.maxUsesOfManiaTablet,
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
