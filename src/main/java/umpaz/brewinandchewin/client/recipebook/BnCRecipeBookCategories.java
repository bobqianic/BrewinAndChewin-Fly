package umpaz.brewinandchewin.client.recipebook;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCRecipeBookCategories {
    public static final RecipeBookCategory FERMENTING_DRINKS = register("fermenting_drinks");
    public static final RecipeBookCategory FERMENTING_MEALS = register("fermenting_meals");

    public static void registerAll() {
    }

    private static RecipeBookCategory register(String name) {
        return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, BrewinAndChewin.asResource(name), new RecipeBookCategory());
    }
}
