package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public class KegStackedContents extends StackedItemContents {
    public final KegMenu menu;
    public final RecipeManager recipeManager;

    public KegStackedContents(KegMenu menu, RecipeManager manager) {
        this.menu = menu;
        this.recipeManager = manager;
    }

    public record PouringEntry(ItemStack stack, long fluidAmount, FluidUnit fluidUnit, boolean strict) {
    }
}
