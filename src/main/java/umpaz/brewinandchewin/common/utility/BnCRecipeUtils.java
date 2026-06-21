package umpaz.brewinandchewin.common.utility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeUtils;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BnCRecipeUtils {
    @SuppressWarnings("unchecked")
    public static <T extends Recipe<?>> List<RecipeHolder<T>> getRecipes(RecipeManager recipeManager, RecipeType<T> recipeType) {
        return recipeManager.getRecipes().stream()
                .filter(recipeHolder -> recipeHolder.value().getType() == recipeType)
                .map(recipeHolder -> (RecipeHolder<T>) recipeHolder)
                .toList();
    }

    public static ItemStack getPouredItemFromFluid(AbstractedFluidStack fluid) {
        if (fluid.isEmpty())
            return ItemStack.EMPTY;
        if (BrewinAndChewin.isClient)
            return BnCClientRecipeUtils.getPouredItemFromFluid(fluid);
        MinecraftServer server = BrewinAndChewin.getHelper().getServer();
        if (server == null)
            return ItemStack.EMPTY;
        Optional<KegPouringRecipe> recipe = getRecipes(server.getRecipeManager(), BnCRecipeTypes.KEG_POURING).stream()
                .map(RecipeHolder::value)
                .sorted(Comparator.comparing(KegPouringRecipe::isStrict))
                .filter(kegPouringRecipe -> kegPouringRecipe.matchesFluid(ItemStack.EMPTY, fluid))
                .findFirst();
        return recipe.map(KegPouringRecipe::getOutput).orElse(ItemStack.EMPTY);
    }
}
