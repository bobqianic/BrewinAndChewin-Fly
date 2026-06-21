package umpaz.brewinandchewin.common.registry;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import vectorwing.farmersdelight.common.FoodValues;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.List;

public class BnCFoods {
    private static final int CUPS_PER_BUCKET = 4;

    private static final FoodAmount WHEAT = food(Foods.BREAD).divide(3.0F);
    private static final FoodAmount POTATO = food(Foods.POTATO);
    private static final FoodAmount HONEY_BOTTLE = food(Foods.HONEY_BOTTLE);
    private static final FoodAmount HONEY_BUCKET = HONEY_BOTTLE.times(CUPS_PER_BUCKET);
    private static final FoodAmount SWEET_BERRIES = food(Foods.SWEET_BERRIES);
    private static final FoodAmount GLOW_BERRIES = food(Foods.GLOW_BERRIES);
    private static final FoodAmount MELON_SLICE = food(Foods.MELON_SLICE);
    private static final FoodAmount DRIED_KELP = food(Foods.DRIED_KELP);
    private static final FoodAmount BEETROOT = food(Foods.BEETROOT);
    private static final FoodAmount CARROT = food(Foods.CARROT);
    private static final FoodAmount CABBAGE = food(FoodValues.CABBAGE);
    private static final FoodAmount TOMATO = food(FoodValues.TOMATO);
    private static final FoodAmount FRIED_EGG = food(FoodValues.FRIED_EGG);
    private static final FoodAmount COOKED_RICE = food(FoodValues.COOKED_RICE);
    private static final FoodAmount JERKY_INGREDIENT = new FoodAmount(3.0F, 4.2F);

    // Keg drink recipes output 1000 mB; one drink item is a 250 mB serving.
    private static final FoodAmount BEER_TOTAL = WHEAT;
    private static final FoodAmount VODKA_TOTAL = POTATO.add(WHEAT);
    private static final FoodAmount MEAD_TOTAL = HONEY_BUCKET.add(WHEAT).add(SWEET_BERRIES);
    private static final FoodAmount RICE_WINE_TOTAL = COOKED_RICE;
    private static final FoodAmount PALE_JANE_TOTAL = RICE_WINE_TOTAL.add(HONEY_BOTTLE);
    private static final FoodAmount EGG_GROG_TOTAL = FRIED_EGG.add(CABBAGE);
    private static final FoodAmount GLITTERING_GRENADINE_TOTAL = GLOW_BERRIES;
    private static final FoodAmount SACCHARINE_RUM_TOTAL = MEAD_TOTAL.add(SWEET_BERRIES).add(MELON_SLICE);
    private static final FoodAmount SALTY_FOLLY_TOTAL = VODKA_TOTAL.add(DRIED_KELP);
    private static final FoodAmount BLOODY_MARY_TOTAL = VODKA_TOTAL.add(TOMATO).add(CABBAGE).add(SWEET_BERRIES);
    private static final FoodAmount RED_RUM_TOTAL = BLOODY_MARY_TOTAL;
    private static final FoodAmount STRONGROOT_ALE_TOTAL = BEER_TOTAL.add(BEETROOT).add(POTATO).add(JERKY_INGREDIENT);
    private static final FoodAmount STEEL_TOE_STOUT_TOTAL = STRONGROOT_ALE_TOTAL.add(WHEAT);
    private static final FoodAmount DREAD_NOG_TOTAL = EGG_GROG_TOTAL;
    private static final FoodAmount WITHERING_DROSS_TOTAL = SALTY_FOLLY_TOTAL;
    private static final FoodAmount KOMBUCHA_TOTAL = VODKA_TOTAL.add(CARROT).add(SWEET_BERRIES);

