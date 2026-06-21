package umpaz.brewinandchewin.common.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public interface BnCMenuConstructor<T extends AbstractContainerMenu> {
    T apply(int id, Inventory inventory, BlockPos data);
}
