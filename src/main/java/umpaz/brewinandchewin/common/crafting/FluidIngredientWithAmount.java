package umpaz.brewinandchewin.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import umpaz.brewinandchewin.common.utility.AbstractedFluidIngredient;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.BnCStreamCodecs;
import umpaz.brewinandchewin.common.utility.FluidUnit;

import java.util.Optional;

public record FluidIngredientWithAmount(AbstractedFluidIngredient ingredient, long amount, Optional<FluidUnit> unit) {
    public static final long DEFAULT_AMOUNT = 81000L;

    public static final Codec<FluidIngredientWithAmount> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            AbstractedFluidIngredient.CODEC.fieldOf("ingredient").forGetter(FluidIngredientWithAmount::ingredient),
            Codec.LONG.validate(l -> {
                if (l < 1)
                    return DataResult.error(() -> "Fluid Ingredient amount must be at least 1.");
                return DataResult.success(l);
            }).optionalFieldOf("amount").forGetter(fluidIngredientWithAmount -> Optional.of(fluidIngredientWithAmount.amount())),
            FluidUnit.CODEC.optionalFieldOf("unit").forGetter(FluidIngredientWithAmount::unit)
    ).apply(inst, (t1, t2, t3) -> new FluidIngredientWithAmount(t1, t2.orElse(DEFAULT_AMOUNT), t3)));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredientWithAmount> STREAM_CODEC = StreamCodec.composite(
            AbstractedFluidIngredient.STREAM_CODEC, FluidIngredientWithAmount::ingredient,
            BnCStreamCodecs.LONG, FluidIngredientWithAmount::amount,
            ByteBufCodecs.optional(FluidUnit.STREAM_CODEC), FluidIngredientWithAmount::unit,
            FluidIngredientWithAmount::new
    );

    public FluidUnit getUnit() {
        return unit().orElse(FluidUnit.getLoaderUnit());
    }

    public long loaderAmount() {
        FluidUnit unit = unit().orElse(FluidUnit.getLoaderUnit());
        return unit.convertToLoader(amount);
    }

    public FluidIngredientWithAmount {
        if (ingredient.matches(AbstractedFluidStack.EMPTY))
            throw new IllegalArgumentException("Fluid Ingredient must not accept empty.");
        if (amount <= 0)
            throw new IllegalArgumentException("Fluid Ingredient amount must be higher than 0.");
    }
}
