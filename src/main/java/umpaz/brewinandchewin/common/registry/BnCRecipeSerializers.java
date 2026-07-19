package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.crafting.CreatePotionPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegPouringRecipe;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;

public class BnCRecipeSerializers {
    public static final RecipeSerializer<KegFermentingRecipe> FERMENTING = new RecipeSerializer<>(KegFermentingRecipe.Serializer.CODEC, KegFermentingRecipe.Serializer.STREAM_CODEC);
    public static final RecipeSerializer<KegPouringRecipe> KEG_POURING = new RecipeSerializer<>(KegPouringRecipe.Serializer.CODEC, KegPouringRecipe.Serializer.STREAM_CODEC);
    public static final RecipeSerializer<CreatePotionPouringRecipe> CREATE_POTION_POURING = createCreatePotionPouringRecipe();

    public static void registerAll() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("fermenting"), FERMENTING);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("keg_pouring"), KEG_POURING);
        if (CREATE_POTION_POURING != null)
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, BrewinAndChewin.asResource("create_potion_pouring"), CREATE_POTION_POURING);
    }

    private static RecipeSerializer<CreatePotionPouringRecipe> createCreatePotionPouringRecipe() {
        if (BrewinAndChewin.getHelper().isModLoaded("create"))
            return new RecipeSerializer<>(CreatePotionPouringRecipe.Serializer.CODEC, CreatePotionPouringRecipe.Serializer.STREAM_CODEC);
        return null;
    }
}
