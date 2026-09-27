package umpaz.brewinandchewin.fabric.utility;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class BnCCreateDelegate {
    public static Fluid getPotionSource() {
        return getFluid("potion");
    }

    public static FlowingFluid getHoneySource() {
        return getFlowingFluid("honey");
    }

    public static FlowingFluid getFlowingHoney() {
        return getFlowingFluid("flowing_honey");
    }

    public static FlowingFluid getMilkSource() {
        return getFlowingFluid("milk");
    }

    public static FlowingFluid getFlowingMilk() {
        return getFlowingFluid("flowing_milk");
    }

    private static Fluid getFluid(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("create", path);
        return BuiltInRegistries.FLUID.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("Create fluid is missing: " + id));
    }

    private static FlowingFluid getFlowingFluid(String path) {
        Fluid fluid = getFluid(path);
        if (fluid instanceof FlowingFluid flowingFluid)
            return flowingFluid;
        throw new IllegalStateException("Create fluid is not a flowing fluid: create:" + path);
    }
}
