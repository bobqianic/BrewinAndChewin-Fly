package umpaz.brewinandchewin.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCItems;

import java.util.List;

public class LargeKegFootprintBlock extends Block implements WorldlyContainerHolder {
    public static final MapCodec<LargeKegFootprintBlock> CODEC = simpleCodec(LargeKegFootprintBlock::new);
    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 2);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 1);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 2);
    private static final int[] INPUT_SLOTS = {0, 1, 2, 3};
    private static final int[] OUTPUT_SLOTS = {KegBlockEntity.CONTAINER_SLOT, KegBlockEntity.OUTPUT_SLOT};
    private static final int[] NO_SLOTS = {};

    public LargeKegFootprintBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OFFSET_X, 1)
                .setValue(OFFSET_Y, 0)
                .setValue(OFFSET_Z, 1));
    }

    @Override
    protected MapCodec<LargeKegFootprintBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockState kegState = level.getBlockState(kegPos);
        if (!kegState.is(BnCBlocks.LARGE_KEG)) {
            return Shapes.empty();
        }
        BlockPos offset = getOffset(state);
        return LargeKegBlock.getOutlineShapeForState(kegState).move(-offset.getX(), -offset.getY(), -offset.getZ());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockState kegState = level.getBlockState(kegPos);
        return kegState.is(BnCBlocks.LARGE_KEG) ? LargeKegBlock.getFootprintShape(kegState, getOffset(state)) : Shapes.empty();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockEntity blockEntity = level.getBlockEntity(kegPos);
        if (blockEntity instanceof KegBlockEntity kegBlockEntity) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            ItemStack handStack = player.getItemInHand(hand);
            List<ItemStack> items = handStack.isEmpty() ? List.of() : kegBlockEntity.extractInWorld(handStack, 1, player.getAbilities().instabuild);
            if (!items.isEmpty()) {
                KegBlock.applyExtractedItems(player, hand, items);
                level.playSound(null, kegPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1, 1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockEntity blockEntity = level.getBlockEntity(kegPos);
        if (blockEntity instanceof KegBlockEntity kegBlockEntity) {
            if (!level.isClientSide()) {
                kegBlockEntity.updateTemperatureTarget();
                BrewinAndChewin.getHelper().openKegMenu(player, kegBlockEntity, kegPos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockEntity blockEntity = level.getBlockEntity(kegPos);
        return blockEntity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Nullable
    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(getKegPos(pos, state));
        return blockEntity instanceof KegBlockEntity kegBlockEntity ? new FootprintContainer(kegBlockEntity) : null;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockState kegState = level.getBlockState(kegPos);
        return kegState.is(BnCBlocks.LARGE_KEG) ? kegState.getAnalogOutputSignal(level, kegPos, direction) : 0;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockState kegState = level.getBlockState(kegPos);
        return kegState.is(BnCBlocks.LARGE_KEG) ? kegState.getCloneItemStack(level, kegPos, includeData) : new ItemStack(BnCItems.LARGE_KEG);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos kegPos = getKegPos(pos, state);
        BlockState kegState = level.getBlockState(kegPos);
        if (kegState.is(BnCBlocks.LARGE_KEG)) {
            level.destroyBlock(kegPos, !player.getAbilities().instabuild, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.getLevel().getBlockState(getKegPos(context.getClickedPos(), state)).is(BnCBlocks.LARGE_KEG);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    public static BlockState stateForOffset(BlockPos offset) {
        return BnCBlocks.LARGE_KEG_FOOTPRINT.defaultBlockState()
                .setValue(OFFSET_X, offset.getX() + 1)
                .setValue(OFFSET_Y, offset.getY())
                .setValue(OFFSET_Z, offset.getZ() + 1);
    }

    public static BlockPos getOffset(BlockState state) {
        return new BlockPos(state.getValue(OFFSET_X) - 1, state.getValue(OFFSET_Y), state.getValue(OFFSET_Z) - 1);
    }

    public static BlockPos getKegPos(BlockPos pos, BlockState state) {
        return pos.subtract(getOffset(state));
    }

    private record FootprintContainer(KegBlockEntity kegBlockEntity) implements WorldlyContainer {
        private AbstractedItemHandler inventory() {
            return kegBlockEntity.getInventory();
        }

        @Override
        public int getContainerSize() {
            return KegBlockEntity.INVENTORY_SIZE;
        }

        @Override
        public boolean isEmpty() {
            AbstractedItemHandler inventory = inventory();
            for (int slot = 0; slot < getContainerSize(); ++slot) {
                if (!inventory.getStackInSlot(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return inventory().getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return inventory().extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = getItem(slot);
            inventory().setStackInSlot(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            inventory().setStackInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
            kegBlockEntity.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(kegBlockEntity, player);
        }

        @Override
        public void clearContent() {
            AbstractedItemHandler inventory = inventory();
            for (int slot = 0; slot < getContainerSize(); ++slot) {
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            if (direction == Direction.UP) {
                return INPUT_SLOTS;
            }
            return direction == null ? NO_SLOTS : OUTPUT_SLOTS;
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
            if (direction == Direction.UP) {
                return slot < KegBlockEntity.CONTAINER_SLOT && inventory().isItemValid(slot, stack);
            }
            return direction != null && slot == KegBlockEntity.CONTAINER_SLOT && inventory().isItemValid(slot, stack);
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return direction != Direction.UP && slot == KegBlockEntity.OUTPUT_SLOT;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return inventory().isItemValid(slot, stack);
        }
    }
}
