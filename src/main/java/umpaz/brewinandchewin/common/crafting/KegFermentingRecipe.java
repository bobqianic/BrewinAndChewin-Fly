package umpaz.brewinandchewin.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.client.recipebook.BnCRecipeBookCategories;
import umpaz.brewinandchewin.client.recipebook.FermentingBookCategory;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.BnCRecipeUtils;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KegFermentingRecipe implements Recipe<KegRecipeWrapper> {
    public static final int INPUT_SLOTS = 4;

    private final NonNullList<Ingredient> inputItems;

    private final Optional<FluidIngredientWithAmount> fluidIngredient;
    private final Optional<FluidUnit> unit;

    private final FermentingBookCategory tab;

    private final Either<AbstractedFluidStack, ItemStackTemplate> result;

    private final float experience;
    private final int fermentTime;
    private final int temperature;

    public KegFermentingRecipe(NonNullList<Ingredient> inputItems, FermentingBookCategory tab, Optional<FluidIngredientWithAmount> fluidIngredient, Optional<FluidUnit> unit, Either<AbstractedFluidStack, ItemStackTemplate> result, float experience, int fermentTime, int temperature) {
        this.inputItems = inputItems;
        this.tab = tab;
        this.fluidIngredient = fluidIngredient;
        this.unit = unit;
        if (unit.isPresent() && result.left().isPresent())
            this.result = Either.left(new AbstractedFluidStack(result.left().get().fluid(), result.left().get().amount(), result.left().get().components(), unit.get(), result.left().get().loaderSpecific()));
        else
            this.result = result;
        this.experience = experience;
        this.fermentTime = fermentTime;
        this.temperature = temperature;
    }

    public FermentingBookCategory getRecipeBookCategory() {
        return this.tab;
    }

    public NonNullList<Ingredient> getIngredients() {
        return this.inputItems;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(this.inputItems.stream().filter(ingredient -> !ingredient.isEmpty()).toList());
    }

    public Optional<FluidIngredientWithAmount> getFluidIngredient() {
        return fluidIngredient;
    }

    public Optional<FluidUnit> getRawUnit() {
        return unit;
    }

    public FluidUnit getUnit() {
        return unit.orElse(FluidUnit.getLoaderUnit());
    }

    public Either<AbstractedFluidStack, ItemStack> getResult() {
        if (result.left().isPresent())
            return Either.left(result.left().get());
        return Either.right(result.right().orElseThrow().create());
    }

    public Either<AbstractedFluidStack, ItemStackTemplate> getResultTemplate() {
        return result;
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    public float getExperience() {
        return this.experience;
    }

    public int getFermentTime() {
        return this.fermentTime;
    }

    public int getTemperature() {
        return this.temperature;
    }

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        List<ItemStack> inputs = new ArrayList<>();

        for (int j = 0; j < INPUT_SLOTS; ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!this.acceptsIngredient(itemstack)) {
                return false;
            }
            if (!itemstack.isEmpty()) {
                inputs.add(itemstack);
            } else
                inputs.add(ItemStack.EMPTY);
        }
        CraftingInput input = CraftingInput.of(2, 2, inputs);
        return input.size() == 1 && inputItems.size() == 1 ? inputItems.getFirst().test(input.getItem(0)) : input.stackedContents().canCraft(this, null) &&
                 (fluidIngredient.isEmpty() && inv.getFluid().isEmpty() || fluidIngredient.isPresent() && !inv.getFluid().isEmpty() && fluidIngredient.get().ingredient().matches(inv.getFluid()) && inv.getFluid().amount() % fluidIngredient.get().amount() == 0);
    }

    public boolean acceptsIngredient(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        for (Ingredient ingredient : this.inputItems) {
            if (!ingredient.isEmpty() && ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        if (result.right().isPresent())
            return result.right().get().create();
        if (result.left().isPresent()) {
            BnCConfiguration.Common.Keg kegConfig = BnCConfiguration.COMMON_CONFIG.get().keg();
            return BnCRecipeUtils.getPouredItemFromFluid(new AbstractedFluidStack(result.left().get().fluid(), kegConfig.capacity(), result.left().get().components(), kegConfig.capacityUnit(), null));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                this.inputItems.stream()
                        .filter(ingredient -> !ingredient.isEmpty())
                        .map(Ingredient::display)
                        .toList(),
                new SlotDisplay.ItemStackSlotDisplay(getRecipeBookResult()),
                new SlotDisplay.ItemSlotDisplay(BnCItems.KEG)
        ));
    }

    private ItemStackTemplate getRecipeBookResult() {
        return this.result.map(fluid -> {
            var fluidId = BuiltInRegistries.FLUID.getKey(fluid.fluid());
            Item displayItem = BuiltInRegistries.ITEM.containsKey(fluidId)
                    ? BuiltInRegistries.ITEM.getValue(fluidId)
                    : fluid.fluid().getBucket();
            if (displayItem == null || displayItem == Items.AIR) {
                displayItem = fluid.fluid().getBucket();
            }
            if (displayItem == Items.AIR) {
                displayItem = BnCItems.TANKARD;
            }
            return new ItemStackTemplate(displayItem);
        }, template -> template);
    }

    @Override
    public RecipeSerializer<KegFermentingRecipe> getSerializer() {
        return BnCRecipeSerializers.FERMENTING;
    }

    @Override
    public RecipeType<KegFermentingRecipe> getType() {
        return BnCRecipeTypes.FERMENTING;
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG);
    }

    public boolean isIncomplete() {
        NonNullList<Ingredient> nonnulllist = getIngredients();
        return nonnulllist.isEmpty() || nonnulllist.stream().allMatch(Ingredient::isEmpty);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (this.tab) {
            case MEALS -> BnCRecipeBookCategories.FERMENTING_MEALS;
            case DRINKS -> BnCRecipeBookCategories.FERMENTING_DRINKS;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegFermentingRecipe that = (KegFermentingRecipe) o;

        if (Float.compare(that.getExperience(), getExperience()) != 0) return false;
        if (getFermentTime() != that.getFermentTime()) return false;
        if (getTemperature() != that.getTemperature()) return false;
        if (result != that.result) return false;
        if (getFluidIngredient() != (that.getFluidIngredient())) return false;

        return inputItems.equals(that.inputItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputItems, fluidIngredient, result, experience, fermentTime, temperature);
    }

    private static NonNullList<Ingredient> toNonNullList(List<Ingredient> ingredients) {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    public static class Serializer {
        public static final MapCodec<KegFermentingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC.listOf(1, 4).xmap(KegFermentingRecipe::toNonNullList, List::copyOf).fieldOf("ingredients").forGetter(KegFermentingRecipe::getIngredients),
                FermentingBookCategory.CODEC.optionalFieldOf("category", FermentingBookCategory.DRINKS).forGetter(KegFermentingRecipe::getRecipeBookCategory),
                FluidIngredientWithAmount.CODEC.optionalFieldOf("base_fluid").forGetter(KegFermentingRecipe::getFluidIngredient),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegFermentingRecipe::getRawUnit),
                Codec.either(AbstractedFluidStack.CODEC, ItemStackTemplate.CODEC).fieldOf("result").forGetter(KegFermentingRecipe::getResultTemplate),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(KegFermentingRecipe::getExperience),
                Codec.INT.optionalFieldOf("fermenting_time", 9600).forGetter(KegFermentingRecipe::getFermentTime),
                Codec.INT.optionalFieldOf("temperature", 3).forGetter(KegFermentingRecipe::getTemperature)
        ).apply(inst, KegFermentingRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, KegFermentingRecipe> STREAM_CODEC = StreamCodec.of(KegFermentingRecipe.Serializer::toNetwork, KegFermentingRecipe.Serializer::fromNetwork);

        public static void toNetwork(RegistryFriendlyByteBuf buf, KegFermentingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(4)).encode(buf, recipe.getIngredients());
            FermentingBookCategory.STREAM_CODEC.encode(buf, recipe.getRecipeBookCategory());
            ByteBufCodecs.optional(FluidIngredientWithAmount.STREAM_CODEC).encode(buf, recipe.getFluidIngredient());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.either(AbstractedFluidStack.STREAM_CODEC, ItemStackTemplate.STREAM_CODEC).encode(buf, recipe.getResultTemplate());
            buf.writeFloat(recipe.getExperience());
            buf.writeInt(recipe.getFermentTime());
            buf.writeInt(recipe.getTemperature());
        }

        public static KegFermentingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            NonNullList<Ingredient> ingredients = toNonNullList(Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(4)).decode(buf));
            FermentingBookCategory category = FermentingBookCategory.STREAM_CODEC.decode(buf);
            Optional<FluidIngredientWithAmount> fluidIngredient = ByteBufCodecs.optional(FluidIngredientWithAmount.STREAM_CODEC).decode(buf);
            Optional<FluidUnit> fluidUnit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            Either<AbstractedFluidStack, ItemStackTemplate> result = ByteBufCodecs.either(AbstractedFluidStack.STREAM_CODEC, ItemStackTemplate.STREAM_CODEC).decode(buf);
            float experience = buf.readFloat();
            int fermentingTime = buf.readInt();
            int temperature = buf.readInt();

            return new KegFermentingRecipe(ingredients, category, fluidIngredient, fluidUnit, result, experience, fermentingTime, temperature);
        }
    }
}
