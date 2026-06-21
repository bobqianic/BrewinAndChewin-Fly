package umpaz.brewinandchewin.fabric.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FlowingFluid;

public class BnCFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
    public static final BnCFluidVariantAttributeHandler INSTANCE = new BnCFluidVariantAttributeHandler();

    protected BnCFluidVariantAttributeHandler() {}

    public Component getName(FluidVariant fluidVariant) {
        return Component.translatable("fluid_type." + BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid() instanceof FlowingFluid flowing ? flowing.getSource() : fluidVariant.getFluid()).toShortLanguageKey());
    }
}
