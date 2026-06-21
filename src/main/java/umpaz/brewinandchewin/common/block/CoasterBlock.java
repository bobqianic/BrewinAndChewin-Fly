package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import umpaz.brewinandchewin.common.utility.CoasterModelCollisionShapes;

import java.util.List;

public class CoasterBlock extends BaseEntityBlock {
    public static final MapCodec<CoasterBlock> CODEC = MapCodec.unit(CoasterBlock::new);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 0, 4);
    public static final BooleanProperty INVISIBLE = BooleanProperty.create("invisible");

    protected static final VoxelShape COASTER_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);

    public CoasterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(ROTATION, 0).setValue(SIZE, 0).setValue(INVISIBLE, false));
    }

    public CoasterBlock() {
        this(Properties.ofFullCopy(Blocks.BROWN_CARPET).sound(SoundType.WOOD).instabreak().dynamicShape());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return COASTER_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(SIZE) > 0) {
            VoxelShape itemShape = getDisplayedItemCollisionShape(worldIn, pos);
            if (!itemShape.isEmpty()) {
                return state.getValue(INVISIBLE) ? itemShape : Shapes.or(COASTER_SHAPE, itemShape);
            }
        }
        return state.getValue(INVISIBLE) ? Shapes.empty() : COASTER_SHAPE;
    }

    private static VoxelShape getDisplayedItemCollisionShape(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof CoasterBlockEntity blockEntity) {
            return blockEntity.getItems().stream()
                    .filter(stack -> !stack.isEmpty())
                    .findFirst()
                    .map(CoasterModelCollisionShapes::get)
                    .orElse(Shapes.empty());
        }
        return Shapes.empty();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CoasterBlockEntity coasterBlockEntity) {
            return coasterBlockEntity.useItemOn(stack, level, state, pos, player, hand);
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CoasterBlockEntity coasterBlockEntity) {
            return coasterBlockEntity.useWithoutItem(state, level, pos, player, hitResult);
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation() + 180.0F));
    }

    @Override
    protected BlockState updateShape(BlockState stateIn, LevelReader worldIn, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        return facing == Direction.DOWN && !stateIn.canSurvive(worldIn, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(stateIn, worldIn, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        return canSupportRigidBlock(level, floorPos) || canSupportCenter(level, floorPos, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ROTATION, SIZE, INVISIBLE);
    }

    @Override
    protected BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(ROTATION, pRot.rotate(pState.getValue(ROTATION), 16));
    }

    @Override
    protected BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.setValue(ROTATION, pMirror.mirror(pState.getValue(ROTATION), 16));
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new java.util.ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CoasterBlockEntity be) {
            be.getItems().stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .forEach(drops::add);
        }
        return drops;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        level.updateNeighbourForOutputSignal(pos, this);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
        return Math.min(blockState.getValue(SIZE) * 4, 15);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        if (level.getBlockEntity(pos) instanceof CoasterBlockEntity blockEntity && blockEntity.getItems().stream().anyMatch(stack -> !stack.isEmpty())) {
            List<ItemStack> stacks = blockEntity.getItems().stream().filter(stack -> !stack.isEmpty()).toList();
            return stacks.getLast();
        }
        return super.getCloneItemStack(level, pos, state, includeData);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CoasterBlockEntity(pPos, pState);
    }
}
