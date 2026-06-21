package umpaz.brewinandchewin.fabric.client.integration;

import net.minecraft.world.food.FoodProperties;
import squeek.appleskin.api.event.FoodValuesEvent;
import umpaz.brewinandchewin.common.registry.BnCEffects;

public class IntoxicationAppleSkinCompatFabric {
    public static void init() {
        FoodValuesEvent.EVENT.register(event -> {
            if (event.player != null && event.player.hasEffect(BnCEffects.INTOXICATION)) {
                event.modifiedFoodComponent = new FoodProperties(
                        event.modifiedFoodComponent.nutrition(),
                        0.0F,
                        event.modifiedFoodComponent.canAlwaysEat()
                );
            }
        });
    }
}
