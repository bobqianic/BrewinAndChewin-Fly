package umpaz.brewinandchewin.integration.jei;

import mezz.jei.api.recipe.RecipeType;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCJEIRecipeTypes {
    public static final RecipeType<KegFermentingPouringRecipe> FERMENTING = RecipeType.create(BrewinAndChewin.MODID, "fermenting", KegFermentingPouringRecipe.class);
   public static final RecipeType<CheeseAgingRecipe> AGING = RecipeType.create(BrewinAndChewin.MODID, "aging", CheeseAgingRecipe.class);

}
