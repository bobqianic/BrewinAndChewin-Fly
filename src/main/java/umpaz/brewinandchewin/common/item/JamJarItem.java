package umpaz.brewinandchewin.common.item;

import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class JamJarItem extends ConsumableItem {
    public JamJarItem(Properties pProperties) {
        super(pProperties);
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

}
