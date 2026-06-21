
package umpaz.brewinandchewin.fabric.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.fabric.fluid.BnCFluidFabric;
import umpaz.brewinandchewin.fabric.utility.BnCCreateDelegate;

public class BnCFluidsImpl {
    private static final ResourceLocation MINECRAFT_MILK = ResourceLocation.withDefaultNamespace("milk");
    private static final ResourceLocation MINECRAFT_FLOWING_MILK = ResourceLocation.withDefaultNamespace("flowing_milk");

    private static FlowingFluid bncMilk;
    private static FlowingFluid bncFlowingMilk;

    public static FlowingFluid MILK;
    public static FlowingFluid FLOWING_MILK;

    public static void init() {
        initMilk();
        if (isBnCMilk())
            registerMilk();

        if (BrewinAndChewin.getHelper().isModLoaded("create")) {
            BnCFluids.HONEY = BnCCreateDelegate.getHoneySource();
            BnCFluids.FLOWING_HONEY = BnCCreateDelegate.getFlowingHoney();
        } else {
            BnCFluids.HONEY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_HONEY, () -> BnCItems.HONEY_BUCKET);
            BnCFluids.FLOWING_HONEY = new BnCFluidFabric.Flowing(() -> BnCFluids.HONEY);
        }
        BnCFluids.BEER = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_BEER, () -> BnCItems.BEER_BUCKET);
        BnCFluids.FLOWING_BEER = new BnCFluidFabric.Flowing(() -> BnCFluids.BEER);
        BnCFluids.VODKA = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_VODKA, () -> BnCItems.VODKA_BUCKET);
        BnCFluids.FLOWING_VODKA = new BnCFluidFabric.Flowing(() -> BnCFluids.VODKA);
        BnCFluids.MEAD = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_MEAD, () -> BnCItems.MEAD_BUCKET);
        BnCFluids.FLOWING_MEAD = new BnCFluidFabric.Flowing(() -> BnCFluids.MEAD);
        BnCFluids.EGG_GROG = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_EGG_GROG, () -> BnCItems.EGG_GROG_BUCKET);
        BnCFluids.FLOWING_EGG_GROG = new BnCFluidFabric.Flowing(() -> BnCFluids.EGG_GROG);
        BnCFluids.STRONGROOT_ALE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_STRONGROOT_ALE, () -> BnCItems.STRONGROOT_ALE_BUCKET);
        BnCFluids.FLOWING_STRONGROOT_ALE = new BnCFluidFabric.Flowing(() -> BnCFluids.STRONGROOT_ALE);
        BnCFluids.RICE_WINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_RICE_WINE, () -> BnCItems.RICE_WINE_BUCKET);
        BnCFluids.FLOWING_RICE_WINE = new BnCFluidFabric.Flowing(() -> BnCFluids.RICE_WINE);
        BnCFluids.GLITTERING_GRENADINE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_GLITTERING_GRENADINE, () -> BnCItems.GLITTERING_GRENADINE_BUCKET);
        BnCFluids.FLOWING_GLITTERING_GRENADINE = new BnCFluidFabric.Flowing(() -> BnCFluids.GLITTERING_GRENADINE);
        BnCFluids.STEEL_TOE_STOUT = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_STEEL_TOE_STOUT, () -> BnCItems.STEEL_TOE_STOUT_BUCKET);
        BnCFluids.FLOWING_STEEL_TOE_STOUT = new BnCFluidFabric.Flowing(() -> BnCFluids.STEEL_TOE_STOUT);
        BnCFluids.DREAD_NOG = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_DREAD_NOG, () -> BnCItems.DREAD_NOG_BUCKET);
        BnCFluids.FLOWING_DREAD_NOG = new BnCFluidFabric.Flowing(() -> BnCFluids.DREAD_NOG);
        BnCFluids.SACCHARINE_RUM = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SACCHARINE_RUM, () -> BnCItems.SACCHARINE_RUM_BUCKET);
        BnCFluids.FLOWING_SACCHARINE_RUM = new BnCFluidFabric.Flowing(() -> BnCFluids.SACCHARINE_RUM);
        BnCFluids.PALE_JANE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_PALE_JANE, () -> BnCItems.PALE_JANE_BUCKET);
        BnCFluids.FLOWING_PALE_JANE = new BnCFluidFabric.Flowing(() -> BnCFluids.PALE_JANE);
        BnCFluids.SALTY_FOLLY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SALTY_FOLLY, () -> BnCItems.SALTY_FOLLY_BUCKET);
        BnCFluids.FLOWING_SALTY_FOLLY = new BnCFluidFabric.Flowing(() -> BnCFluids.SALTY_FOLLY);
        BnCFluids.BLOODY_MARY = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_BLOODY_MARY, () -> BnCItems.BLOODY_MARY_BUCKET);
        BnCFluids.FLOWING_BLOODY_MARY = new BnCFluidFabric.Flowing(() -> BnCFluids.BLOODY_MARY);
        BnCFluids.RED_RUM = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_RED_RUM, () -> BnCItems.RED_RUM_BUCKET);
        BnCFluids.FLOWING_RED_RUM = new BnCFluidFabric.Flowing(() -> BnCFluids.RED_RUM);
        BnCFluids.WITHERING_DROSS = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_WITHERING_DROSS, () -> BnCItems.WITHERING_DROSS_BUCKET);
        BnCFluids.FLOWING_WITHERING_DROSS = new BnCFluidFabric.Flowing(() -> BnCFluids.WITHERING_DROSS);
        BnCFluids.KOMBUCHA = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_KOMBUCHA, () -> BnCItems.KOMBUCHA_BUCKET);
        BnCFluids.FLOWING_KOMBUCHA = new BnCFluidFabric.Flowing(() -> BnCFluids.KOMBUCHA);
        BnCFluids.FLAXEN_CHEESE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_FLAXEN_CHEESE, () -> BnCItems.FLAXEN_CHEESE_BUCKET);
        BnCFluids.FLOWING_FLAXEN_CHEESE = new BnCFluidFabric.Flowing(() -> BnCFluids.FLAXEN_CHEESE);
        BnCFluids.SCARLET_CHEESE = new BnCFluidFabric.Source(() -> BnCFluids.FLOWING_SCARLET_CHEESE, () -> BnCItems.SCARLET_CHEESE_BUCKET);
        BnCFluids.FLOWING_SCARLET_CHEESE = new BnCFluidFabric.Flowing(() -> BnCFluids.SCARLET_CHEESE);
    }

    public static boolean isBnCMilk() {
        return MILK == bncMilk;
    }

    private static void initMilk() {
        FlowingFluid vanillaMilk = findRegisteredFlowingFluid(MINECRAFT_MILK);
        if (vanillaMilk != null) {
            MILK = vanillaMilk;
            FLOWING_MILK = findRegisteredFlowingFluid(MINECRAFT_FLOWING_MILK);
            if (FLOWING_MILK == null)
                FLOWING_MILK = vanillaMilk;
            return;
        }

        if (BrewinAndChewin.getHelper().isModLoaded("create")) {
            MILK = BnCCreateDelegate.getMilkSource();
            FLOWING_MILK = BnCCreateDelegate.getFlowingMilk();
            return;
        }

        bncMilk = new BnCFluidFabric.Source(() -> BnCFluidsImpl.FLOWING_MILK);
        bncFlowingMilk = new BnCFluidFabric.Flowing(() -> BnCFluidsImpl.MILK);
        MILK = bncMilk;
        FLOWING_MILK = bncFlowingMilk;
    }

    private static FlowingFluid findRegisteredFlowingFluid(ResourceLocation location) {
        return BuiltInRegistries.FLUID.getOptional(location)
                .filter(FlowingFluid.class::isInstance)
                .map(FlowingFluid.class::cast)
                .orElse(null);
    }

    private static void registerMilk() {
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("milk"), MILK);
        Registry.register(BuiltInRegistries.FLUID, BrewinAndChewin.asResource("flowing_milk"), FLOWING_MILK);
    }
}
