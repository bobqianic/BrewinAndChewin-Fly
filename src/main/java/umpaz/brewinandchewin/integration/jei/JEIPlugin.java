package umpaz.brewinandchewin.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.integration.jei.category.CheeseRecipeCategory;
import umpaz.brewinandchewin.integration.jei.category.FermentingRecipeCategory;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransfer;

import java.util.Optional;

@JeiPlugin
@SuppressWarnings("unused")
public class JEIPlugin implements IModPlugin {
    private static final ResourceLocation ID = BrewinAndChewin.asResource("jei_plugin");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new FermentingRecipeCategory(registry.getJeiHelpers().getGuiHelper(), registry.getJeiHelpers().getPlatformFluidHelper(), registry.getJeiHelpers().getModIdHelper()));
        registry.addRecipeCategories(new CheeseRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        BnCJEIRecipes modRecipes = new BnCJEIRecipes();
        registration.addRecipes(BnCJEIRecipeTypes.FERMENTING, modRecipes.getKegRecipes());
        registration.addRecipes(BnCJEIRecipeTypes.AGING, modRecipes.getCheeseRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(BnCJEIRecipeTypes.FERMENTING, new ItemStack(BnCItems.KEG));
        registration.addCraftingStation(BnCJEIRecipeTypes.FERMENTING, new ItemStack(BnCItems.LARGE_KEG));
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(KegScreen.class, 80, 25, 23, 17, BnCJEIRecipeTypes.FERMENTING);

        Rect2i bounds = new Rect2i(107, 18, 26, 30);

        registration.addGuiContainerHandler(KegScreen.class, new IGuiContainerHandler<>() {
            @Override
            public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(IClickableIngredientFactory builder, KegScreen containerScreen, double mouseX, double mouseY) {
                if (bounds.contains((int) mouseX - containerScreen.getRectangle().left(), (int) mouseY - containerScreen.getRectangle().top())) {
                    AbstractedFluidStack fluidStack = containerScreen.getMenu().kegTank.getAbstractedFluid();
                    if (fluidStack.isEmpty()) {
                        return Optional.empty();
                    }

                    Rect2i screenBounds = new Rect2i(containerScreen.getRectangle().left() + bounds.getX(), containerScreen.getRectangle().top() + bounds.getY(), bounds.getWidth(), bounds.getHeight());
                    return createFluidClickableIngredient(builder, registration.getJeiHelpers().getPlatformFluidHelper(), fluidStack, screenBounds);
                }
                return Optional.empty();
            }
        });
    }

    private static <T> Optional<IClickableIngredient<T>> createFluidClickableIngredient(IClickableIngredientFactory builder, IPlatformFluidHelper<T> fluidHelper, AbstractedFluidStack fluidStack, Rect2i screenBounds) {
        T fluidIngredient = fluidHelper.create(fluidStack.fluid().builtInRegistryHolder(), fluidStack.amount(), fluidStack.componentPatch());
        return builder.createBuilder(fluidHelper.getFluidIngredientType(), fluidIngredient)
                .buildWithArea(screenBounds);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new FermentingTransfer.Handler(registration.getTransferHelper(), registration.getJeiHelpers().getStackHelper(), registration.getJeiHelpers().getPlatformFluidHelper()), BnCJEIRecipeTypes.FERMENTING);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
}
