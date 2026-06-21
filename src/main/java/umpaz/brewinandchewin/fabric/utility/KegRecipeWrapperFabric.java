package umpaz.brewinandchewin.fabric.utility;

import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;
import vectorwing.farmersdelight.refabricated.inventory.ItemHandler;

public class KegRecipeWrapperFabric implements KegRecipeWrapper {
    private final ItemHandler inventory;
    private final AbstractedFluidTank tank;

    public KegRecipeWrapperFabric(ItemHandler itemHandler, AbstractedFluidTank fluidHandler) {
        this.inventory = itemHandler;
        this.tank = fluidHandler;
    }

    @Override
    public AbstractedFluidStack getFluid() {
        return tank.getAbstractedFluid();
    }

    @Override
    public long getTankCapacity() {
        return tank.getFluidCapacity();
    }

    @Override
    public ItemStack getItem(int i) {
        return inventory.getStackInSlot(i);
    }

    @Override
    public int size() {
        return inventory.getSlotCount();
    }
}
