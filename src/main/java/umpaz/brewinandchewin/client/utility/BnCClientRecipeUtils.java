package umpaz.brewinandchewin.client.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Recipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BnCClientRecipeUtils {
    public static <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(RecipeType<T> recipeType) {
        RecipeManager recipeManager = Minecraft.getInstance().getSingleplayerServer() != null
                ? Minecraft.getInstance().getSingleplayerServer().getRecipeManager()
                : null;
        if (recipeManager != null) {
            return BnCRecipeUtils.getRecipes(recipeManager, recipeType);
        }
        List<RecipeHolder<T>> synchronizedRecipes = BrewinAndChewinClient.getHelper().getSynchronizedRecipes(recipeType);
        return synchronizedRecipes.isEmpty() ? BnCClientRecipeCache.getRecipes(recipeType) : synchronizedRecipes;
    }

    public static List<KegPouringRecipe> getPouringRecipes() {
        return getRecipes(BnCRecipeTypes.KEG_POURING).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    public static ItemStack getPouredItemFromFluid(AbstractedFluidStack fluid) {
        if (fluid.isEmpty() || Minecraft.getInstance().level == null)
            return ItemStack.EMPTY;
        RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
        ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(registryAccess, fluid);

        if (!itemDisplay.isEmpty())
            return itemDisplay;

        Optional<KegPouringRecipe> recipe = getPouringRecipes().stream()
                .sorted(Comparator.comparing(KegPouringRecipe::isStrict))
                .filter(kegPouringRecipe -> kegPouringRecipe.matchesFluid(ItemStack.EMPTY, fluid))
                .findFirst();
        return recipe.map(KegPouringRecipe::getOutput).orElse(ItemStack.EMPTY);
    }
}
