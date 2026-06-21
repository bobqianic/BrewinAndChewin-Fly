package umpaz.brewinandchewin.client.utility;

import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;

public class BnCHudIcons {
    public static final ResourceLocation TIPSY_HALF = BrewinAndChewin.asResource("hud/heart/tipsy_half");
    public static final ResourceLocation TIPSY_FULL = BrewinAndChewin.asResource("hud/heart/tipsy_full");
    public static final ResourceLocation TIPSY_RIGHT = BrewinAndChewin.asResource("hud/heart/tipsy_right");

    public static final ResourceLocation ABSORBING_TIPSY_HALF = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_half");
    public static final ResourceLocation ABSORBING_TIPSY_FULL = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_full");
    public static final ResourceLocation ABSORBING_TIPSY_RIGHT = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_right");

    public static final ResourceLocation TIPSY_HARDCORE_HALF = BrewinAndChewin.asResource("hud/heart/tipsy_hardcore_half");
    public static final ResourceLocation TIPSY_HARDCORE_FULL = BrewinAndChewin.asResource("hud/heart/tipsy_hardcore_full");
    public static final ResourceLocation TIPSY_HARDCORE_RIGHT = BrewinAndChewin.asResource("hud/heart/tipsy_hardcore_right");

    public static final ResourceLocation ABSORBING_TIPSY_HARDCORE_HALF = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_hardcore_half");
    public static final ResourceLocation ABSORBING_TIPSY_HARDCORE_FULL = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_hardcore_full");
    public static final ResourceLocation ABSORBING_TIPSY_HARDCORE_RIGHT = BrewinAndChewin.asResource("hud/heart/absorbing_tipsy_hardcore_right");

    public static ResourceLocation getTipsyFullHeartTexture(boolean absorption, boolean hardcore) {
        if (hardcore) {
            if (absorption)
                return ABSORBING_TIPSY_HARDCORE_FULL;
            return TIPSY_HARDCORE_FULL;
        }
        if (absorption)
            return ABSORBING_TIPSY_FULL;
        return TIPSY_FULL;
    }

    public static ResourceLocation getTipsyHalfHeartTexture(boolean absorption, boolean hardcore) {
        if (hardcore) {
            if (absorption)
                return ABSORBING_TIPSY_HARDCORE_HALF;
            return TIPSY_HARDCORE_HALF;
        }
        if (absorption)
            return ABSORBING_TIPSY_HALF;
        return TIPSY_HALF;
    }

    public static ResourceLocation getTipsyRightHeartTexture(boolean absorption, boolean hardcore) {
        if (hardcore) {
            if (absorption)
                return ABSORBING_TIPSY_HARDCORE_RIGHT;
            return TIPSY_HARDCORE_RIGHT;
        }
        if (absorption)
            return ABSORBING_TIPSY_RIGHT;
        return TIPSY_RIGHT;
    }
}
