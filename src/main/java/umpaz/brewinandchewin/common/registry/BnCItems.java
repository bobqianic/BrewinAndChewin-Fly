package umpaz.brewinandchewin.common.registry;

import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.item.BnCBucketItem;
import umpaz.brewinandchewin.common.item.BoozeItem;
import umpaz.brewinandchewin.common.item.DreadNogItem;
import umpaz.brewinandchewin.common.item.JamJarItem;
import umpaz.brewinandchewin.common.item.KegItem;
import umpaz.brewinandchewin.common.item.LargeKegItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;

import java.util.LinkedHashSet;

public class BnCItems {
    public static LinkedHashSet<Item> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, BrewinAndChewin.asResource(name));
    }

    private static Item.Properties itemProperties(String name) {
        return new Item.Properties().setId(key(name));
    }

    private static Item bucketProperties(String name, Fluid fluid) {
        return new BnCBucketItem(fluid, itemProperties(name).stacksTo(1).craftRemainder(Items.BUCKET));
    }

    private static Item bucketProperties(String name, Fluid fluid, FoodProperties food, Consumable consumable) {
        return new BnCBucketItem(fluid, itemProperties(name).stacksTo(1).craftRemainder(Items.BUCKET).food(food, consumable));
    }

    public static void registerWithTab(String name, Item item) {
        registerWithTab(name, item, null);
    }

    public static void registerWithTab(String name, Item item, @Nullable String requiredMod) {
        Registry.register(BuiltInRegistries.ITEM, BrewinAndChewin.asResource(name), item);
        if (requiredMod == null || BrewinAndChewin.getHelper().isModLoaded(requiredMod))
            CREATIVE_TAB_ITEMS.add(item);
    }
    
    public static final Item KEG = new KegItem(BnCBlocks.KEG, itemProperties("keg").stacksTo(1));
    public static final Item LARGE_KEG = new LargeKegItem(BnCBlocks.LARGE_KEG, itemProperties("large_keg").stacksTo(1));
    public static final Item HEATING_CASK = new BlockItem(BnCBlocks.HEATING_CASK, itemProperties("heating_cask"));
    public static final Item ICE_CRATE = new BlockItem(BnCBlocks.ICE_CRATE, itemProperties("ice_crate"));
    public static final Item COASTER = new BlockItem(BnCBlocks.COASTER, itemProperties("coaster"));

    public static final Item TANKARD = new Item(itemProperties("tankard"));

    public static final Item BEER = new BoozeItem(() -> BnCFluids.BEER, itemProperties("beer")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.BEER, BnCFoods.BEER_CONSUMABLE));
    public static final Item VODKA = new BoozeItem(() -> BnCFluids.VODKA, itemProperties("vodka")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.VODKA, BnCFoods.VODKA_CONSUMABLE));
    public static final Item MEAD = new BoozeItem(() -> BnCFluids.MEAD, itemProperties("mead")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.MEAD, BnCFoods.MEAD_CONSUMABLE));
    public static final Item RICE_WINE = new BoozeItem(() -> BnCFluids.RICE_WINE, itemProperties("rice_wine")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.RICE_WINE, BnCFoods.RICE_WINE_CONSUMABLE));
    public static final Item PALE_JANE = new BoozeItem(() -> BnCFluids.PALE_JANE, itemProperties("pale_jane")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.PALE_JANE, BnCFoods.PALE_JANE_CONSUMABLE));
    public static final Item EGG_GROG = new BoozeItem(() -> BnCFluids.EGG_GROG, itemProperties("egg_grog")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.EGG_GROG, BnCFoods.EGG_GROG_CONSUMABLE));
    public static final Item GLITTERING_GRENADINE = new BoozeItem(() -> BnCFluids.GLITTERING_GRENADINE, itemProperties("glittering_grenadine")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.GLITTERING_GRENADINE, BnCFoods.GLITTERING_GRENADINE_CONSUMABLE));
    public static final Item SACCHARINE_RUM = new BoozeItem(() -> BnCFluids.SACCHARINE_RUM, itemProperties("saccharine_rum")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.SACCHARINE_RUM, BnCFoods.SACCHARINE_RUM_CONSUMABLE));
    public static final Item SALTY_FOLLY = new BoozeItem(() -> BnCFluids.SALTY_FOLLY, itemProperties("salty_folly")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.SALTY_FOLLY, BnCFoods.SALTY_FOLLY_CONSUMABLE));
    public static final Item BLOODY_MARY = new BoozeItem(() -> BnCFluids.BLOODY_MARY,  itemProperties("bloody_mary")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.BLOODY_MARY, BnCFoods.BLOODY_MARY_CONSUMABLE));
    public static final Item RED_RUM = new BoozeItem(() -> BnCFluids.RED_RUM, itemProperties("red_rum")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.RED_RUM, BnCFoods.RED_RUM_CONSUMABLE));
    public static final Item STRONGROOT_ALE = new BoozeItem(() -> BnCFluids.STRONGROOT_ALE, itemProperties("strongroot_ale")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.STRONGROOT_ALE, BnCFoods.STRONGROOT_ALE_CONSUMABLE));
    public static final Item STEEL_TOE_STOUT = new BoozeItem(() -> BnCFluids.STEEL_TOE_STOUT, itemProperties("steel_toe_stout")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.STEEL_TOE_STOUT, BnCFoods.STEEL_TOE_STOUT_CONSUMABLE));
    public static final Item DREAD_NOG = new DreadNogItem(() -> BnCFluids.DREAD_NOG, itemProperties("dread_nog")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.DREAD_NOG, BnCFoods.DREAD_NOG_CONSUMABLE));
    public static final Item WITHERING_DROSS = new BoozeItem(() -> BnCFluids.WITHERING_DROSS, itemProperties("withering_dross")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.WITHERING_DROSS, BnCFoods.WITHERING_DROSS_CONSUMABLE));

    public static final Item KOMBUCHA = new BoozeItem(() -> BnCFluids.KOMBUCHA, itemProperties("kombucha")
            .stacksTo(16).craftRemainder(BnCItems.TANKARD).food(BnCFoods.KOMBUCHA, BnCFoods.KOMBUCHA_CONSUMABLE));

    public static Item HONEY_BUCKET;
    public static Item BEER_BUCKET;
    public static Item VODKA_BUCKET;
    public static Item MEAD_BUCKET;
    public static Item RICE_WINE_BUCKET;
    public static Item PALE_JANE_BUCKET;
    public static Item EGG_GROG_BUCKET;
    public static Item GLITTERING_GRENADINE_BUCKET;
    public static Item SACCHARINE_RUM_BUCKET;
    public static Item SALTY_FOLLY_BUCKET;
    public static Item BLOODY_MARY_BUCKET;
    public static Item RED_RUM_BUCKET;
    public static Item STRONGROOT_ALE_BUCKET;
    public static Item STEEL_TOE_STOUT_BUCKET;
    public static Item DREAD_NOG_BUCKET;
    public static Item WITHERING_DROSS_BUCKET;
    public static Item KOMBUCHA_BUCKET;
    public static Item FLAXEN_CHEESE_BUCKET;
    public static Item SCARLET_CHEESE_BUCKET;

    public static final Item UNRIPE_FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL, itemProperties("unripe_flaxen_cheese_wheel").stacksTo(16));
    public static final Item FLAXEN_CHEESE_WHEEL = new BlockItem(BnCBlocks.FLAXEN_CHEESE_WHEEL, itemProperties("flaxen_cheese_wheel").stacksTo(16));
    public static final Item FLAXEN_CHEESE_WEDGE = new Item(itemProperties("flaxen_cheese_wedge").food(BnCFoods.FLAXEN_CHEESE));

    public static final Item UNRIPE_SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL, itemProperties("unripe_scarlet_cheese_wheel").stacksTo(16));
    public static final Item SCARLET_CHEESE_WHEEL = new BlockItem(BnCBlocks.SCARLET_CHEESE_WHEEL, itemProperties("scarlet_cheese_wheel").stacksTo(16));
    public static final Item SCARLET_CHEESE_WEDGE = new Item(itemProperties("scarlet_cheese_wedge").food(BnCFoods.SCARLET_CHEESE));

    public static final Item VEGETABLE_OMELET = new ConsumableItem(itemProperties("vegetable_omelet").stacksTo(16).food(BnCFoods.VEGETABLE_OMELET, BnCFoods.VEGETABLE_OMELET_CONSUMABLE).craftRemainder(Items.BOWL), true);
    public static final Item CREAMY_ONION_SOUP = new ConsumableItem(itemProperties("creamy_onion_soup").stacksTo(16).food(BnCFoods.CREAMY_ONION_SOUP, BnCFoods.CREAMY_ONION_SOUP_CONSUMABLE).craftRemainder(Items.BOWL), true);
    public static final Item CHEESY_PASTA = new ConsumableItem(itemProperties("cheesy_pasta").stacksTo(16).food(BnCFoods.CHEESY_PASTA, BnCFoods.CHEESY_PASTA_CONSUMABLE).craftRemainder(Items.BOWL), true);
    public static final Item HORROR_LASAGNA = new ConsumableItem(itemProperties("horror_lasagna").stacksTo(16).food(BnCFoods.HORROR_LASAGNA, BnCFoods.HORROR_LASAGNA_CONSUMABLE).craftRemainder(Items.BOWL), true);
    public static final Item SCARLET_PIEROGI = new ConsumableItem(itemProperties("scarlet_pierogi").stacksTo(16).food(BnCFoods.SCARLET_PIEROGI, BnCFoods.SCARLET_PIEROGI_CONSUMABLE).craftRemainder(Items.BOWL), true);

    public static final Item FIERY_FONDUE_POT = new BlockItem(BnCBlocks.FIERY_FONDUE_POT, itemProperties("fiery_fondue_pot").stacksTo(1));
    public static final Item FIERY_FONDUE = new ConsumableItem(itemProperties("fiery_fondue").stacksTo(16).food(BnCFoods.FIERY_FONDUE, BnCFoods.FIERY_FONDUE_CONSUMABLE).craftRemainder(Items.BOWL), true);

    public static final Item PIZZA = new BlockItem(BnCBlocks.PIZZA, itemProperties("pizza").stacksTo(1));
    public static final Item QUICHE = new BlockItem(BnCBlocks.QUICHE, itemProperties("quiche"));

    public static final Item PIZZA_SLICE = new Item(itemProperties("pizza_slice").food(BnCFoods.PIZZA_SLICE));
    public static final Item QUICHE_SLICE = new Item(itemProperties("quiche_slice").food(BnCFoods.QUICHE_SLICE, BnCFoods.FAST_FOOD));

    public static final Item HAM_AND_CHEESE_SANDWICH = new Item(itemProperties("ham_and_cheese_sandwich").food(BnCFoods.HAM_AND_CHEESE_SANDWICH));

    public static final Item KIMCHI = new ConsumableItem(itemProperties("kimchi").food(BnCFoods.KIMCHI));
    public static final Item JERKY = new ConsumableItem(itemProperties("jerky").food(BnCFoods.JERKY, BnCFoods.FAST_FOOD));
    public static final Item PICKLED_PICKLES = new ConsumableItem(itemProperties("pickled_pickles").food(BnCFoods.PICKLED_PICKLES));
    public static final Item KIPPERS = new ConsumableItem(itemProperties("kippers").food(BnCFoods.KIPPERS));
    public static final Item COCOA_FUDGE = new ConsumableItem(itemProperties("cocoa_fudge").food(BnCFoods.COCOA_FUDGE, BnCFoods.COCOA_FUDGE_CONSUMABLE));

    public static final Item SWEET_BERRY_JAM = new JamJarItem(itemProperties("sweet_berry_jam").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.SWEET_BERRY_JAM));
    public static final Item GLOW_BERRY_MARMALADE = new JamJarItem(itemProperties("glow_berry_marmalade").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.GLOW_BERRY_MARMALADE));
    public static final Item APPLE_JELLY = new JamJarItem(itemProperties("apple_jelly").stacksTo(16).craftRemainder(Items.GLASS_BOTTLE).food(BnCFoods.APPLE_JELLY));

    private static void initBucketItems() {
        HONEY_BUCKET = BrewinAndChewin.getHelper().isModLoaded("create") ? Items.AIR : bucketProperties("honey_bucket", BnCFluids.HONEY);
        BEER_BUCKET = bucketProperties("beer_bucket", BnCFluids.BEER, BnCFoods.bucketFood(BnCFoods.BEER), BnCFoods.BEER_BUCKET_CONSUMABLE);
        VODKA_BUCKET = bucketProperties("vodka_bucket", BnCFluids.VODKA, BnCFoods.bucketFood(BnCFoods.VODKA), BnCFoods.VODKA_BUCKET_CONSUMABLE);
        MEAD_BUCKET = bucketProperties("mead_bucket", BnCFluids.MEAD, BnCFoods.bucketFood(BnCFoods.MEAD), BnCFoods.MEAD_BUCKET_CONSUMABLE);
        RICE_WINE_BUCKET = bucketProperties("rice_wine_bucket", BnCFluids.RICE_WINE, BnCFoods.bucketFood(BnCFoods.RICE_WINE), BnCFoods.RICE_WINE_BUCKET_CONSUMABLE);
        PALE_JANE_BUCKET = bucketProperties("pale_jane_bucket", BnCFluids.PALE_JANE, BnCFoods.bucketFood(BnCFoods.PALE_JANE), BnCFoods.PALE_JANE_BUCKET_CONSUMABLE);
        EGG_GROG_BUCKET = bucketProperties("egg_grog_bucket", BnCFluids.EGG_GROG, BnCFoods.bucketFood(BnCFoods.EGG_GROG), BnCFoods.EGG_GROG_BUCKET_CONSUMABLE);
        GLITTERING_GRENADINE_BUCKET = bucketProperties("glittering_grenadine_bucket", BnCFluids.GLITTERING_GRENADINE, BnCFoods.bucketFood(BnCFoods.GLITTERING_GRENADINE), BnCFoods.GLITTERING_GRENADINE_BUCKET_CONSUMABLE);
        SACCHARINE_RUM_BUCKET = bucketProperties("saccharine_rum_bucket", BnCFluids.SACCHARINE_RUM, BnCFoods.bucketFood(BnCFoods.SACCHARINE_RUM), BnCFoods.SACCHARINE_RUM_BUCKET_CONSUMABLE);
        SALTY_FOLLY_BUCKET = bucketProperties("salty_folly_bucket", BnCFluids.SALTY_FOLLY, BnCFoods.bucketFood(BnCFoods.SALTY_FOLLY), BnCFoods.SALTY_FOLLY_BUCKET_CONSUMABLE);
        BLOODY_MARY_BUCKET = bucketProperties("bloody_mary_bucket", BnCFluids.BLOODY_MARY, BnCFoods.bucketFood(BnCFoods.BLOODY_MARY), BnCFoods.BLOODY_MARY_BUCKET_CONSUMABLE);
        RED_RUM_BUCKET = bucketProperties("red_rum_bucket", BnCFluids.RED_RUM, BnCFoods.bucketFood(BnCFoods.RED_RUM), BnCFoods.RED_RUM_BUCKET_CONSUMABLE);
        STRONGROOT_ALE_BUCKET = bucketProperties("strongroot_ale_bucket", BnCFluids.STRONGROOT_ALE, BnCFoods.bucketFood(BnCFoods.STRONGROOT_ALE), BnCFoods.STRONGROOT_ALE_BUCKET_CONSUMABLE);
        STEEL_TOE_STOUT_BUCKET = bucketProperties("steel_toe_stout_bucket", BnCFluids.STEEL_TOE_STOUT, BnCFoods.bucketFood(BnCFoods.STEEL_TOE_STOUT), BnCFoods.STEEL_TOE_STOUT_BUCKET_CONSUMABLE);
        DREAD_NOG_BUCKET = bucketProperties("dread_nog_bucket", BnCFluids.DREAD_NOG, BnCFoods.bucketFood(BnCFoods.DREAD_NOG), BnCFoods.DREAD_NOG_BUCKET_CONSUMABLE);
        WITHERING_DROSS_BUCKET = bucketProperties("withering_dross_bucket", BnCFluids.WITHERING_DROSS, BnCFoods.bucketFood(BnCFoods.WITHERING_DROSS), BnCFoods.WITHERING_DROSS_BUCKET_CONSUMABLE);
        KOMBUCHA_BUCKET = bucketProperties("kombucha_bucket", BnCFluids.KOMBUCHA, BnCFoods.bucketFood(BnCFoods.KOMBUCHA), BnCFoods.KOMBUCHA_BUCKET_CONSUMABLE);
        FLAXEN_CHEESE_BUCKET = bucketProperties("flaxen_cheese_bucket", BnCFluids.FLAXEN_CHEESE);
        SCARLET_CHEESE_BUCKET = bucketProperties("scarlet_cheese_bucket", BnCFluids.SCARLET_CHEESE);
    }

    public static void registerAll() {
        initBucketItems();

        registerWithTab("keg", KEG);
        registerWithTab("large_keg", LARGE_KEG);
        registerWithTab("heating_cask", HEATING_CASK);
        registerWithTab("ice_crate", ICE_CRATE);
        registerWithTab("coaster", COASTER);

        registerWithTab("tankard", TANKARD);

        registerWithTab("beer", BEER);
        registerWithTab("vodka", VODKA);
        registerWithTab("mead", MEAD);
        registerWithTab("rice_wine", RICE_WINE);
        registerWithTab("pale_jane", PALE_JANE);
        registerWithTab("egg_grog", EGG_GROG);
        registerWithTab("glittering_grenadine", GLITTERING_GRENADINE);
        registerWithTab("saccharine_rum", SACCHARINE_RUM);
        registerWithTab("salty_folly", SALTY_FOLLY);
        registerWithTab("bloody_mary", BLOODY_MARY);
        registerWithTab("red_rum", RED_RUM);
        registerWithTab("strongroot_ale", STRONGROOT_ALE);
        registerWithTab("steel_toe_stout", STEEL_TOE_STOUT);
        registerWithTab("dread_nog", DREAD_NOG);
        registerWithTab("withering_dross", WITHERING_DROSS);

        registerWithTab("kombucha", KOMBUCHA, "farmersrespite");

        if (!BrewinAndChewin.getHelper().isModLoaded("create")) {
            registerWithTab("honey_bucket", HONEY_BUCKET);
        }
        registerWithTab("beer_bucket", BEER_BUCKET);
        registerWithTab("vodka_bucket", VODKA_BUCKET);
        registerWithTab("mead_bucket", MEAD_BUCKET);
        registerWithTab("rice_wine_bucket", RICE_WINE_BUCKET);
        registerWithTab("pale_jane_bucket", PALE_JANE_BUCKET);
        registerWithTab("egg_grog_bucket", EGG_GROG_BUCKET);
        registerWithTab("glittering_grenadine_bucket", GLITTERING_GRENADINE_BUCKET);
        registerWithTab("saccharine_rum_bucket", SACCHARINE_RUM_BUCKET);
        registerWithTab("salty_folly_bucket", SALTY_FOLLY_BUCKET);
        registerWithTab("bloody_mary_bucket", BLOODY_MARY_BUCKET);
        registerWithTab("red_rum_bucket", RED_RUM_BUCKET);
        registerWithTab("strongroot_ale_bucket", STRONGROOT_ALE_BUCKET);
        registerWithTab("steel_toe_stout_bucket", STEEL_TOE_STOUT_BUCKET);
        registerWithTab("dread_nog_bucket", DREAD_NOG_BUCKET);
        registerWithTab("withering_dross_bucket", WITHERING_DROSS_BUCKET);
        registerWithTab("kombucha_bucket", KOMBUCHA_BUCKET, "farmersrespite");
        registerWithTab("flaxen_cheese_bucket", FLAXEN_CHEESE_BUCKET);
        registerWithTab("scarlet_cheese_bucket", SCARLET_CHEESE_BUCKET);

        registerWithTab("unripe_flaxen_cheese_wheel", UNRIPE_FLAXEN_CHEESE_WHEEL);
        registerWithTab("flaxen_cheese_wheel", FLAXEN_CHEESE_WHEEL);
        registerWithTab("flaxen_cheese_wedge", FLAXEN_CHEESE_WEDGE);

        registerWithTab("unripe_scarlet_cheese_wheel", UNRIPE_SCARLET_CHEESE_WHEEL);
        registerWithTab("scarlet_cheese_wheel", SCARLET_CHEESE_WHEEL);
        registerWithTab("scarlet_cheese_wedge", SCARLET_CHEESE_WEDGE);
        
        registerWithTab("vegetable_omelet", VEGETABLE_OMELET);
        registerWithTab("creamy_onion_soup", CREAMY_ONION_SOUP);
        registerWithTab("cheesy_pasta", CHEESY_PASTA);
        registerWithTab("horror_lasagna", HORROR_LASAGNA);
        registerWithTab("scarlet_pierogi", SCARLET_PIEROGI);

        registerWithTab("fiery_fondue_pot", FIERY_FONDUE_POT);
        registerWithTab("fiery_fondue", FIERY_FONDUE);

        registerWithTab("pizza", PIZZA);
        registerWithTab("quiche", QUICHE);

        registerWithTab("pizza_slice", PIZZA_SLICE);
        registerWithTab("quiche_slice", QUICHE_SLICE);

        registerWithTab("ham_and_cheese_sandwich", HAM_AND_CHEESE_SANDWICH);

        registerWithTab("kimchi", KIMCHI);
        registerWithTab("jerky", JERKY);
        registerWithTab("pickled_pickles", PICKLED_PICKLES);
        registerWithTab("kippers", KIPPERS);
        registerWithTab("cocoa_fudge", COCOA_FUDGE);

        registerWithTab("sweet_berry_jam", SWEET_BERRY_JAM);
        registerWithTab("glow_berry_marmalade", GLOW_BERRY_MARMALADE);
        registerWithTab("apple_jelly", APPLE_JELLY);
    }
}
