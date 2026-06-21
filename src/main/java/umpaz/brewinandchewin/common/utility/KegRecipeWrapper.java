package umpaz.brewinandchewin.common.utility;

import net.minecraft.world.item.crafting.RecipeInput;

public interface KegRecipeWrapper extends RecipeInput {
    AbstractedFluidStack getFluid();
    long getTankCapacity();

    @Override
    default boolean isEmpty() {
        if (getFluid().isEmpty())
            return false;
        return RecipeInput.super.isEmpty();
    }
}
