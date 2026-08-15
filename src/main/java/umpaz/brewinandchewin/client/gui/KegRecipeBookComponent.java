package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.recipebook.BnCSearchRecipeBookCategory;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeUtils;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.crafting.FluidIngredientWithAmount;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.mixin.client.GhostSlotsInvoker;
import umpaz.brewinandchewin.common.mixin.client.RecipeBookComponentAccessor;
import umpaz.brewinandchewin.common.mixin.client.RecipeCollectionAccessor;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class KegRecipeBookComponent extends RecipeBookComponent<KegMenu> {
    private static final int MISSING_SLOT_OVERLAY_COLOR = 0x30FF0000;
    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled_highlighted"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled_highlighted")
    );
    private static final Component FILTER_NAME = Component.translatable("brewinandchewin.container.recipe_book.fermentable");

    private final KegMenu kegMenu;
    private final List<Slot> occupiedMissingSlots = new ArrayList<>(4);
    private final List<ItemStack> lastInputStacks = new ArrayList<>(4);
    @Nullable
    private KegFermentingRecipe lastRecipe;
    @Nullable
    private RecipeDisplay lastRecipeDisplay;

    public KegRecipeBookComponent(KegMenu menu) {
        super(menu, List.of(
                new RecipeBookComponent.TabInfo(new ItemStack(Items.COMPASS), Optional.empty(), BnCSearchRecipeBookCategory.FERMENTING),
                new RecipeBookComponent.TabInfo(BnCItems.KEG, BnCRecipeBookCategories.FERMENTING_DRINKS),
                new RecipeBookComponent.TabInfo(BnCItems.VEGETABLE_OMELET, BnCRecipeBookCategories.FERMENTING_MEALS)
        ));
        this.kegMenu = menu;
    }

    @Override
    protected void initFilterButtonTextures() {
        this.filterButton.initTextureValues(FILTER_SPRITES);
    }

    @Override
    protected void selectMatchingRecipes(RecipeCollection possibleRecipes, StackedItemContents stackedItemContents) {
        possibleRecipes.selectRecipes(stackedItemContents, recipeDisplay -> true);
        int scale = this.kegMenu.getFermentationScale();
        if (scale <= 1) {
            return;
        }

        var craftable = ((RecipeCollectionAccessor) possibleRecipes).brewinandchewin$getCraftable();
        for (RecipeDisplayEntry entry : possibleRecipes.getRecipes()) {
            KegFermentingRecipe recipe = findRecipe(entry.display());
            if (recipe != null && !stackedItemContents.canCraft(recipe, scale, null)) {
                craftable.remove(entry.id());
            }
        }
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        int menuSlot = this.kegMenu.slots.indexOf(slot);
        return menuSlot >= 0 && menuSlot <= KegBlockEntity.CONTAINER_SLOT;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isVisible() && this.updateInputStackSnapshot()) {
            ((RecipeBookComponentAccessor) this).brewinandchewin$updateStackedContents();
        }
    }

    private boolean updateInputStackSnapshot() {
        int inputSlots = this.kegMenu.getGridWidth() * this.kegMenu.getGridHeight();
        boolean changed = this.lastInputStacks.size() != inputSlots;
        if (!changed) {
            for (int i = 0; i < inputSlots; ++i) {
                if (!ItemStack.matches(this.lastInputStacks.get(i), this.kegMenu.getSlot(i).getItem())) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            this.lastInputStacks.clear();
            for (int i = 0; i < inputSlots; ++i) {
                this.lastInputStacks.add(this.kegMenu.getSlot(i).getItem().copy());
            }
        }
        return changed;
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
        this.lastRecipe = findRecipe(recipeDisplay);
        this.lastRecipeDisplay = this.lastRecipe == null ? null : recipeDisplay;
        this.populateGhostRecipe(ghostSlots, recipeDisplay, contextMap);
    }

    private void populateGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
        this.occupiedMissingSlots.clear();
        GhostSlotsInvoker invoker = (GhostSlotsInvoker) ghostSlots;

        if (this.lastRecipe == null || !(recipeDisplay instanceof ShapelessCraftingRecipeDisplay fermentingDisplay)) {
            return;
        }

        int scale = this.kegMenu.getFermentationScale();
        List<Ingredient> ingredients = this.lastRecipe.getIngredients().stream()
                .filter(ingredient -> !ingredient.isEmpty())
                .toList();
        int inputSlots = this.kegMenu.getGridWidth() * this.kegMenu.getGridHeight();
        int displayedIngredients = Math.min(Math.min(inputSlots, fermentingDisplay.ingredients().size()), ingredients.size());
        List<ItemStack> availableIngredients = new ArrayList<>(inputSlots);
        for (int i = 0; i < inputSlots; ++i) {
            Slot slot = this.kegMenu.getSlot(i);
            availableIngredients.add(slot.getItem().copy());
            if (!this.lastRecipe.acceptsIngredient(slot.getItem())) {
                this.addOccupiedMissingSlot(slot);
            }
        }

        boolean[] usedGhostSlots = new boolean[inputSlots];
        for (int i = 0; i < displayedIngredients; ++i) {
            int missingCount = consumeRequiredIngredient(availableIngredients, ingredients.get(i), scale);
            if (missingCount > 0) {
                this.markInsufficientOccupiedSlots(ingredients.get(i), inputSlots);
                int ghostSlotIndex = this.findAvailableGhostSlot(i, inputSlots, usedGhostSlots);
                if (ghostSlotIndex >= 0) {
                    usedGhostSlots[ghostSlotIndex] = true;
                    setCountedGhost(invoker, this.kegMenu.getSlot(ghostSlotIndex), contextMap,
                            fermentingDisplay.ingredients().get(i), missingCount);
                } else {
                    this.markOccupiedMissingSlots(ingredients.get(i), i, inputSlots);
                }
            }
        }

        this.addRequiredFluidGhost(invoker, contextMap);
    }

    private int findAvailableGhostSlot(int preferredIndex, int inputSlots, boolean[] usedGhostSlots) {
        if (!usedGhostSlots[preferredIndex] && this.kegMenu.getSlot(preferredIndex).getItem().isEmpty()) {
            return preferredIndex;
        }
        for (int i = 0; i < inputSlots; ++i) {
            if (!usedGhostSlots[i] && this.kegMenu.getSlot(i).getItem().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void markOccupiedMissingSlots(Ingredient ingredient, int preferredIndex, int inputSlots) {
        boolean foundMatchingStack = false;
        for (int i = 0; i < inputSlots; ++i) {
            Slot slot = this.kegMenu.getSlot(i);
            if (!slot.getItem().isEmpty() && ingredient.test(slot.getItem())) {
                this.addOccupiedMissingSlot(slot);
                foundMatchingStack = true;
            }
        }
        if (!foundMatchingStack) {
            this.addOccupiedMissingSlot(this.kegMenu.getSlot(preferredIndex));
        }
    }

    private void markInsufficientOccupiedSlots(Ingredient ingredient, int inputSlots) {
        for (int i = 0; i < inputSlots; ++i) {
            Slot slot = this.kegMenu.getSlot(i);
            if (!slot.getItem().isEmpty() && ingredient.test(slot.getItem())) {
                this.addOccupiedMissingSlot(slot);
            }
        }
    }

    private void addOccupiedMissingSlot(Slot slot) {
        if (!slot.getItem().isEmpty() && !this.occupiedMissingSlots.contains(slot)) {
            this.occupiedMissingSlots.add(slot);
        }
    }

    @Nullable
    private static KegFermentingRecipe findRecipe(RecipeDisplay recipeDisplay) {
        return BnCClientRecipeUtils.getRecipes(BnCRecipeTypes.FERMENTING).stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.display().contains(recipeDisplay))
                .findFirst()
                .orElse(null);
    }

    private static int consumeRequiredIngredient(List<ItemStack> availableIngredients, Ingredient ingredient, int requiredCount) {
        int remaining = requiredCount;
        for (ItemStack stack : availableIngredients) {
            if (ingredient.test(stack)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
                if (remaining == 0) {
                    break;
                }
            }
        }
        return remaining;
    }

    private static void setCountedGhost(GhostSlotsInvoker invoker, Slot slot, ContextMap contextMap, SlotDisplay display, int scale) {
        List<ItemStack> scaledDisplays = display.resolveForStacks(contextMap).stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount((int) Math.min(Integer.MAX_VALUE, (long) stack.getCount() * scale)))
                .toList();
        if (!scaledDisplays.isEmpty()) {
            invoker.brewinandchewin$setResult(slot, contextMap, countedDisplay(scaledDisplays));
        }
    }

    private void addRequiredFluidGhost(GhostSlotsInvoker invoker, ContextMap contextMap) {
        Optional<FluidIngredientWithAmount> optionalFluid = this.lastRecipe.getFluidIngredient();
        if (optionalFluid.isEmpty()) {
            return;
        }

        FluidIngredientWithAmount requiredFluid = optionalFluid.orElseThrow();
        long requiredAmount = requiredFluid.loaderAmount() * this.kegMenu.getFermentationScale();
        AbstractedFluidStack tankFluid = this.kegMenu.kegTank.getAbstractedFluid();
        long tankAmount = tankFluid.unit().convertToLoader(tankFluid.amount());
        if (requiredFluid.ingredient().matches(tankFluid)
                && tankAmount >= requiredAmount
                && tankAmount % requiredAmount == 0) {
            return;
        }

        List<ItemStack> containerDisplays = new ArrayList<>();
        if (this.kegMenu.blockEntity.getLevel() != null) {
            for (AbstractedFluidStack fluid : requiredFluid.ingredient().displayStacks()) {
                addRequiredContainerDisplay(
                        containerDisplays,
                        BnCFluidItemDisplays.getFluidItemDisplay(this.kegMenu.blockEntity.getLevel().registryAccess(), fluid),
                        requiredFluid,
                        requiredAmount
                );
            }
        }

        BnCClientRecipeUtils.getPouringRecipes().stream()
                .filter(KegPouringRecipe::canFill)
                .sorted(Comparator.comparingInt(recipe -> recipe.isStrict() ? 0 : 1))
                .forEach(recipe -> {
                    ItemStack output = recipe.getOutput();
                    if (requiredFluid.ingredient().matches(recipe.getFluid(output))) {
                        addRequiredContainerDisplay(containerDisplays, output, requiredFluid, requiredAmount);
                    }
                });

        for (AbstractedFluidStack fluid : requiredFluid.ingredient().displayStacks()) {
            ItemStack bucket = this.kegMenu.blockEntity.getBucketForFluid(fluid);
            addRequiredContainerDisplay(containerDisplays, bucket, requiredFluid, requiredAmount);
        }

        if (!containerDisplays.isEmpty()) {
            Slot containerSlot = this.kegMenu.getSlot(KegBlockEntity.CONTAINER_SLOT);
            if (containerSlot.hasItem()) {
                this.addOccupiedMissingSlot(containerSlot);
                return;
            }
            invoker.brewinandchewin$setResult(containerSlot, contextMap, countedDisplay(containerDisplays));
        }
    }

    private static SlotDisplay countedDisplay(List<ItemStack> stacks) {
        List<SlotDisplay> displays = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            displays.add(new SlotDisplay.ItemStackSlotDisplay(stack.copy()));
        }
        return new SlotDisplay.Composite(displays);
    }

    private static void addRequiredContainerDisplay(List<ItemStack> displays, ItemStack container,
                                                    FluidIngredientWithAmount requiredFluid, long requiredAmount) {
        if (container.isEmpty()
                || displays.stream().anyMatch(existing -> ItemStack.isSameItemSameComponents(existing, container))) {
            return;
        }

        ItemStack display = container.copy();
        display.setCount(getRequiredContainerCount(display, requiredFluid, requiredAmount));
        displays.add(display);
    }

    private static int getRequiredContainerCount(ItemStack container, FluidIngredientWithAmount requiredFluid, long requiredAmount) {
        long containerAmount = getContainerAmount(container, requiredFluid);
        if (containerAmount <= 0) {
            return 1;
        }
        return (int) Math.min(Integer.MAX_VALUE, ((requiredAmount - 1L) / containerAmount) + 1L);
    }

    private static long getContainerAmount(ItemStack container, FluidIngredientWithAmount requiredFluid) {
        long recipeAmount = BnCClientRecipeUtils.getPouringRecipes().stream()
                .sorted(Comparator.comparingInt(recipe -> recipe.isStrict() ? 0 : 1))
                .filter(KegPouringRecipe::canFill)
                .filter(recipe -> recipe.isStrict()
                        ? ItemStack.isSameItemSameComponents(container, recipe.getOutput())
                        : ItemStack.isSameItem(container, recipe.getOutput()))
                .filter(recipe -> requiredFluid.ingredient().matches(recipe.getFluid(container)))
                .mapToLong(KegPouringRecipe::getLoaderAmount)
                .findFirst()
                .orElse(0L);
        if (recipeAmount > 0) {
            return recipeAmount;
        }

        AbstractedFluidTank itemTank = BrewinAndChewin.getHelper().getFluidContainerFromItem(container.copyWithCount(1));
        if (itemTank != null && itemTank.getFluidCapacity() > 0) {
            return itemTank.getFluidCapacity();
        }
        if (container.getItem() instanceof BucketItem) {
            return FluidUnit.MILLIBUCKET.convertToLoader(1000L);
        }

        return 0L;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (handled) {
            this.syncSelectedRecipe();
        }
        return handled;
    }

    private void syncSelectedRecipe() {
        RecipeBookComponentAccessor accessor = (RecipeBookComponentAccessor) this;
        RecipeDisplayId selectedId = accessor.brewinandchewin$getLastRecipe();
        RecipeCollection selectedCollection = accessor.brewinandchewin$getLastRecipeCollection();
        if (selectedId == null || selectedCollection == null) {
            return;
        }

        for (RecipeDisplayEntry entry : selectedCollection.getRecipes()) {
            if (entry.id().equals(selectedId)) {
                KegFermentingRecipe selectedRecipe = findRecipe(entry.display());
                this.lastRecipe = selectedRecipe;
                this.lastRecipeDisplay = selectedRecipe == null ? null : entry.display();
                return;
            }
        }
    }

    @Override
    public void slotClicked(@Nullable Slot slot) {
        super.slotClicked(slot);
        if (slot != null && this.isCraftingSlot(slot)) {
            this.lastRecipe = null;
            this.lastRecipeDisplay = null;
        }
    }

    @Override
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    public void hide() {
        this.setVisible(false);
    }

    public void renderMissingSlotOverlays(GuiGraphics guiGraphics) {
        this.refreshGhostRecipe();
        for (Slot slot : this.occupiedMissingSlots) {
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, MISSING_SLOT_OVERLAY_COLOR);
        }
    }

    private void refreshGhostRecipe() {
        GhostSlots ghostSlots = ((RecipeBookComponentAccessor) this).brewinandchewin$getGhostSlots();
        ghostSlots.clear();
        this.occupiedMissingSlots.clear();
        if (this.lastRecipe == null || this.lastRecipeDisplay == null || this.minecraft.level == null) {
            return;
        }

        this.populateGhostRecipe(ghostSlots, this.lastRecipeDisplay, SlotDisplayContext.fromLevel(this.minecraft.level));
    }

    @Nullable
    public KegFermentingRecipe getGhostRecipe() {
        return this.lastRecipe;
    }
}
