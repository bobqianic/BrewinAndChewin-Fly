package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

public class BnCBlockEntityTypes {
    public static final BlockEntityType<KegBlockEntity> KEG = BrewinAndChewin.getHelper().createBlockEntityType(BrewinAndChewin.getHelper().supplyBlockEntity(), BnCBlocks.KEG, BnCBlocks.LARGE_KEG);
    public static final BlockEntityType<CoasterBlockEntity> COASTER = BrewinAndChewin.getHelper().createBlockEntityType(CoasterBlockEntity::new, BnCBlocks.COASTER);

    public static void registerAll() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("keg"), KEG);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BrewinAndChewin.asResource("coaster"), COASTER);
    }
}
