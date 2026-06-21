package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.GhostSlots;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.registry.BnCItems;

import java.util.List;

public class KegRecipeBookComponent extends RecipeBookComponent<KegMenu> {
    private static final WidgetSprites FILTER_SPRITES = new WidgetSprites(
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_enabled_highlighted"),
            BrewinAndChewin.asResource("recipe_book/keg_filter_disabled_highlighted")
    );
    private static final Component FILTER_NAME = Component.translatable("brewinandchewin.container.recipe_book.fermentable");

    private final KegMenu kegMenu;
    @Nullable
    private KegFermentingRecipe lastRecipe;

    public KegRecipeBookComponent(KegMenu menu) {
        super(menu, List.of(
                new RecipeBookComponent.TabInfo(SearchRecipeBookCategory.CRAFTING),
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
    }

    @Override
    protected boolean isCraftingSlot(Slot slot) {
        return slot.index >= 0 && slot.index < this.kegMenu.getGridWidth() * this.kegMenu.getGridHeight();
    }

    @Override
    protected void fillGhostRecipe(GhostSlots ghostSlots, RecipeDisplay recipeDisplay, ContextMap contextMap) {
        this.lastRecipe = null;
        // GhostSlots no longer exposes setters outside its package in 1.21.10.
        // Keep the recipe-book interaction alive; result ghost rendering can be restored with an accessor later.
    }

    @Override
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    public void hide() {
        this.setVisible(false);
    }

    @Override
    public void renderTooltip(GuiGraphics gui, int mouseX, int mouseY, @Nullable Slot slot) {
        super.renderTooltip(gui, mouseX, mouseY, slot);
    }

    @Override
    public void renderGhostRecipe(GuiGraphics guiGraphics, boolean isBiggerResultSlot) {
        super.renderGhostRecipe(guiGraphics, isBiggerResultSlot);
    }

    @Nullable
    public KegFermentingRecipe getGhostRecipe() {
        return this.lastRecipe;
    }
}
