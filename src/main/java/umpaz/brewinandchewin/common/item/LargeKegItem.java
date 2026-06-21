package umpaz.brewinandchewin.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.LargeKegBlock;

public class LargeKegItem extends KegItem {
    public LargeKegItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPlaceContext updatedContext = super.updatePlacementContext(context);
        if (updatedContext == null) {
            return null;
        }

        BlockState state = ((LargeKegBlock) getBlock()).getUnvalidatedStateForPlacement(updatedContext);
        if (state == null) {
            return updatedContext;
        }

        Level level = updatedContext.getLevel();
        BlockPos anchorPos = LargeKegBlock.findBestAnchor(level, updatedContext.getClickedPos(), state);
        if (anchorPos.equals(updatedContext.getClickedPos())) {
            return updatedContext;
        }

        return new BlockPlaceContext(
                level,
                updatedContext.getPlayer(),
                updatedContext.getHand(),
                updatedContext.getItemInHand(),
                new BlockHitResult(Vec3.atCenterOf(anchorPos), Direction.UP, anchorPos, false)
        );
    }
}
