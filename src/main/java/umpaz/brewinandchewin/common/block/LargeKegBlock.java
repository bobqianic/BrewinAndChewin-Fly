package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import umpaz.brewinandchewin.common.registry.BnCBlocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LargeKegBlock extends KegBlock {
    public static final MapCodec<LargeKegBlock> CODEC = simpleCodec(LargeKegBlock::new);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(2.0D, 4.0D, 0.0D, 30.0D, 32.0D, 32.0D),
            Block.box(24.0D, 0.0D, 26.0D, 30.0D, 4.0D, 32.0D),
            Block.box(2.0D, 0.0D, 26.0D, 8.0D, 4.0D, 32.0D),
            Block.box(2.0D, 0.0D, 0.0D, 8.0D, 4.0D, 6.0D),
            Block.box(24.0D, 0.0D, 0.0D, 30.0D, 4.0D, 6.0D)
    ).optimize();
    private static final VoxelShape SHAPE_EAST = rotateShape(SHAPE_NORTH, 1);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(SHAPE_NORTH, 2);
    private static final VoxelShape SHAPE_WEST = rotateShape(SHAPE_NORTH, 3);
    private static final VoxelShape SHAPE_VERTICAL_NORTH = Block.box(2.0D, 0.0D, 2.0D, 30.0D, 32.0D, 30.0D);
    private static final VoxelShape SHAPE_VERTICAL_EAST = rotateShape(SHAPE_VERTICAL_NORTH, 1);
    private static final VoxelShape SHAPE_VERTICAL_SOUTH = rotateShape(SHAPE_VERTICAL_NORTH, 2);
    private static final VoxelShape SHAPE_VERTICAL_WEST = rotateShape(SHAPE_VERTICAL_NORTH, 3);

    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_NORTH = createFootprintShapes(SHAPE_NORTH);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_EAST = createFootprintShapes(SHAPE_EAST);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_SOUTH = createFootprintShapes(SHAPE_SOUTH);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_WEST = createFootprintShapes(SHAPE_WEST);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_VERTICAL_NORTH = createFootprintShapes(SHAPE_VERTICAL_NORTH);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_VERTICAL_EAST = createFootprintShapes(SHAPE_VERTICAL_EAST);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_VERTICAL_SOUTH = createFootprintShapes(SHAPE_VERTICAL_SOUTH);
    private static final Map<BlockPos, VoxelShape> FOOTPRINT_SHAPES_VERTICAL_WEST = createFootprintShapes(SHAPE_VERTICAL_WEST);
    private static final BlockPos[] FOOTPRINT_OFFSETS = createRemovalOffsets();

    public LargeKegBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getOutlineShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getFootprintShape(state, BlockPos.ZERO);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state != null && canPlaceAt(context.getLevel(), context.getClickedPos(), state) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        placeFootprint(level, pos, state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        removeFootprint(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    public static VoxelShape getFootprintShape(BlockState state, BlockPos offset) {
        return getFootprintShapes(state).getOrDefault(offset, Shapes.empty());
    }

    public static Set<BlockPos> getFootprintOffsets(BlockState state) {
        return getFootprintShapes(state).keySet();
    }

    public BlockState getUnvalidatedStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    public static VoxelShape getOutlineShapeForState(BlockState state) {
        return getOutlineShape(state);
    }

    public static BlockPos findBestAnchor(Level level, BlockPos clickedPos, BlockState state) {
        BlockPos bestAnchor = null;
        int bestPriority = Integer.MAX_VALUE;

        for (BlockPos offset : getFootprintShapes(state).keySet()) {
            if (offset.getY() != 0) {
                continue;
            }
            BlockPos anchor = clickedPos.subtract(offset);
            if (!canPlaceAt(level, anchor, state)) {
                continue;
            }
            int priority = getPlacementPriority(state, offset);
            if (priority < bestPriority || priority == bestPriority && compareOffsets(offset, clickedPos.subtract(bestAnchor)) < 0) {
                bestAnchor = anchor;
                bestPriority = priority;
            }
        }

        return bestAnchor == null ? clickedPos : bestAnchor;
    }

    private static int getPlacementPriority(BlockState state, BlockPos offset) {
        Direction facing = state.getValue(FACING);
        Direction side = facing.getClockWise();
        int forwardDistance = Math.abs(offset.getX() * facing.getStepX() + offset.getZ() * facing.getStepZ());
        int sideDistance = Math.abs(offset.getX() * side.getStepX() + offset.getZ() * side.getStepZ());
        return forwardDistance * 2 + sideDistance;
    }

    private static int compareOffsets(BlockPos first, BlockPos second) {
        if (second == null) {
            return -1;
        }
        int firstDistance = Math.abs(first.getX()) + Math.abs(first.getZ());
        int secondDistance = Math.abs(second.getX()) + Math.abs(second.getZ());
        if (firstDistance != secondDistance) {
            return Integer.compare(firstDistance, secondDistance);
        }
        if (first.getX() != second.getX()) {
            return Integer.compare(first.getX(), second.getX());
        }
        return Integer.compare(first.getZ(), second.getZ());
    }

    public static boolean canPlaceAt(Level level, BlockPos pos, BlockState kegState) {
        for (Map.Entry<BlockPos, VoxelShape> entry : getFootprintShapes(kegState).entrySet()) {
            BlockPos offset = entry.getKey();
            BlockPos footprintPos = pos.offset(offset);
            BlockState state = level.getBlockState(footprintPos);
            if (state.is(BnCBlocks.LARGE_KEG_FOOTPRINT)) {
                BlockPos existingKegPos = LargeKegFootprintBlock.getKegPos(footprintPos, state);
                if (level.getBlockState(existingKegPos).is(BnCBlocks.LARGE_KEG)) {
                    return false;
                }
            } else if (!state.canBeReplaced()) {
                return false;
            }
            if (!isFootprintUnobstructed(level, footprintPos, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static VoxelShape getOutlineShape(BlockState state) {
        if (state.getValue(VERTICAL)) {
            return switch (state.getValue(FACING)) {
                case EAST -> SHAPE_VERTICAL_EAST;
                case SOUTH -> SHAPE_VERTICAL_SOUTH;
                case WEST -> SHAPE_VERTICAL_WEST;
                default -> SHAPE_VERTICAL_NORTH;
            };
        }
        return switch (state.getValue(FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    private static Map<BlockPos, VoxelShape> createFootprintShapes(VoxelShape collisionShape) {
        Map<BlockPos, VoxelShape> shapes = new HashMap<>();
        for (AABB aabb : collisionShape.toAabbs()) {
            int minX = (int) Math.floor(aabb.minX);
            int minY = (int) Math.floor(aabb.minY);
            int minZ = (int) Math.floor(aabb.minZ);
            int maxX = (int) Math.ceil(aabb.maxX);
            int maxY = (int) Math.ceil(aabb.maxY);
            int maxZ = (int) Math.ceil(aabb.maxZ);

            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        AABB cellBounds = new AABB(x, y, z, x + 1, y + 1, z + 1);
                        AABB intersection = new AABB(
                                Math.max(aabb.minX, cellBounds.minX),
                                Math.max(aabb.minY, cellBounds.minY),
                                Math.max(aabb.minZ, cellBounds.minZ),
                                Math.min(aabb.maxX, cellBounds.maxX),
                                Math.min(aabb.maxY, cellBounds.maxY),
                                Math.min(aabb.maxZ, cellBounds.maxZ)
                        );

                        if (intersection.getXsize() > 0.0D && intersection.getYsize() > 0.0D && intersection.getZsize() > 0.0D) {
                            BlockPos offset = new BlockPos(x, y, z);
                            VoxelShape localShape = Shapes.create(intersection.move(-x, -y, -z));
                            shapes.merge(offset, localShape, Shapes::or);
                        }
                    }
                }
            }
        }
        shapes.replaceAll((offset, shape) -> shape.optimize());
        return shapes;
    }

    private static Map<BlockPos, VoxelShape> getFootprintShapes(BlockState state) {
        if (state.getValue(VERTICAL)) {
            return switch (state.getValue(FACING)) {
                case EAST -> FOOTPRINT_SHAPES_VERTICAL_EAST;
                case SOUTH -> FOOTPRINT_SHAPES_VERTICAL_SOUTH;
                case WEST -> FOOTPRINT_SHAPES_VERTICAL_WEST;
                default -> FOOTPRINT_SHAPES_VERTICAL_NORTH;
            };
        }
        return switch (state.getValue(FACING)) {
            case EAST -> FOOTPRINT_SHAPES_EAST;
            case SOUTH -> FOOTPRINT_SHAPES_SOUTH;
            case WEST -> FOOTPRINT_SHAPES_WEST;
            default -> FOOTPRINT_SHAPES_NORTH;
        };
    }

    private static boolean isFootprintUnobstructed(Level level, BlockPos pos, VoxelShape shape) {
        for (AABB bounds : shape.toAabbs()) {
            if (!level.noCollision(bounds.move(pos))) {
                return false;
            }
        }
        return true;
    }

    private static void placeFootprint(Level level, BlockPos pos, BlockState kegState) {
        for (BlockPos offset : getFootprintShapes(kegState).keySet()) {
            if (offset.equals(BlockPos.ZERO)) {
                continue;
            }
            BlockPos footprintPos = pos.offset(offset);
            level.setBlock(footprintPos, LargeKegFootprintBlock.stateForOffset(offset), Block.UPDATE_ALL);
        }
    }

    private static VoxelShape rotateShape(VoxelShape shape, int quarterTurns) {
        VoxelShape rotated = Shapes.empty();
        for (AABB bounds : shape.toAabbs()) {
            AABB rotatedBounds = bounds;
            for (int i = 0; i < quarterTurns; i++) {
                rotatedBounds = new AABB(
                        1.0D - rotatedBounds.maxZ,
                        rotatedBounds.minY,
                        rotatedBounds.minX,
                        1.0D - rotatedBounds.minZ,
                        rotatedBounds.maxY,
                        rotatedBounds.maxX
                );
            }
            rotated = Shapes.or(rotated, Shapes.create(rotatedBounds));
        }
        return rotated.optimize();
    }

    private static BlockPos[] createRemovalOffsets() {
        Set<BlockPos> offsets = new HashSet<>();
        addOffsets(offsets, FOOTPRINT_SHAPES_NORTH);
        addOffsets(offsets, FOOTPRINT_SHAPES_EAST);
        addOffsets(offsets, FOOTPRINT_SHAPES_SOUTH);
        addOffsets(offsets, FOOTPRINT_SHAPES_WEST);
        addOffsets(offsets, FOOTPRINT_SHAPES_VERTICAL_NORTH);
        addOffsets(offsets, FOOTPRINT_SHAPES_VERTICAL_EAST);
        addOffsets(offsets, FOOTPRINT_SHAPES_VERTICAL_SOUTH);
        addOffsets(offsets, FOOTPRINT_SHAPES_VERTICAL_WEST);
        offsets.remove(BlockPos.ZERO);
        return offsets.toArray(BlockPos[]::new);
    }

    private static void addOffsets(Set<BlockPos> offsets, Map<BlockPos, VoxelShape> shapes) {
        offsets.addAll(shapes.keySet());
    }

    private static void removeFootprint(Level level, BlockPos pos) {
        for (BlockPos offset : FOOTPRINT_OFFSETS) {
            BlockPos footprintPos = pos.offset(offset);
            BlockState state = level.getBlockState(footprintPos);
            if (state.is(BnCBlocks.LARGE_KEG_FOOTPRINT) && LargeKegFootprintBlock.getKegPos(footprintPos, state).equals(pos)) {
                level.setBlock(footprintPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
