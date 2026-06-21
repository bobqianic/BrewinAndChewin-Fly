package umpaz.brewinandchewin.common.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.access.FoodDataEntityAccess;
import umpaz.brewinandchewin.common.registry.BnCEffects;

@Mixin(FoodData.class)
public class IntoxicationCancelSaturationMixin implements FoodDataEntityAccess {

    @Unique
    private LivingEntity brewinandchewin$entity;

    @ModifyVariable(method = "add", at = @At("HEAD"), argsOnly = true, index = 2)
    private float brewinandchewin$disableSaturation(float value) {
        if (brewinandchewin$entity != null && brewinandchewin$entity.hasEffect(BnCEffects.INTOXICATION) && value > 0.0F) {
            return 0.0F;
        }
        return value;
    }

    @Override
    public void brewinandchewin$setEntity(LivingEntity entity) {
        brewinandchewin$entity = entity;
    }
}