    public static final FoodProperties BEER = BEER_TOTAL.perCup();
    public static final Consumable BEER_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, true, false)
    );
    public static final Consumable BEER_BUCKET_CONSUMABLE = bucketDrink(BEER_CONSUMABLE);
    public static final FoodProperties VODKA = VODKA_TOTAL.perCup();
    public static final Consumable VODKA_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 3600, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false)
    );
    public static final Consumable VODKA_BUCKET_CONSUMABLE = bucketDrink(VODKA_CONSUMABLE);
    public static final FoodProperties MEAD = MEAD_TOTAL.perCup();
    public static final Consumable MEAD_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(BnCEffects.SWEET_HEART, 2400, 0, false, false)
    );
    public static final Consumable MEAD_BUCKET_CONSUMABLE = bucketDrink(MEAD_CONSUMABLE);
    public static final FoodProperties RICE_WINE = RICE_WINE_TOTAL.perCup();
    public static final Consumable RICE_WINE_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(ModEffects.COMFORT, 1200, 0)
    );
    public static final Consumable RICE_WINE_BUCKET_CONSUMABLE = bucketDrink(RICE_WINE_CONSUMABLE);
    public static final FoodProperties PALE_JANE = PALE_JANE_TOTAL.perCup();
    public static final Consumable PALE_JANE_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 3600, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
            new MobEffectInstance(ModEffects.COMFORT, 2400, 0, false, false)
    );
    public static final Consumable PALE_JANE_BUCKET_CONSUMABLE = bucketDrink(PALE_JANE_CONSUMABLE);
    public static final FoodProperties EGG_GROG = EGG_GROG_TOTAL.perCup();
    public static final Consumable EGG_GROG_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(MobEffects.ABSORPTION, 600, 0)
    );
    public static final Consumable EGG_GROG_BUCKET_CONSUMABLE = bucketDrink(EGG_GROG_CONSUMABLE);
    public static final FoodProperties GLITTERING_GRENADINE = GLITTERING_GRENADINE_TOTAL.perCup();
    public static final Consumable GLITTERING_GRENADINE_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(MobEffects.GLOWING, 600, 0),
            new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0)
    );
    public static final Consumable GLITTERING_GRENADINE_BUCKET_CONSUMABLE = bucketDrink(GLITTERING_GRENADINE_CONSUMABLE);
    public static final FoodProperties SACCHARINE_RUM = SACCHARINE_RUM_TOTAL.perCup();
    public static final Consumable SACCHARINE_RUM_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 3600, 1),
            new MobEffectInstance(BnCEffects.INTOXICATION, 2400, 0, false, false),
            new MobEffectInstance(BnCEffects.SWEET_HEART, 3600, 0, false, false)
    );
    public static final Consumable SACCHARINE_RUM_BUCKET_CONSUMABLE = bucketDrink(SACCHARINE_RUM_CONSUMABLE);
    public static final FoodProperties SALTY_FOLLY = SALTY_FOLLY_TOTAL.perCup();
    public static final Consumable SALTY_FOLLY_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 3600, 1),
            new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
            new MobEffectInstance(MobEffects.WATER_BREATHING, 1800, 0)
    );
    public static final Consumable SALTY_FOLLY_BUCKET_CONSUMABLE = bucketDrink(SALTY_FOLLY_CONSUMABLE);
    public static final FoodProperties BLOODY_MARY = BLOODY_MARY_TOTAL.perCup();
    public static final Consumable BLOODY_MARY_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 1),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(BnCEffects.RAGING, 1200, 0)
    );
    public static final Consumable BLOODY_MARY_BUCKET_CONSUMABLE = bucketDrink(BLOODY_MARY_CONSUMABLE);
    public static final FoodProperties RED_RUM = RED_RUM_TOTAL.perCup();
    public static final Consumable RED_RUM_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 2),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(BnCEffects.RAGING, 2400, 0)
    );
    public static final Consumable RED_RUM_BUCKET_CONSUMABLE = bucketDrink(RED_RUM_CONSUMABLE);
    public static final FoodProperties STRONGROOT_ALE = STRONGROOT_ALE_TOTAL.perCup();
    public static final Consumable STRONGROOT_ALE_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 1),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(MobEffects.RESISTANCE, 600, 0)
    );
    public static final Consumable STRONGROOT_ALE_BUCKET_CONSUMABLE = bucketDrink(STRONGROOT_ALE_CONSUMABLE);
    public static final FoodProperties STEEL_TOE_STOUT = STEEL_TOE_STOUT_TOTAL.perCup();
    public static final Consumable STEEL_TOE_STOUT_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 2),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0)
    );
    public static final Consumable STEEL_TOE_STOUT_BUCKET_CONSUMABLE = bucketDrink(STEEL_TOE_STOUT_CONSUMABLE);
    public static final FoodProperties DREAD_NOG = DREAD_NOG_TOTAL.perCup();
    public static final Consumable DREAD_NOG_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 4800, 2),
            new MobEffectInstance(BnCEffects.INTOXICATION, 4200, 0, false, false),
            new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0)
    );
    public static final Consumable DREAD_NOG_BUCKET_CONSUMABLE = bucketDrink(DREAD_NOG_CONSUMABLE);
    public static final FoodProperties WITHERING_DROSS = WITHERING_DROSS_TOTAL.perCup();
    public static final Consumable WITHERING_DROSS_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 3600, 2),
            new MobEffectInstance(BnCEffects.INTOXICATION, 3000, 0, false, false),
            new MobEffectInstance(MobEffects.BLINDNESS, 200, 0),
            new MobEffectInstance(MobEffects.WEAKNESS, 3000, 0),
            new MobEffectInstance(MobEffects.SLOWNESS, 3000, 0),
            new MobEffectInstance(MobEffects.WITHER, 1200, 0)
    );
    public static final Consumable WITHERING_DROSS_BUCKET_CONSUMABLE = bucketDrink(WITHERING_DROSS_CONSUMABLE);

    // TODO: Give this more attention after the Farmer's Respite rework.
    public static final FoodProperties KOMBUCHA = KOMBUCHA_TOTAL.perCup();
    public static final Consumable KOMBUCHA_CONSUMABLE = drink(
            new MobEffectInstance(BnCEffects.TIPSY, 2400, 0),
            new MobEffectInstance(BnCEffects.INTOXICATION, 1800, 0, false, false),
            new MobEffectInstance(MobEffects.HASTE, 1200, 1)
    );
    public static final Consumable KOMBUCHA_BUCKET_CONSUMABLE = bucketDrink(KOMBUCHA_CONSUMABLE);

    public static final FoodProperties FLAXEN_CHEESE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(1.0F).build();
    public static final FoodProperties SCARLET_CHEESE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(1.0F).build();

    public static final FoodProperties VEGETABLE_OMELET = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(0.8F).build();
    public static final Consumable VEGETABLE_OMELET_CONSUMABLE = food(new MobEffectInstance(ModEffects.NOURISHMENT, 3600, 0, false, false));
    public static final FoodProperties CREAMY_ONION_SOUP = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(0.8F).build();
    public static final Consumable CREAMY_ONION_SOUP_CONSUMABLE = food(new MobEffectInstance(ModEffects.COMFORT, 3600, 0, false, false));
    public static final FoodProperties CHEESY_PASTA = (new FoodProperties.Builder())
            .nutrition(14).saturationModifier(0.75F).build();
    public static final Consumable CHEESY_PASTA_CONSUMABLE = food(new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, false, false));
    public static final FoodProperties HORROR_LASAGNA = (new FoodProperties.Builder())
            .nutrition(16).saturationModifier(0.55F).build();
    public static final Consumable HORROR_LASAGNA_CONSUMABLE = food(new MobEffectInstance(ModEffects.NOURISHMENT, 6000, 0, false, false));
    public static final FoodProperties SCARLET_PIEROGI = (new FoodProperties.Builder())
            .nutrition(12).saturationModifier(1.0F).build();
    public static final Consumable SCARLET_PIEROGI_CONSUMABLE = food(new MobEffectInstance(ModEffects.NOURISHMENT, 8400, 0, false, false));
    public static final FoodProperties FIERY_FONDUE = (new FoodProperties.Builder())
            .nutrition(14).saturationModifier(0.75f).build();
    public static final Consumable FIERY_FONDUE_CONSUMABLE = food(new MobEffectInstance(ModEffects.COMFORT, 8400, 0, false, false));

    public static final FoodProperties PIZZA_SLICE = (new FoodProperties.Builder())
            .nutrition(5).saturationModifier(1.0F).build();
    public static final FoodProperties QUICHE_SLICE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.8F).build();
    public static final Consumable FAST_FOOD = Consumables.defaultFood().consumeSeconds(0.8F).build();

    public static final FoodProperties HAM_AND_CHEESE_SANDWICH = (new FoodProperties.Builder())
            .nutrition(9).saturationModifier(1.0F).build();

    public static final FoodProperties KIMCHI = (new FoodProperties.Builder())
            .nutrition(2).saturationModifier(0.6F).build();
    public static final FoodProperties JERKY = (new FoodProperties.Builder())
            .nutrition(3).saturationModifier(0.7F).build();
    public static final FoodProperties PICKLED_PICKLES = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.3F).build();
    public static final FoodProperties KIPPERS = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.5F).build();
    public static final FoodProperties COCOA_FUDGE = (new FoodProperties.Builder())
            .nutrition(4).saturationModifier(0.8F).build();
    public static final Consumable COCOA_FUDGE_CONSUMABLE = food(new MobEffectInstance(MobEffects.SPEED, 800, 0, false, false));

    public static final FoodProperties SWEET_BERRY_JAM = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties GLOW_BERRY_MARMALADE = (new FoodProperties.Builder())
            .nutrition(6).saturationModifier(0.4F).build();
    public static final FoodProperties APPLE_JELLY = (new FoodProperties.Builder())
            .nutrition(10).saturationModifier(0.6F).build();

    public static FoodProperties bucketFood(FoodProperties cupFood) {
        return new FoodProperties(cupFood.nutrition() * CUPS_PER_BUCKET, cupFood.saturation() * CUPS_PER_BUCKET, true);
    }

    private static FoodAmount food(FoodProperties food) {
        return new FoodAmount(food.nutrition(), food.saturation());
    }

    private static Consumable food(MobEffectInstance... effects) {
        return Consumables.defaultFood()
                .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects)))
                .build();
    }

    private static Consumable drink(MobEffectInstance... effects) {
        return Consumables.defaultDrink()
                .consumeSeconds(1.6F)
                .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(effects)))
                .build();
    }

    private static Consumable bucketDrink(Consumable cupDrink) {
        return new Consumable(
                cupDrink.consumeSeconds(),
                cupDrink.animation(),
                cupDrink.sound(),
                cupDrink.hasConsumeParticles(),
                cupDrink.onConsumeEffects().stream().map(BnCFoods::bucketConsumeEffect).toList()
        );
    }

    private static ConsumeEffect bucketConsumeEffect(ConsumeEffect effect) {
        if (effect instanceof ApplyStatusEffectsConsumeEffect statusEffects) {
            return new ApplyStatusEffectsConsumeEffect(
                    statusEffects.effects().stream().map(BnCFoods::bucketStatusEffect).toList(),
                    statusEffects.probability()
            );
        }
        return effect;
    }

    private static MobEffectInstance bucketStatusEffect(MobEffectInstance effect) {
        int duration = effect.getDuration() == -1 ? -1 : effect.getDuration() * CUPS_PER_BUCKET;
        int amplifier = effect.getAmplifier();
        if (effect.getEffect() == BnCEffects.TIPSY) {
            amplifier = Math.min(amplifier * CUPS_PER_BUCKET + CUPS_PER_BUCKET - 1, 9);
        } else if (effect.getEffect() == MobEffects.BAD_OMEN) {
            duration = effect.getDuration();
            amplifier = Math.min(amplifier * CUPS_PER_BUCKET + CUPS_PER_BUCKET - 1, 4);
        }
        return new MobEffectInstance(effect.getEffect(), duration, amplifier, effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    private record FoodAmount(float nutrition, float saturation) {
        private FoodAmount add(FoodAmount other) {
            return new FoodAmount(this.nutrition + other.nutrition, this.saturation + other.saturation);
        }

        private FoodAmount times(float amount) {
            return new FoodAmount(this.nutrition * amount, this.saturation * amount);
        }

        private FoodAmount divide(float amount) {
            return new FoodAmount(this.nutrition / amount, this.saturation / amount);
        }

        private FoodProperties perCup() {
            return new FoodProperties((int)Math.ceil(this.nutrition / CUPS_PER_BUCKET), this.saturation / CUPS_PER_BUCKET, true);
        }
    }
}
