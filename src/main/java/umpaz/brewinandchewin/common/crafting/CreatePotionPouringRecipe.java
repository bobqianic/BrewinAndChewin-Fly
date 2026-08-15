package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeSerializer;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCRecipeSerializers;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.common.utility.KegRecipeWrapper;

import java.util.Optional;

public class CreatePotionPouringRecipe extends KegPouringRecipe {
    public CreatePotionPouringRecipe(Optional<ItemStackTemplate> container, ItemStackTemplate result, long amount, Optional<FluidUnit> unit, boolean canFill) {
        super(new AbstractedFluidStack(BrewinAndChewin.getHelper().getCreatePotionFluid(), amount), container, result, unit, false, canFill);
    }

    @Override
    public ItemStack assemble(KegRecipeWrapper recipeWrapper) {
        ItemStack stack = super.assemble(recipeWrapper).copy();
        AbstractedFluidStack fluidStack = recipeWrapper.getFluid();
        if (fluidStack.components().has(DataComponents.POTION_CONTENTS) && fluidStack.components().get(DataComponents.POTION_CONTENTS) != PotionContents.EMPTY)
            stack.set(DataComponents.POTION_CONTENTS, fluidStack.components().get(DataComponents.POTION_CONTENTS));
        return stack;
    }

    @Override
    public AbstractedFluidStack getFluid(ItemStack container) {
        AbstractedFluidStack fluidStack = super.getFluid(container);
        DataComponentPatch.Builder patch = DataComponentPatch.builder();
        for (var entry : container.getComponentsPatch().entrySet())
            patch.set((DataComponentType<Object>) entry.getKey(), entry.getValue().get());
        if (container.has(DataComponents.POTION_CONTENTS) && container.get(DataComponents.POTION_CONTENTS) != PotionContents.EMPTY)
            patch.set(DataComponents.POTION_CONTENTS, container.get(DataComponents.POTION_CONTENTS));
        return new AbstractedFluidStack(fluidStack.fluid(), fluidStack.amount(), PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch.build()), getUnit());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<KegPouringRecipe> getSerializer() {
        return (RecipeSerializer) BnCRecipeSerializers.CREATE_POTION_POURING;
    }

    public static class Serializer {
        public static final MapCodec<CreatePotionPouringRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ItemStackTemplate.CODEC.optionalFieldOf("container").forGetter(KegPouringRecipe::getRawContainer),
                ItemStackTemplate.CODEC.fieldOf("output").forGetter(KegPouringRecipe::getOutputTemplate),
                Codec.LONG.fieldOf("fluid_amount").forGetter(CreatePotionPouringRecipe::getFluidAmount),
                FluidUnit.CODEC.optionalFieldOf("unit").forGetter(KegPouringRecipe::getRawUnit),
                Codec.BOOL.optionalFieldOf("can_fill", true).forGetter(KegPouringRecipe::canFill)
        ).apply(inst, CreatePotionPouringRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, CreatePotionPouringRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        public static CreatePotionPouringRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Optional<ItemStackTemplate> container = ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).decode(buf);
            ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
            long amount = buf.readLong();
            Optional<FluidUnit> unit = ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).decode(buf);
            boolean canFill = buf.readBoolean();
            return new CreatePotionPouringRecipe(container, output, amount, unit, canFill);
        }

        public static void toNetwork(RegistryFriendlyByteBuf buf, CreatePotionPouringRecipe recipe) {
            ByteBufCodecs.optional(ItemStackTemplate.STREAM_CODEC).encode(buf, Optional.of(recipe.getContainerTemplate()));
            ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.getOutputTemplate());
            buf.writeLong(recipe.getFluidAmount());
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC).encode(buf, recipe.getRawUnit());
            ByteBufCodecs.BOOL.encode(buf, recipe.canFill());
        }

    }
}
