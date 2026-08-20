package umpaz.brewinandchewin.fabric.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.recipebook.BnCSearchRecipeBookCategory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ClientRecipeBook.class)
public abstract class ClientRecipeBookMixin {
    @Shadow
    private Map<ExtendedRecipeBookCategory, List<RecipeCollection>> collectionsByTab;

    @Inject(method = "rebuildCollections", at = @At("TAIL"))
    private void brewinandchewin$addFermentingSearchCategory(CallbackInfo ci) {
        List<RecipeCollection> fermentingRecipes = Stream.of(
                        BnCRecipeBookCategories.FERMENTING_DRINKS,
                        BnCRecipeBookCategories.FERMENTING_MEALS
                )
                .flatMap(category -> this.collectionsByTab.getOrDefault(category, List.of()).stream())
                .toList();

        Map<ExtendedRecipeBookCategory, List<RecipeCollection>> categories = new HashMap<>(this.collectionsByTab);
        categories.put(BnCSearchRecipeBookCategory.FERMENTING, fermentingRecipes);
        this.collectionsByTab = Map.copyOf(categories);
    }
}
