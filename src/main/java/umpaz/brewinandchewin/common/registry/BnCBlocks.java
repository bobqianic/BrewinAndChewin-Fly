package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.*;
import vectorwing.farmersdelight.common.block.PieBlock;

public class BnCBlocks {
    private static ResourceKey<Block> key(String name) {
        return ResourceKey.create(Registries.BLOCK, BrewinAndChewin.asResource(name));
    }

    private static BlockBehaviour.Properties blockProperties(String name, BlockBehaviour block) {
        return BlockBehaviour.Properties.ofFullCopy(block).setId(key(name));
    }

    public static final Block KEG = new KegBlock(
            blockProperties("keg", Blocks.OAK_PLANKS));

    public static final Block LARGE_KEG = new LargeKegBlock(
            blockProperties("large_keg", Blocks.OAK_PLANKS)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false));

    public static final Block LARGE_KEG_FOOTPRINT = new LargeKegFootprintBlock(
            blockProperties("large_keg_footprint", Blocks.OAK_PLANKS)
                    .noLootTable()
                    .noOcclusion()
                    .dynamicShape()
                    .overrideDescription("block.brewinandchewin.large_keg")
                    .isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos) -> false));

    public static final Block HEATING_CASK = new HeatingCaskBlock(
            blockProperties("heating_cask", Blocks.OAK_PLANKS));

    public static final Block ICE_CRATE = new IceCrateBlock(
            blockProperties("ice_crate", Blocks.OAK_PLANKS));

    public static final Block TEMPORARY_WATER = new TemporaryWaterBlock(
            blockProperties("temporary_water", Blocks.WATER).noCollision());

    public static final Block COASTER = new CoasterBlock(blockProperties("coaster", Blocks.BROWN_CARPET).sound(SoundType.WOOD).instabreak().dynamicShape());

    //Cheese
    public static final Block UNRIPE_FLAXEN_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.FLAXEN_CHEESE_WHEEL, blockProperties("unripe_flaxen_cheese_wheel", Blocks.CAKE));

    public static final Block FLAXEN_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.FLAXEN_CHEESE_WEDGE, blockProperties("flaxen_cheese_wheel", Blocks.CAKE));

    public static final Block UNRIPE_SCARLET_CHEESE_WHEEL = new
            UnripeCheeseWheelBlock(() -> BnCBlocks.SCARLET_CHEESE_WHEEL, blockProperties("unripe_scarlet_cheese_wheel", Blocks.CAKE));

    public static final Block SCARLET_CHEESE_WHEEL = new
            CheeseWheelBlock(() -> BnCItems.SCARLET_CHEESE_WEDGE, blockProperties("scarlet_cheese_wheel", Blocks.CAKE));

    // Feasts
    public static final Block FIERY_FONDUE_POT = new
            FieryFonduePotBlock(blockProperties("fiery_fondue_pot", Blocks.CAULDRON));

    public static final Block PIZZA = new
            PizzaBlock(blockProperties("pizza", Blocks.CAKE));

    public static final Block QUICHE = new
            PieBlock(blockProperties("quiche", Blocks.CAKE), () -> BnCItems.QUICHE_SLICE);


    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("large_keg"), LARGE_KEG);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("large_keg_footprint"), LARGE_KEG_FOOTPRINT);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("heating_cask"), HEATING_CASK);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("ice_crate"), ICE_CRATE);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("temporary_water"), TEMPORARY_WATER);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("coaster"), COASTER);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_flaxen_cheese_wheel"), UNRIPE_FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("flaxen_cheese_wheel"), FLAXEN_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("unripe_scarlet_cheese_wheel"), UNRIPE_SCARLET_CHEESE_WHEEL);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("scarlet_cheese_wheel"), SCARLET_CHEESE_WHEEL);

        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("fiery_fondue_pot"), FIERY_FONDUE_POT);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("pizza"), PIZZA);
        Registry.register(BuiltInRegistries.BLOCK, BrewinAndChewin.asResource("quiche"), QUICHE);
    }
}
