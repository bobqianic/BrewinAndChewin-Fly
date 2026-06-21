package umpaz.brewinandchewin.common.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.effect.IntoxicationEffect;
import umpaz.brewinandchewin.common.effect.RagingEffect;
import umpaz.brewinandchewin.common.effect.SweetHeartEffect;
import umpaz.brewinandchewin.common.effect.TipsyEffect;


public class BnCEffects {
    public static Holder<MobEffect> INTOXICATION;
    public static Holder<MobEffect> SWEET_HEART;
    public static Holder<MobEffect> RAGING;
    public static Holder<MobEffect> TIPSY;

    public static void registerAll() {
        INTOXICATION = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, BrewinAndChewin.asResource("intoxication"), new IntoxicationEffect());
        SWEET_HEART = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, BrewinAndChewin.asResource("sweet_heart"), new SweetHeartEffect());
        RAGING = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, BrewinAndChewin.asResource("raging"), new RagingEffect());
        TIPSY = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, BrewinAndChewin.asResource("tipsy"), new TipsyEffect());
    }
}
