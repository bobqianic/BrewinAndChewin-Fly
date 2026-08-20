package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;

public class LargeKegFootprintBlockEntity extends BlockEntity {
    public LargeKegFootprintBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.LARGE_KEG_FOOTPRINT, pos, state);
    }
}
