package umpaz.brewinandchewin.common.utility;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import umpaz.brewinandchewin.BrewinAndChewin;

public class AbstractedFluidStack {
    public static final Codec<AbstractedFluidStack> CODEC = BrewinAndChewin.getHelper().getFluidStackWrapperCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, AbstractedFluidStack> STREAM_CODEC = BrewinAndChewin.getHelper().getFluidStackWrapperStreamCodec();
    public static final AbstractedFluidStack EMPTY = new AbstractedFluidStack(Fluids.EMPTY, 0, new PatchedDataComponentMap(DataComponentMap.EMPTY), FluidUnit.getLoaderUnit(), null);

    private final Fluid fluid;
    private final long amount;
    private final DataComponentPatch components;
    private final FluidUnit unit;
    private Object loaderSpecific;

    public AbstractedFluidStack(Fluid fluid, long amount, DataComponentMap components, FluidUnit unit, Object loaderSpecific) {
        this.fluid = fluid;
        this.amount = amount;
        this.components = components instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY;
        this.unit = unit;
        this.loaderSpecific = loaderSpecific;
    }

    public AbstractedFluidStack(Fluid fluid, long amount, DataComponentMap components) {
        this(fluid, amount, components, FluidUnit.getLoaderUnit(), null);
    }

    public AbstractedFluidStack(Fluid fluid, long amount, DataComponentMap components, FluidUnit unit) {
        this(fluid, amount, components, unit, null);
    }

    public AbstractedFluidStack(Fluid fluid, long amount) {
        this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY), FluidUnit.getLoaderUnit(), null);
    }

    public boolean isEmpty() {
        return this == EMPTY || fluid == Fluids.EMPTY || amount <= 0;
    }

    public boolean matches(AbstractedFluidStack other) {
        return fluid == other.fluid && components.equals(other.components);
    }

    public Fluid fluid() {
        return isEmpty() ? Fluids.EMPTY : fluid;
    }

    public long amount() {
        return isEmpty() ? 0 : amount;
    }

    public DataComponentMap components() {
        return PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components);
    }

    public DataComponentPatch componentPatch() {
        return components;
    }

    public FluidUnit unit() {
        return unit;
    }

    public Object loaderSpecific() {
        if (loaderSpecific == null)
            loaderSpecific = BrewinAndChewin.getHelper().createLoaderFluidStack(this);

        return BrewinAndChewin.getHelper().copyLoaderFluidStack(loaderSpecific);
    }
}
