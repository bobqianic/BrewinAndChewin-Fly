package umpaz.brewinandchewin.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCItems;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.registry.BnCRecipeTypes;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.Objects;
import java.util.Optional;

public class KegPouringRecipe implements Recipe<KegRecipeWrapper> {
    private final AbstractedFluidStack fluid;
    private final Optional<FluidIngredientWithAmount> fluidIngredient;
    private final Optional<ItemStack> container;
    private final ItemStack output;
    private final Optional<FluidUnit> unit;
    private final boolean strict;
    private final boolean filling;

    public KegPouringRecipe(AbstractedFluidStack fluid, Optional<ItemStack> container, ItemStack output, Optional<FluidUnit> unit, boolean strict, boolean filling) {
        this(PouringFluid.exact(fluid), container, output, unit, strict, filling);
    }

    public KegPouringRecipe(PouringFluid fluid, Optional<ItemStack> container, ItemStack output, Optional<FluidUnit> unit, boolean strict, boolean filling) {
        if (container.isEmpty() && BrewinAndChewin.getHelper().getCraftingRemainingItem(output).isEmpty())
            throw new UnsupportedOperationException("'container' field must be specified as the output item stack doesn't have a crafting remainder item.");
        this.fluid = fluid.stack();
        this.fluidIngredient = fluid.ingredient();
        this.container = container;
        this.output = output;
        this.unit = unit;
        this.strict = strict;
        this.filling = filling;
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredient = NonNullList.create();
        ingredient.add(Ingredient.of(getContainer().getItem()));
        return ingredient;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(Ingredient.of(getContainer().getItem()));
    }

    @Override
    public boolean matches(KegRecipeWrapper inv, Level level) {
        return Ingredient.of(getContainer().getItem()).test(inv.getItem(4));
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    public ItemStack getContainer() {
        return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(output));
    }

    public ItemStack getContainer(ItemStack stack) {
        return this.container.orElse(BrewinAndChewin.getHelper().getCraftingRemainingItem(stack));
    }

    public Optional<FluidUnit> getRawUnit() {
        return unit;
    }

    public FluidUnit getUnit() {
        return unit.orElse(FluidUnit.getLoaderUnit());
    }

    public long getFluidAmount() {
        return fluidIngredient
                .map(FluidIngredientWithAmount::amount)
                .orElseGet(fluid::amount);
    }

    public long getLoaderAmount() {
        return getUnit().convertToLoader(getFluidAmount());
    }

    public Optional<ItemStack> getRawContainer(){
        return this.container;
    }

    public ItemStack getOutput(){
        return this.output;
    }

    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    public AbstractedFluidStack getFluid(ItemStack container) {
        return getDisplayFluid();
    }

    public boolean matchesFluid(ItemStack container, AbstractedFluidStack stack) {
        return fluidIngredient
                .map(ingredient -> ingredient.ingredient().matches(stack))
                .orElseGet(() -> getFluid(container).matches(stack));
    }

    public AbstractedFluidStack getRawFluid() {
        return getDisplayFluid();
    }

    public PouringFluid getSerializedFluid() {
        return new PouringFluid(this.fluid, this.fluidIngredient);
    }

    public boolean hasSpecialFluid() {
        return false;
    }

    private AbstractedFluidStack getDisplayFluid() {
        return fluidIngredient
                .map(KegPouringRecipe::displayStack)
                .orElse(fluid);
    }

    private static AbstractedFluidStack displayStack(FluidIngredientWithAmount ingredient) {
        AbstractedFluidIngredient fluidIngredient = ingredient.ingredient();
        var displayStacks = fluidIngredient.displayStacks();
        if (displayStacks.isEmpty())
            return AbstractedFluidStack.EMPTY;
        AbstractedFluidStack displayStack = displayStacks.getFirst();
        return new AbstractedFluidStack(displayStack.fluid(), ingredient.amount(), displayStack.components(), ingredient.getUnit(), null);
    }

    public boolean isStrict() {
        return strict;
    }

    public boolean canFill() {
        return filling;
    }

    @Override
    public RecipeSerializer<KegPouringRecipe> getSerializer() {
        return BnCRecipeSerializers.KEG_POURING;
    }

    @Override
    public RecipeType<KegPouringRecipe> getType() {
        return BnCRecipeTypes.KEG_POURING;
    }

