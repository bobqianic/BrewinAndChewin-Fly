package umpaz.brewinandchewin.common.block.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.tag.BnCCompatTags;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

import static umpaz.brewinandchewin.common.block.CoasterBlock.INVISIBLE;
import static umpaz.brewinandchewin.common.block.CoasterBlock.SIZE;

public class CoasterBlockEntity extends SyncedBlockEntity {

    private static final int MAX_DISPLAYED_ITEMS = 1;

    // Keep four slots for old saves; new interactions only use slot 0.
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);

    public CoasterBlockEntity(BlockPos pos, BlockState state) {
        super(BnCBlockEntityTypes.COASTER, pos, state);
    }

    public InteractionResult useItemOn(ItemStack stack, Level level, BlockState state, BlockPos pos, Player player, InteractionHand hand) {
        if (!player.getAbilities().mayBuild)
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (state.getValue(CoasterBlock.INVISIBLE) && stack.is(BnCItems.COASTER)) {
            if (!player.getAbilities().instabuild)
                stack.shrink(1);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS);
            level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, false));
            return InteractionResult.SUCCESS;
        }
        if (stack.is(BnCItems.COASTER)) {
            return InteractionResult.PASS;
        }
        if (!canPlaceOnCoaster(stack) || state.getValue(CoasterBlock.SIZE) >= MAX_DISPLAYED_ITEMS) {
            return InteractionResult.PASS;
        }
        if (addItem(level, pos, state, stack, player.getAbilities().instabuild)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild)
            return InteractionResult.PASS;
        int itemIndex = getLastItemIndex();
        if (itemIndex >= 0) { //Pickup Logic
            if (player.isShiftKeyDown() && !state.getValue(INVISIBLE)) {
                ItemStack coaster = new ItemStack(BnCItems.COASTER);
                if (!player.getAbilities().instabuild && !player.addItem(coaster)) {
                    player.drop(coaster, false);
                }
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS);
                level.setBlockAndUpdate(pos, state.setValue(CoasterBlock.INVISIBLE, true));
                return InteractionResult.SUCCESS;
            }
            ItemStack storedStack = inventory.get(itemIndex);
            if (!player.getAbilities().instabuild && !player.addItem(storedStack)) {
                player.drop(storedStack, false);
            }
            BlockState replaceWith = state.getValue(INVISIBLE) ? Blocks.AIR.defaultBlockState() : state.setValue(CoasterBlock.SIZE, 0);
            level.setBlockAndUpdate(pos, replaceWith);
            inventory.set(itemIndex, ItemStack.EMPTY);
            inventoryChanged();

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean addItem(Level level, BlockPos pos, BlockState state, ItemStack stack, boolean instabuild) {
        if (stack.isEmpty())
            return false;
        level.setBlock(pos, state.setValue(SIZE, 1), 3);
        inventory.set(0, stack.copyWithCount(1));
        inventoryChanged();
        if (!instabuild)
            stack.shrink(1);
        return true;
    }

    private static boolean canPlaceOnCoaster(ItemStack stack) {
        return stack.has(DataComponents.FOOD)
                || stack.has(DataComponents.CONSUMABLE)
                || stack.is(BnCCompatTags.FOODS_EDIBLE_WHEN_PLACED);
    }

    private int getLastItemIndex() {
        for (int i = inventory.size() - 1; i >= 0; --i) {
            if (!inventory.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.clear();
        ContainerHelper.loadAllItems(input, inventory);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
    }

    // Implement through method override in renderer.
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return AABB.of(BoundingBox.fromCorners(pos, pos.above()));
    }

    public NonNullList<ItemStack> getItems() {
        return inventory;
    }
}
