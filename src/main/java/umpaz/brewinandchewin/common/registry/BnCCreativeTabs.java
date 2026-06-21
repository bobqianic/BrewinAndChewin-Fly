package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCCreativeTabs {
    public static CreativeModeTab TAB_BREWIN_AND_CHEWIN;

    public static void registerAll() {
        BrewinAndChewin.getHelper().initCreativeTab();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BrewinAndChewin.asResource("brewinandchewin"), TAB_BREWIN_AND_CHEWIN);
    }
}
