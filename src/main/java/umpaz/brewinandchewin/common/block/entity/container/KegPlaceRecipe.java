package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class KegPlaceRecipe {
    private KegPlaceRecipe() {
    }

    public static void handleFluidPlacement(KegMenu menu, RecipeManager manager, Inventory inventory, RecipeHolder<KegFermentingRecipe> recipe, boolean placeAll) {
        KegFermentingRecipe fermentingRecipe = recipe.value();
        AbstractedFluidTank kegTank = menu.kegTank;
        boolean recipeFluidMatches = fermentingRecipe.getFluidIngredient().isEmpty() && kegTank.isEmpty()
                || fermentingRecipe.getFluidIngredient().isPresent()
                && fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid())
                && kegTank.getAbstractedFluid().amount() >= fermentingRecipe.getFluidIngredient().get().loaderAmount() * menu.getFermentationScale();

        if (recipeFluidMatches) {
            return;
        }

        if (fermentingRecipe.getFluidIngredient().isEmpty()) {
            emptyCurrentFluid(menu, manager, inventory);
        } else {
            replaceOrFillFluid(menu, manager, inventory, fermentingRecipe, placeAll);
        }
    }

    private static void emptyCurrentFluid(KegMenu menu, RecipeManager manager, Inventory inventory) {
        KegBlockEntity blockEntity = menu.blockEntity;
        AbstractedFluidTank kegTank = menu.kegTank;
        if (kegTank.isEmpty()) {
            return;
        }

        List<KegPouringRecipe> pouringRecipes = getPouringRecipes(manager);
        for (int i = 0; i < inventory.getContainerSize() && !kegTank.isEmpty(); ++i) {
            ItemStack stack = inventory.getItem(i);
            Optional<KegPouringRecipe> recipe = findExtractRecipe(pouringRecipes, stack, kegTank);
            if (recipe.isPresent()) {
                int slot = i;
                blockEntity.extractInGui(stack, stack.getCount()).forEach(result -> {
                    if (!inventory.add(inventory.getItem(slot).isEmpty() ? slot : inventory.getSlotWithRemainingSpace(result), result)) {
                        inventory.player.drop(result, false);
                    }
                });
            }
        }
    }

    private static void replaceOrFillFluid(KegMenu menu, RecipeManager manager, Inventory inventory, KegFermentingRecipe fermentingRecipe, boolean placeAll) {
        KegBlockEntity blockEntity = menu.blockEntity;
        AbstractedFluidTank kegTank = menu.kegTank;

        if (!kegTank.isEmpty() && !fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid())) {
            emptyCurrentFluid(menu, manager, inventory);
        }

        long recipeAmount = fermentingRecipe.getFluidIngredient().get().loaderAmount() * menu.getFermentationScale();
        long targetAmount = Math.min(kegTank.getFluidCapacity(), recipeAmount * (placeAll ? Math.max(1, menu.getGridWidth() * menu.getGridHeight()) : 1L));
        long missingAmount = Math.max(0L, targetAmount - (fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegTank.getAbstractedFluid()) ? kegTank.getAbstractedFluid().amount() : 0L));
        if (missingAmount <= 0) {
            return;
        }

        List<KegPouringRecipe> pouringRecipes = getPouringRecipes(manager);
        List<RecipeItem> insertItems = new ArrayList<>();
        long fluidToInsert = 0;
        for (int i = 0; i < inventory.getContainerSize() && fluidToInsert < missingAmount; ++i) {
            ItemStack stack = inventory.getItem(i);
            Optional<KegPouringRecipe> recipe = findFillRecipe(pouringRecipes, stack, fermentingRecipe);
            if (recipe.isPresent()) {
                long loaderAmount = recipe.get().getLoaderAmount();
                int itemAmount = (int) Mth.clamp((missingAmount - fluidToInsert + loaderAmount - 1) / loaderAmount, 1, stack.getCount());
                ItemStack outputStack = recipe.get().getContainer().copyWithCount(itemAmount);
                insertItems.add(new RecipeItem(i, itemAmount, recipe.get().getLoaderAmount() * itemAmount, outputStack));
                fluidToInsert += recipe.get().getLoaderAmount() * itemAmount;
            }
        }

        for (RecipeItem insertItem : insertItems) {
            inventory.getItem(insertItem.slot).shrink(insertItem.maxInsert);
            List<ItemStack> inserted = blockEntity.extractInGui(insertItem.output, insertItem.maxInsert);
            inserted.forEach(stack -> {
                if (!inventory.add(inventory.getItem(insertItem.slot).isEmpty() ? insertItem.slot : inventory.getSlotWithRemainingSpace(stack), stack)) {
                    inventory.player.drop(stack, false);
                }
            });
        }
    }

    private static List<KegPouringRecipe> getPouringRecipes(RecipeManager manager) {
        return BnCRecipeUtils.getRecipes(manager, BnCRecipeTypes.KEG_POURING).stream()
                .map(RecipeHolder::value)
                .toList();
    }

    private static Optional<KegPouringRecipe> findExtractRecipe(List<KegPouringRecipe> pouringRecipes, ItemStack stack, AbstractedFluidTank kegTank) {
        return pouringRecipes.stream()
                .filter(kegPouringRecipe -> kegPouringRecipe.matchesFluid(stack, kegTank.getAbstractedFluid()))
                .filter(pouring -> pouring.isStrict() ? ItemStack.isSameItemSameComponents(stack, pouring.getContainer()) : ItemStack.isSameItem(stack, pouring.getContainer()))
                .findFirst();
    }

    private static Optional<KegPouringRecipe> findFillRecipe(List<KegPouringRecipe> pouringRecipes, ItemStack stack, KegFermentingRecipe fermentingRecipe) {
        return pouringRecipes.stream()
                .filter(kegPouringRecipe -> kegPouringRecipe.canFill() && fermentingRecipe.getFluidIngredient().get().ingredient().matches(kegPouringRecipe.getFluid(stack)))
                .filter(pouring -> pouring.isStrict() ? ItemStack.isSameItemSameComponents(stack, pouring.getOutput()) : ItemStack.isSameItem(stack, pouring.getOutput()))
                .findFirst();
    }

    private record RecipeItem(int slot, int maxInsert, long fluidAmount, ItemStack output) {
    }
}
