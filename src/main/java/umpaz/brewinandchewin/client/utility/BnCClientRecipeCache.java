package umpaz.brewinandchewin.client.utility;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public final class BnCClientRecipeCache {
    private static List<RecipeHolder<?>> recipes = List.of();

    private BnCClientRecipeCache() {
    }

    public static void setRecipes(List<RecipeHolder<?>> recipes) {
        BnCClientRecipeCache.recipes = List.copyOf(recipes);
    }

    public static void clear() {
        recipes = List.of();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(RecipeType<T> recipeType) {
        List<RecipeHolder<T>> result = new ArrayList<>();
        for (RecipeHolder<?> recipe : recipes) {
            if (recipe.value().getType() == recipeType) {
                result.add((RecipeHolder<T>) recipe);
            }
        }
        return result;
    }
}
