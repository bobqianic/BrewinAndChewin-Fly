package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;

public class BnCMenuTypes {
    public static final MenuType<KegMenu> KEG = BrewinAndChewin.getHelper().createMenuType(KegMenu::new);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.MENU, BrewinAndChewin.asResource("keg"), KEG);
    }
}
