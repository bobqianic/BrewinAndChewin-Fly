package umpaz.brewinandchewin.common.block.entity.container;

import net.minecraft.core.BlockPos;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCBlocks;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.Objects;

public class KegMenu extends RecipeBookMenu
{
    public static final Identifier EMPTY_CONTAINER_SLOT_TANKARD = BrewinAndChewin.asResource("container/slot/empty_container_slot_tankard");

    public final KegBlockEntity blockEntity;
    public final AbstractedItemHandler inventory;
    public final AbstractedFluidTank kegTank;
    private final ContainerData kegData;
    private final ContainerLevelAccess canInteractWithCallable;
    protected final Level level;
    private final KegRecipeWrapper recipeWrapper;

    public KegMenu(final int windowId, final Inventory playerInventory, final BlockPos data) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data), new SimpleContainerData(7));
    }

    public KegMenu(final int windowId, final Inventory playerInventory, final KegBlockEntity blockEntity, ContainerData kegDataIn) {
        super(BnCMenuTypes.KEG, windowId);
        this.blockEntity = blockEntity;
        this.inventory = blockEntity.getInventory();
        this.kegTank = blockEntity.getFluidTank();
        this.kegData = kegDataIn;
        this.level = playerInventory.player.level();
        this.canInteractWithCallable = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.recipeWrapper = BrewinAndChewin.getHelper().createRecipeWrapper(inventory, kegTank);

        // Ingredient Slots - 2 Rows x 2 Columns
        int startX = 8;
        int startY = 18;
        int inputStartX = 39;
        int inputStartY = 17;
        int borderSlotSize = 18;
        for (int row = 0; row < 2; ++row) {
            for (int column = 0; column < 2; ++column) {
                this.addSlot(BrewinAndChewin.getHelper().createKegSlot(inventory, (row * 2) + column,
                        inputStartX + (column * borderSlotSize),
                        inputStartY + (row * borderSlotSize)));
            }
        }


        // Tankard Input
        this.addSlot(BrewinAndChewin.getHelper().createKegContainerSlot(inventory, 4, 91, 55));

        // Tankard Output
        this.addSlot(BrewinAndChewin.getHelper().createKegResultSlot(inventory, 5, 124, 55));


        // Main Player Inventory
        int startPlayerInvY = startY * 4 + 12;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, startX + (column * borderSlotSize),
                        startPlayerInvY + (row * borderSlotSize)));
            }
        }

        // Hotbar
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(playerInventory, column, startX + (column * borderSlotSize), 142));
        }

        this.addDataSlots(kegDataIn);
    }

    private static KegBlockEntity getTileEntity(final Inventory playerInventory, final BlockPos data) {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        final BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data);
        if (tileAtPos instanceof KegBlockEntity) {
            return (KegBlockEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(canInteractWithCallable, playerIn, BnCBlocks.KEG)
                || stillValid(canInteractWithCallable, playerIn, BnCBlocks.LARGE_KEG);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        int indexContainerInput = 4;
        int indexOutput = 5;
        int startPlayerInv = indexOutput + 1;
        int endPlayerInv = startPlayerInv + 36;

        Slot slot = this.slots.get(index);
        ItemStack slotStackCopy = ItemStack.EMPTY;
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            slotStackCopy = slotStack.copy();
            if (index == indexOutput) {
                if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index > indexOutput) {
                boolean isValidContainer = slotStack.is(blockEntity.getInventory().getStackInSlot(indexContainerInput).getItem()) ||
                        KegBlockEntity.isDirectKegFluidContainer(slotStack) ||
                        !this.level.isClientSide() && blockEntity.getPouringRecipe(slotStack).isPresent();
                if (isValidContainer && !this.moveItemStackTo(slotStack, indexContainerInput, indexContainerInput + 1, false)) {
                    return ItemStack.EMPTY;
                }
                else if ( !this.moveItemStackTo(slotStack, 0, indexContainerInput, false) ) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, startPlayerInv, endPlayerInv, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == slotStackCopy.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, slotStack);
        }


        return slotStackCopy;

    }

    public int getFermentProgressionScaled() {
        int i = this.kegData.get(0);
        int j = this.kegData.get(1);
        return j != 0 && i != 0 ? i * 22 / j : 0;
    }

    public float getProgression() {
        return this.kegData.get(0);
    }

    public int getKegTemperature() {
        return this.kegData.get(2);
    }

    public double getRawKegTemperature() {
        return this.kegData.get(3) / (double) KegBlockEntity.TEMPERATURE_SCALE;
    }

    public double getTargetRawKegTemperature() {
        return this.kegData.get(4) / (double) KegBlockEntity.TEMPERATURE_SCALE;
    }

    public int getTemperatureProgress() {
        return this.kegData.get(5);
    }

    public int getTemperatureProgressScale() {
        return this.kegData.get(6);
    }

    public int getFermentationScale() {
        return this.blockEntity.getFermentationScale();
    }

    public boolean isFermenting() {
        if (level.isClientSide()) {
            return getProgression() > 0;
        }
        var recipe = blockEntity.getRecipeWithoutTemperature();
        return recipe.isPresent() && KegBlockEntity.isValidTemp(getKegTemperature(), recipe.get().value().getTemperature());
    }

    @Override
    public RecipeBookMenu.PostPlaceAction handlePlacement(boolean placeAll, boolean isCreative, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInventory) {
        RecipeHolder<KegFermentingRecipe> recipeHolder = (RecipeHolder)recipe;
        this.returnRejectedIngredients(playerInventory, recipeHolder.value());
        int scale = this.getFermentationScale();
        if (scale > 1 && !this.hasAvailableIngredients(playerInventory, recipeHolder, scale)) {
            return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
        }

        ServerPlaceRecipe.CraftingMenuAccess<KegFermentingRecipe> placementAccess = new ServerPlaceRecipe.CraftingMenuAccess<>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
                KegMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
            }

            @Override
            public void clearCraftingContent() {
                KegMenu.this.clearCraftingContent();
            }

            @Override
            public boolean recipeMatches(RecipeHolder<KegFermentingRecipe> recipe) {
                return scale > 1
                        ? KegMenu.this.hasIngredientsInGrid(recipe, 1)
                        : KegMenu.this.recipeMatches(recipe);
            }
        };

        KegPlaceRecipe.handleFluidPlacement(this, level.recipeAccess(), playerInventory, recipeHolder, placeAll);
        RecipeBookMenu.PostPlaceAction action;
        if (scale <= 1 || placeAll) {
            action = this.placeRecipeItems(placementAccess, playerInventory, recipeHolder, placeAll, isCreative);
        } else {
            action = RecipeBookMenu.PostPlaceAction.NOTHING;
            for (int attempt = 0; attempt < scale && !this.hasIngredientsInGrid(recipeHolder, scale); ++attempt) {
                action = this.placeRecipeItems(placementAccess, playerInventory, recipeHolder, false, isCreative);
                if (action == RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE) {
                    break;
                }
            }
        }

        if (!this.hasIngredientsInGrid(recipeHolder, scale) || !this.hasRequiredFluid(recipeHolder.value())) {
            return RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE;
        }
        return action;
    }

    private void returnRejectedIngredients(Inventory playerInventory, KegFermentingRecipe recipe) {
        int gridSize = this.getGridWidth() * this.getGridHeight();
        for (int i = 0; i < gridSize; ++i) {
            if (!recipe.acceptsIngredient(this.getSlot(i).getItem())) {
                this.quickMoveStack(playerInventory.player, i);
            }
        }
    }

    private RecipeBookMenu.PostPlaceAction placeRecipeItems(ServerPlaceRecipe.CraftingMenuAccess<KegFermentingRecipe> placementAccess,
                                                             Inventory playerInventory,
                                                             RecipeHolder<KegFermentingRecipe> recipe,
                                                             boolean placeAll,
                                                             boolean isCreative) {
        int gridSize = this.getGridWidth() * this.getGridHeight();
        return ServerPlaceRecipe.placeRecipe(
                placementAccess,
                this.getGridWidth(),
                this.getGridHeight(),
                this.slots.subList(0, gridSize),
                this.slots.subList(0, gridSize),
                playerInventory,
                recipe,
                placeAll,
                isCreative
        );
    }

    private boolean hasAvailableIngredients(Inventory playerInventory, RecipeHolder<KegFermentingRecipe> recipe, int count) {
        StackedItemContents available = new StackedItemContents();
        playerInventory.fillStackedContents(available);
        this.fillCraftSlotsStackedContents(available);
        return available.canCraft(recipe.value(), count, null);
    }

    private boolean hasIngredientsInGrid(RecipeHolder<KegFermentingRecipe> recipe, int count) {
        for (int i = 0; i < this.getGridWidth() * this.getGridHeight(); ++i) {
            if (!recipe.value().acceptsIngredient(this.inventory.getStackInSlot(i))) {
                return false;
            }
        }
        StackedItemContents contents = new StackedItemContents();
        this.fillCraftSlotsStackedContents(contents);
        return contents.canCraft(recipe.value(), count, null);
    }

    private boolean hasRequiredFluid(KegFermentingRecipe recipe) {
        if (recipe.getFluidIngredient().isEmpty()) {
            return this.kegTank.isEmpty();
        }

        var requiredFluid = recipe.getFluidIngredient().orElseThrow();
        var tankFluid = this.kegTank.getAbstractedFluid();
        return requiredFluid.ingredient().matches(tankFluid)
                && tankFluid.unit().convertToLoader(tankFluid.amount()) >= requiredFluid.loaderAmount() * this.getFermentationScale();
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents helper) {
        for (int i = 0; i < getGridWidth() * getGridHeight(); i++) {
            helper.accountSimpleStack(inventory.getStackInSlot(i));
        }
    }

    public void clearCraftingContent() {
        for (int i = 0; i < 4; i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public boolean recipeMatches(RecipeHolder<KegFermentingRecipe> recipe) {
        return recipe.value().matches(recipeWrapper, level);
    }

    public int getResultSlotIndex() {
        return 5;
    }

    public int getGridWidth() {
        return 2;
    }

    public int getGridHeight() {
        return 2;
    }

    public int getSize() {
        return 6;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return BnCRecipeBookTypes.fermentingOrFallback();
    }

    public boolean shouldMoveToInventory(int slot) {
        return slot < (getGridWidth() * getGridHeight());
    }
}
