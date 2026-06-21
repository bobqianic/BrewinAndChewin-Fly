package umpaz.brewinandchewin.fabric.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.registry.BnCCreativeTabs;
import umpaz.brewinandchewin.common.registry.BnCItems;

public class BnCCreativeTabsImpl {
    public static void init() {
        BnCCreativeTabs.TAB_BREWIN_AND_CHEWIN = FabricItemGroup.builder().title(Component.translatable("itemGroup.brewinandchewin"))
                .icon(() -> new ItemStack(BnCItems.KEG))
                .displayItems((parameters, output) -> BnCItems.CREATIVE_TAB_ITEMS.forEach(output::accept))
                .build();
    }
}
