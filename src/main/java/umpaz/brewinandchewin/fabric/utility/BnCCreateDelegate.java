package umpaz.brewinandchewin.fabric.utility;

import com.zurrtum.create.AllFluids;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;

public class BnCCreateDelegate {
    public static Fluid getPotionSource() {
        return AllFluids.POTION.getSource();
    }

    public static FlowingFluid getHoneySource() {
        return (FlowingFluid) AllFluids.HONEY.getSource();
    }

    public static FlowingFluid getFlowingHoney() {
        return (FlowingFluid) AllFluids.HONEY.getFlowing();
    }

    public static FlowingFluid getMilkSource() {
        return (FlowingFluid) AllFluids.MILK.getSource();
    }

    public static FlowingFluid getFlowingMilk() {
        return (FlowingFluid) AllFluids.MILK.getFlowing();
    }
}