    public ItemStack getToastSymbol() {
        return new ItemStack(BnCItems.KEG);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, fluidIngredient, container, output, strict, filling);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KegPouringRecipe that = (KegPouringRecipe) o;

        if (!output.equals(that.output)) return false;
        if (!fluid.equals(that.fluid)) return false;
        if (!fluidIngredient.equals(that.fluidIngredient)) return false;
        if (!container.equals(that.container)) return false;
        if (strict != that.strict) return false;
        return filling == that.filling;
    }

    public record PouringFluid(AbstractedFluidStack stack, Optional<FluidIngredientWithAmount> ingredient) {
        public static final Codec<PouringFluid> CODEC = Codec.either(AbstractedFluidStack.CODEC, FluidIngredientWithAmount.CODEC)
                .xmap(either -> either.map(PouringFluid::exact, PouringFluid::ingredient), fluid -> fluid.ingredient()
                        .<Either<AbstractedFluidStack, FluidIngredientWithAmount>>map(Either::right)
                        .orElseGet(() -> Either.left(fluid.stack())));
        public static final StreamCodec<RegistryFriendlyByteBuf, PouringFluid> STREAM_CODEC = StreamCodec.of(PouringFluid::toNetwork, PouringFluid::fromNetwork);

        public static PouringFluid exact(AbstractedFluidStack stack) {
            return new PouringFluid(stack, Optional.empty());
        }

        public static PouringFluid ingredient(FluidIngredientWithAmount ingredient) {
            return new PouringFluid(AbstractedFluidStack.EMPTY, Optional.of(ingredient));
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, PouringFluid fluid) {
            buf.writeBoolean(fluid.ingredient().isPresent());
            if (fluid.ingredient().isPresent()) {
                FluidIngredientWithAmount.STREAM_CODEC.encode(buf, fluid.ingredient().get());
            } else {
                AbstractedFluidStack.STREAM_CODEC.encode(buf, fluid.stack());
            }
        }

        private static PouringFluid fromNetwork(RegistryFriendlyByteBuf buf) {
            if (buf.readBoolean())
                return PouringFluid.ingredient(FluidIngredientWithAmount.STREAM_CODEC.decode(buf));
            return PouringFluid.exact(AbstractedFluidStack.STREAM_CODEC.decode(buf));
        }
    }

    public static class Serializer implements RecipeSerializer<KegPouringRecipe> {
        public static final MapCodec<KegPouringRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                PouringFluid.CODEC.fieldOf("fluid").forGetter(KegPouringRecipe::getSerializedFluid),
                ItemStack.CODEC.optionalFieldOf("container").forGetter(KegPouringRecipe::getRawContainer),
                ItemStack.CODEC.fieldOf("output").forGetter(KegPouringRecipe::getOutput),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegPouringRecipe::getRawUnit),
                Codec.BOOL.optionalFieldOf("strict", false).forGetter(KegPouringRecipe::isStrict),
                Codec.BOOL.optionalFieldOf("can_fill", true).forGetter(KegPouringRecipe::canFill)
        ).apply(inst, KegPouringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, KegPouringRecipe> STREAM_CODEC = StreamCodec.of(KegPouringRecipe.Serializer::toNetwork, KegPouringRecipe.Serializer::fromNetwork);

        public Serializer() {}

        public MapCodec<KegPouringRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, KegPouringRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, KegPouringRecipe recipe) {
            PouringFluid.STREAM_CODEC.encode(buf, recipe.getSerializedFluid());
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC).encode(buf, recipe.getRawContainer());
            ItemStack.STREAM_CODEC.encode(buf, recipe.getOutput());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.BOOL.encode(buf, recipe.isStrict());
            ByteBufCodecs.BOOL.encode(buf, recipe.canFill());
        }

        public static KegPouringRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            PouringFluid fluid = PouringFluid.STREAM_CODEC.decode(buf);
            Optional<ItemStack> container = ByteBufCodecs.optional(ItemStack.STREAM_CODEC).decode(buf);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
            Optional<FluidUnit> unit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            boolean strict = buf.readBoolean();
            boolean canFill = buf.readBoolean();

            return new KegPouringRecipe(fluid, container, output, unit, strict, canFill);
        }
    }
}
