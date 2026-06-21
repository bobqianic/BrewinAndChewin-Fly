package umpaz.brewinandchewin.integration.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeUtils;


import java.util.*;

public class BnCJEIRecipes {

    public List<KegFermentingPouringRecipe> getKegRecipes() {

        List<RecipeHolder<KegFermentingRecipe>> ferms = BnCClientRecipeUtils.getRecipes(BnCRecipeTypes.FERMENTING);
        List<RecipeHolder<KegPouringRecipe>> pours = BnCClientRecipeUtils.getRecipes(BnCRecipeTypes.KEG_POURING);

        List<KegFermentingPouringRecipe> kegRecipes = new ArrayList<>();

        // add all of ferms
        for (RecipeHolder<KegFermentingRecipe> fermentingRecipe : ferms) {
            if (fermentingRecipe.value().getResult().left().isPresent()) {
                for (RecipeHolder<KegPouringRecipe> pouringRecipe : pours) {
                    if (pouringRecipe.value().matchesFluid(ItemStack.EMPTY, fermentingRecipe.value().getResult().left().get())) {
                        kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe.id().location(), fermentingRecipe.value(), pouringRecipe.value(), Minecraft.getInstance().level.registryAccess()));
                    }
                }
            }
            else {
                kegRecipes.add(new KegFermentingPouringRecipe(fermentingRecipe.id().location(), fermentingRecipe.value(), null, Minecraft.getInstance().level.registryAccess()));
            }
        }


        return kegRecipes;
    }


   public List<CheeseAgingRecipe> getCheeseRecipes() {
      List<CheeseAgingRecipe> cheese = new ArrayList<>();


      cheese.add(new CheeseAgingRecipe(BnCBlocks.UNRIPE_FLAXEN_CHEESE_WHEEL.asItem(), BnCBlocks.FLAXEN_CHEESE_WHEEL.asItem()));
      cheese.add(new CheeseAgingRecipe(BnCBlocks.UNRIPE_SCARLET_CHEESE_WHEEL.asItem(), BnCBlocks.SCARLET_CHEESE_WHEEL.asItem()));

      // find every instance of Unripe Cheese Wheel block, and call the supplier :)
      return cheese;
   }
}
