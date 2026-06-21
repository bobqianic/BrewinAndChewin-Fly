package umpaz.brewinandchewin.integration.jei;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class CheeseAgingRecipe implements Recipe<RecipeInput> {
    private final Item before;
    private final Item after;

    public CheeseAgingRecipe(Item before, Item after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public boolean matches(RecipeInput recipeWrapper, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return new ItemStack(this.after, 1);
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(this.after, 1);
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> retVal = NonNullList.create();
        retVal.add(Ingredient.of(this.before));
        return retVal;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(Ingredient.of(this.before));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return null;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }
}
