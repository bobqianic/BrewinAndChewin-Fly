package umpaz.brewinandchewin.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.common.tag.BnCTags;
import vectorwing.farmersdelight.common.item.component.consumable.RemoveRandomStatusEffectsConsumeEffect;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;

@Mixin(RemoveRandomStatusEffectsConsumeEffect.class)
public class PreventTaggedEffectRemovalMixin {
    @ModifyExpressionValue(method = "apply", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;isEmpty()Z"), remap = false)
    private boolean brewinandchewin$preventLowPriorityEffectRemoval(boolean original, @Local(argsOnly = true) ItemStack stack, @Local ArrayList<Holder<MobEffect>> compatibleEffects) {
        TagKey<MobEffect> lowPriorityTag = getLowPriorityTag(stack);
        if (lowPriorityTag != null && compatibleEffects.stream().anyMatch(effect -> !effect.is(lowPriorityTag))) {
            compatibleEffects.removeIf(effect -> effect.is(lowPriorityTag));
        }
        return compatibleEffects.isEmpty();
    }

    private static TagKey<MobEffect> getLowPriorityTag(ItemStack stack) {
        if (stack.is(ModItems.MILK_BOTTLE.get())) {
            return BnCTags.Effects.MILK_BOTTLE_LOW_PRIORITY;
        }
        if (stack.is(ModItems.HOT_COCOA.get())) {
            return BnCTags.Effects.HOT_COCOA_LOW_PRIORITY;
        }
        return null;
    }
}
