package umpaz.brewinandchewin.platform.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.List;

public interface BnCClientPlatformHelper {
    BlockStateModel getModel(Identifier modelId);

    void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphicsExtractor graphics, int x, int y, float alphaModifier, long capacity);

    default <T extends Recipe<?>> List<RecipeHolder<T>> getSynchronizedRecipes(RecipeType<T> recipeType) {
        return List.of();
    }
}
