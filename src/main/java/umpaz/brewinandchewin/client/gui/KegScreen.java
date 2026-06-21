package umpaz.brewinandchewin.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeUtils;
import umpaz.brewinandchewin.client.utility.BnCRectangle;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.utility.BnCTextUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KegScreen extends AbstractContainerScreen<KegMenu> implements RecipeUpdateListener {
    public static final ResourceLocation BACKGROUND_TEXTURE = BrewinAndChewin.asResource("textures/gui/keg.png");
    private static final BnCRectangle PROGRESS_ARROW = new BnCRectangle(80, 25, 0, 18);
    public static final BnCRectangle COLD_BAR = new BnCRectangle(35, 55, 8, 4);
    public static final BnCRectangle CHILLY_BAR = new BnCRectangle(43, 55, 9, 4);
    public static final BnCRectangle WARM_BAR = new BnCRectangle(60, 55, 9, 4);
    public static final BnCRectangle HOT_BAR = new BnCRectangle(69, 55, 8, 4);
    private static final BnCRectangle LEFT_BUBBLE = new BnCRectangle(109, 44, 9, 24);
    private static final BnCRectangle RIGHT_BUBBLE = new BnCRectangle(147, 44, 9, 24);

    private final KegRecipeBookComponent recipeBookComponent = new KegRecipeBookComponent(this.menu);
    private boolean widthTooNarrow;
    private boolean recipeBookEnabled;

    public KegScreen(KegMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.leftPos = 0;
        this.topPos = 0;
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 28;
    }

    @Override
    public void init() {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.titleLabelX = 38;
        this.recipeBookEnabled = BnCConfiguration.COMMON_CONFIG.get().recipeBook().enabled() && BnCRecipeBookTypes.hasFermenting();
        if (this.recipeBookEnabled) {
            this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            this.addRenderableWidget(new ImageButton(this.leftPos + 5, this.height / 2 - 49, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
                this.recipeBookComponent.toggleVisibility();
                this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
                button.setPosition(this.leftPos + 5, this.height / 2 - 49);
            }));
            this.addWidget(this.recipeBookComponent);
            this.setInitialFocus(this.recipeBookComponent);
        } else {
            this.leftPos = (this.width - this.imageWidth) / 2;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.recipeBookEnabled) {
            this.recipeBookComponent.tick();
        }
    }

    @Override
    public void render(GuiGraphics gui, final int mouseX, final int mouseY, float partialTicks) {
        if (this.isRecipeBookVisible() && this.widthTooNarrow) {
            this.renderBackground(gui, mouseX, mouseY, partialTicks);
            this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
        } else {
            super.render(gui, mouseX, mouseY, partialTicks);
            if (this.recipeBookEnabled) {
                this.recipeBookComponent.render(gui, mouseX, mouseY, partialTicks);
                this.recipeBookComponent.renderGhostRecipe(gui, false);
            }
        }
        blit(gui, this.leftPos + 119, this.topPos + 15, 176, 22, 27, 33);
        this.renderTankTooltip(gui, mouseX, mouseY);
        this.renderTemperatureTooltip(gui, mouseX, mouseY);
        this.renderTooltip(gui, mouseX, mouseY);
        if (this.recipeBookEnabled) {
            this.recipeBookComponent.renderTooltip(gui, mouseX, mouseY, this.hoveredSlot);
        }
    }

    private static final Map<Fluid, Component> FLUID_CONTAINER_COMPONENTS = new HashMap<>();

    // Called on /reload.
    public static void clearFluidContainerComponents() {
        FLUID_CONTAINER_COMPONENTS.clear();
    }


    private void renderTankTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        KegFermentingRecipe ghostRecipe = getGhostRecipe();
        if (isHovering(120, 19, 24, 28, mouseX, mouseY) && !menu.kegTank.isEmpty() && (ghostRecipe == null || ghostRecipe.getResult().left().isPresent() && ghostRecipe.getResult().left().get().matches(menu.kegTank.getAbstractedFluid()))) {
            Component containerComponent = (BnCTextUtils.getTranslation("container.keg.served_in", FLUID_CONTAINER_COMPONENTS.computeIfAbsent(menu.kegTank.getAbstractedFluid().fluid(), fluid -> {
                MutableComponent component = MutableComponent.create(PlainTextContents.EMPTY).withStyle(ChatFormatting.GRAY);
                int amountAdded = 0;
                for (KegPouringRecipe recipe : getPouringRecipes().stream()
                        .filter(pouringRecipe -> pouringRecipe.matchesFluid(ItemStack.EMPTY, menu.kegTank.getAbstractedFluid()))
                        .sorted(Comparator.comparing(recipe -> recipe.getContainer().getHoverName().getString())).toList()) {
                    if (amountAdded > 0)
                        component.append(", ");
                    component.append(recipe.getContainer().getHoverName().copy().withStyle(ChatFormatting.GRAY));
                    ++amountAdded;
                }
                return component;
            }))).withStyle(ChatFormatting.GRAY);
            Component component = MutableComponent.create(BrewinAndChewin.getHelper().getFluidDisplayName(this.menu.kegTank.getAbstractedFluid()).getContents())
                    .append((FluidUnit.MILLIBUCKET.shortFormat(" (%s/%s") + ")").formatted(FluidUnit.convert(menu.kegTank.getAbstractedFluid().amount(), FluidUnit.getLoaderUnit(), FluidUnit.MILLIBUCKET), FluidUnit.convert(menu.kegTank.getFluidCapacity(), FluidUnit.getLoaderUnit(), FluidUnit.MILLIBUCKET)));
            List<Component> components = new ArrayList<>(List.of(component, containerComponent));
            if (minecraft.options.advancedItemTooltips) {
                ResourceLocation fluidId = menu.kegTank.getAbstractedFluid().fluid().builtInRegistryHolder().key().location();
                components.add(Component.literal(fluidId.toString()).withStyle(ChatFormatting.DARK_GRAY));
                if (!menu.kegTank.getAbstractedFluid().components().isEmpty()) {
                    components.add(Component.translatable("item.components", menu.kegTank.getAbstractedFluid().components().size()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.setComponentTooltipForNextFrame(this.font, components, mouseX, mouseY);
        }
    }

    private void renderTemperatureTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        KegFermentingRecipe ghostRecipe = getGhostRecipe();
        if (this.isHovering(35, 54, 42, 5, mouseX, mouseY)) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(ghostRecipe == null
                    ? BnCTextUtils.getKegTemperatureName(menu.getKegTemperature())
                    : BnCTextUtils.getTranslation("container.keg.temperature_requirement", BnCTextUtils.getAcceptableKegTemperatures(ghostRecipe.getTemperature())));
            gui.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        gui.drawString(this.font, this.playerInventoryTitle, 8, (this.imageHeight - 96 + 2), 4210752, false);
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        // Render UI background
        if (this.minecraft == null)
            return;

        blit(gui, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Render progress arrow
        int l = this.menu.getFermentProgressionScaled();
        blit(gui, this.leftPos + PROGRESS_ARROW.x(), this.topPos + PROGRESS_ARROW.y(), 176, 4, l + 1, PROGRESS_ARROW.height());


        if (menu.isFermenting()) {
            int bubScale = (int) (((this.menu.getProgression() / 80)) * LEFT_BUBBLE.height()) % (LEFT_BUBBLE.height() + 1);
            // render bubbles
            blit(gui, this.leftPos + LEFT_BUBBLE.x(), this.topPos + LEFT_BUBBLE.y() - bubScale, 176, 79 - bubScale, LEFT_BUBBLE.width(), bubScale + 1);
            blit(gui, this.leftPos + RIGHT_BUBBLE.x(), this.topPos + RIGHT_BUBBLE.y() - bubScale, 186, 79 - bubScale, RIGHT_BUBBLE.width(), bubScale + 1);
        }

        renderTemperatureBars(gui);

        AbstractedFluidStack fluidStack = this.menu.kegTank.getAbstractedFluid();
        KegFermentingRecipe ghostRecipe = getGhostRecipe();
        if (!fluidStack.isEmpty() && (ghostRecipe == null || ghostRecipe.getFluidIngredient().isEmpty() && menu.kegTank.isEmpty() || ghostRecipe.getFluidIngredient().isPresent() && ghostRecipe.getFluidIngredient().get().ingredient().matches(fluidStack))) {
            if (BnCConfiguration.CLIENT_CONFIG.get().renderFluidInKeg())
                BrewinAndChewinClient.getHelper().renderFluidInKeg(fluidStack, gui, leftPos + 120, topPos + 19, 1.0F, menu.kegTank.getFluidCapacity());

            ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), fluidStack).copy();
            Optional<KegPouringRecipe> pouringRecipe = getPouringRecipes().stream().sorted(Comparator.comparing(KegPouringRecipe::isStrict)).filter(kegPouringRecipe -> {
                if (kegPouringRecipe.isStrict())
                    return ItemStack.isSameItemSameComponents(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
                return ItemStack.isSameItem(itemDisplay, kegPouringRecipe.getResultItem(minecraft.level.registryAccess()));
            }).findFirst();
            int pourCount = pouringRecipe.map(kegPouringRecipe -> (int) (Math.min(this.menu.kegTank.getFluidCapacity(), this.menu.kegTank.getAbstractedFluid().amount()) / kegPouringRecipe.getLoaderAmount())).orElse(1);
            itemDisplay.setCount(pourCount);
            if (!itemDisplay.isEmpty()) {
                gui.renderItem(itemDisplay, this.leftPos + 124, this.topPos + 23);
                gui.renderItemDecorations(minecraft.font, itemDisplay, this.leftPos + 124, this.topPos + 23);
            }
        }
    }

    private void renderTemperatureBars(GuiGraphics gui) {
        double temperature = getVisualRawKegTemperature();
        renderTemperatureBar(gui, COLD_BAR, 176, getColdFill(temperature), false);
        renderTemperatureBar(gui, CHILLY_BAR, 184, getChillyFill(temperature), false);
        renderTemperatureBar(gui, WARM_BAR, 201, getWarmFill(temperature), true);
        renderTemperatureBar(gui, HOT_BAR, 210, getHotFill(temperature), true);
    }

    private double getVisualRawKegTemperature() {
        double current = this.menu.getRawKegTemperature();
        double target = this.menu.getTargetRawKegTemperature();
        int scale = this.menu.getTemperatureProgressScale();
        if (Double.compare(current, target) == 0 || scale <= 0) {
            return current;
        }
        double progress = Mth.clamp(this.menu.getTemperatureProgress() / (double) scale, 0.0D, 1.0D);
        double temperatureDifference = target - current;
        double temperatureStep = Math.min(1.0D, Math.abs(temperatureDifference));
        return current + Math.signum(temperatureDifference) * temperatureStep * progress;
    }

    private double getColdFill(double temperature) {
        int cold = -BnCConfiguration.COMMON_CONFIG.get().keg().cold();
        int chilly = -BnCConfiguration.COMMON_CONFIG.get().keg().chilly();
        if (temperature <= cold) {
            return 1.0D;
        }
        if (temperature >= chilly || cold == chilly) {
            return 0.0D;
        }
        return Mth.clamp((chilly - temperature) / (chilly - cold), 0.0D, 1.0D);
    }

    private double getChillyFill(double temperature) {
        int chilly = -BnCConfiguration.COMMON_CONFIG.get().keg().chilly();
        if (temperature <= chilly) {
            return 1.0D;
        }
        if (temperature >= 0) {
            return 0.0D;
        }
        return Mth.clamp(-temperature / -chilly, 0.0D, 1.0D);
    }

    private double getWarmFill(double temperature) {
        int warm = BnCConfiguration.COMMON_CONFIG.get().keg().warm();
        if (temperature >= warm) {
            return 1.0D;
        }
        if (temperature <= 0) {
            return 0.0D;
        }
        return Mth.clamp(temperature / warm, 0.0D, 1.0D);
    }

    private double getHotFill(double temperature) {
        int warm = BnCConfiguration.COMMON_CONFIG.get().keg().warm();
        int hot = BnCConfiguration.COMMON_CONFIG.get().keg().hot();
        if (temperature >= hot) {
            return 1.0D;
        }
        if (temperature <= warm || warm == hot) {
            return 0.0D;
        }
        return Mth.clamp((temperature - warm) / (hot - warm), 0.0D, 1.0D);
    }

    private void renderTemperatureBar(GuiGraphics gui, BnCRectangle bar, int textureX, double fill, boolean fillFromLeft) {
        int width = Mth.clamp((int) Math.round(bar.width() * fill), 0, bar.width());
        if (width <= 0) {
            return;
        }
        int xOffset = fillFromLeft ? 0 : bar.width() - width;
        blit(gui, this.leftPos + bar.x() + xOffset, this.topPos + bar.y(), textureX + xOffset, 0, width, bar.height());
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return (!this.widthTooNarrow || !this.isRecipeBookVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (this.recipeBookEnabled && this.recipeBookComponent.mouseClicked(event, isDoubleClick)) {
            this.setFocused(this.recipeBookComponent);
            return true;
        } else {
            return this.widthTooNarrow && this.isRecipeBookVisible() || super.mouseClicked(event, isDoubleClick);
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int x, int y) {
        boolean flag = mouseX < (double) x || mouseY < (double) y || mouseX >= (double) (x + this.imageWidth) || mouseY >= (double) (y + this.imageHeight);
        return flag && (!this.recipeBookEnabled || this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight));
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        super.slotClicked(slot, slotId, mouseButton, clickType);
        if (this.recipeBookEnabled) {
            this.recipeBookComponent.slotClicked(slot);
        }
    }

    @Override
    public void recipesUpdated() {
        if (this.recipeBookEnabled) {
            recipeBookComponent.recipesUpdated();
        }
    }

    public RecipeBookComponent getRecipeBookComponent() {
        return recipeBookComponent;
    }

    @Override
    public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
        if (this.recipeBookEnabled) {
            this.recipeBookComponent.fillGhostRecipe(recipeDisplay);
        }
    }

    private static void blit(GuiGraphics gui, int x, int y, int u, int v, int width, int height) {
        gui.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, (float) u, (float) v, width, height, 256, 256);
    }

    private static List<KegPouringRecipe> getPouringRecipes() {
        if (Minecraft.getInstance().level == null) {
            return List.of();
        }

        return BnCClientRecipeUtils.getPouringRecipes();
    }

    private boolean isRecipeBookVisible() {
        return this.recipeBookEnabled && this.recipeBookComponent.isVisible();
    }

    private KegFermentingRecipe getGhostRecipe() {
        return this.recipeBookEnabled ? this.recipeBookComponent.getGhostRecipe() : null;
    }
}
