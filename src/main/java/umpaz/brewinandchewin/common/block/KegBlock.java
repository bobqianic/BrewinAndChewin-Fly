package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.utility.BnCMathUtils;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class KegBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<KegBlock> CODEC = simpleCodec(KegBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HAS_LIQUID = BooleanProperty.create("has_liquid");


    protected static final VoxelShape SHAPE_X = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D);
    protected static final VoxelShape SHAPE_Z = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D);
    protected static final VoxelShape SHAPE_VERTICAL = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public KegBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(VERTICAL, false).setValue(WATERLOGGED, false).setValue(HAS_LIQUID, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KegBlockEntity kegBE) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            ItemStack handStack = player.getItemInHand(hand);
            List<ItemStack> itms = handStack.isEmpty() ? List.of() : kegBE.extractInWorld(handStack, 1, player.getAbilities().instabuild);
            if (!itms.isEmpty()) {
                applyExtractedItems(player, hand, itms);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return InteractionResult.PASS;
    }

    public static void applyExtractedItems(Player player, InteractionHand hand, List<ItemStack> items) {
        for (ItemStack item : items) {
            ItemStack handStack = player.getItemInHand(hand);
            if (item.isEmpty() || ItemStack.isSameItemSameComponents(item, handStack)) {
                continue;
            }
            if (handStack.isEmpty()) {
                player.setItemInHand(hand, item);
            } else if (!player.getInventory().add(item)) {
                player.drop(item, false);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KegBlockEntity kegBE) {
            if (!level.isClientSide()) {
                kegBE.updateTemperatureTarget();
                BrewinAndChewin.getHelper().openKegMenu(player, kegBE, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(VERTICAL)) {
            return SHAPE_VERTICAL;
        }
        if ((state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH)) {
            return SHAPE_X;
        }
        return SHAPE_Z;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        FluidState fluid = level.getFluidState(context.getClickedPos());
        boolean hasLiquid = !KegBlockEntity.getMealFromItem(context.getItemInHand(), level.registryAccess()).isEmpty();

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(VERTICAL, context.getNearestLookingVerticalDirection() == Direction.UP || context.getNearestLookingDirection() == Direction.DOWN)
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER)
                .setValue(HAS_LIQUID, hasLiquid);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof KegBlockEntity keg) || isTouchingBurningSource(level, pos, state)) {
            return;
        }

        int temperature = keg.getTemperature();
        if (temperature == 5 && state.getValue(HAS_LIQUID)) {
            spawnHotTemperatureSteamParticles(state, level, pos, random);
        } else if (temperature == 1) {
            spawnColdTemperatureFogParticles(state, level, pos, random);
        }
    }

    private void spawnHotTemperatureSteamParticles(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(35) != 0) {
            return;
        }
        BlockPos particlePos = getRandomKegPosition(state, pos, random);
        double x = particlePos.getX() + 0.5D + (random.nextDouble() * 0.5D - 0.25D);
        double y = particlePos.getY() + 0.85D + random.nextDouble() * 0.45D;
        double z = particlePos.getZ() + 0.5D + (random.nextDouble() * 0.5D - 0.25D);
        level.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0D, 0.005D, 0.0D);
    }

    private void spawnColdTemperatureFogParticles(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) {
            return;
        }
        BlockPos particlePos = getRandomKegPosition(state, pos, random);
        Supplier<Vec3> supplier = () -> new Vec3(Mth.nextDouble(random, -0.005D, 0.005D), Mth.nextDouble(random, -0.025D, -0.01D), Mth.nextDouble(random, -0.005D, 0.005D));
        for (Direction direction : Direction.values()) {
            ParticleUtils.spawnParticlesOnBlockFace(level, particlePos, BnCParticleTypes.FOG, UniformInt.of(1, 1), direction, supplier, direction == Direction.UP ? 0.25D : 0.55D);
        }
    }

    private boolean isTouchingBurningSource(Level level, BlockPos pos, BlockState state) {
        for (BlockPos offset : getKegFootprintOffsets(state)) {
            BlockPos kegPos = pos.offset(offset);
            for (Direction direction : Direction.values()) {
                BlockState neighborState = level.getBlockState(kegPos.relative(direction));
                if (neighborState.is(Blocks.FIRE) || neighborState.is(Blocks.SOUL_FIRE) || neighborState.getFluidState().is(FluidTags.LAVA)) {
                    return true;
                }
            }
        }
        return false;
    }

    private BlockPos getRandomKegPosition(BlockState state, BlockPos pos, RandomSource random) {
        Set<BlockPos> offsets = getKegFootprintOffsets(state);
        int index = random.nextInt(offsets.size());
        for (BlockPos offset : offsets) {
            if (index-- == 0) {
                return pos.offset(offset);
            }
        }
        return pos;
    }

    private Set<BlockPos> getKegFootprintOffsets(BlockState state) {
        return state.is(BnCBlocks.LARGE_KEG) ? LargeKegBlock.getFootprintOffsets(state) : Set.of(BlockPos.ZERO);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);
        Optional<KegBlockEntity> kegBE = level.getBlockEntity(pos, BnCBlockEntityTypes.KEG);
        kegBE.ifPresent(blockEntity -> stack.applyComponents(blockEntity.collectComponents()));
        return stack;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KegBlockEntity) {
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, VERTICAL, WATERLOGGED, HAS_LIQUID);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos, Direction direction) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KegBlockEntity) {
            AbstractedItemHandler inventory = ((KegBlockEntity) tileEntity).getInventory();
            return BnCMathUtils.redstoneFromItemHandler(inventory);
        }
        return 0;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(BlockState state) {
        return state.is(this);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BnCBlockEntityTypes.KEG.create(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntity) {
        if (level.isClientSide())
            return null;
        return createTickerHelper(blockEntity, BnCBlockEntityTypes.KEG, KegBlockEntity::fermentingTick);
    }
}
