package umpaz.brewinandchewin.common.utility;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.BrewinAndChewin;

import java.util.List;

public interface AbstractedFluidIngredient {
    Codec<AbstractedFluidIngredient> CODEC = BrewinAndChewin.getHelper().getFluidIngredientWrapperCodec();
    StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidIngredient> STREAM_CODEC = BrewinAndChewin.getHelper().getFluidIngredientWrapperStreamCodec();

    List<AbstractedFluidStack> displayStacks();
    boolean matches(AbstractedFluidStack wrapper);
}
