package umpaz.brewinandchewin.common.utility;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;

public class BnCMathUtils {
    public static int redstoneFromItemHandler(@Nullable AbstractedItemHandler handler) {
        if (handler == null) {
            return 0;
        } else {
            int i = 0;
            float f = 0.0F;

            for(int j = 0; j < handler.getSlotCount(); ++j) {
                ItemStack itemstack = handler.getStackInSlot(j);
                if (!itemstack.isEmpty()) {
                    f += (float)itemstack.getCount() / (float)Math.min(handler.getSlotLimit(j), itemstack.getMaxStackSize());
                    ++i;
                }
            }

            f /= (float)handler.getSlotCount();
            return Mth.floor(f * 14.0F) + (i > 0 ? 1 : 0);
        }
    }
}
