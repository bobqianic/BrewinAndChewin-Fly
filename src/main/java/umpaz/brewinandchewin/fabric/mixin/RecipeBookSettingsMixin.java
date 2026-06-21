package umpaz.brewinandchewin.fabric.mixin;

import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.inventory.RecipeBookType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;

import java.util.function.UnaryOperator;

@Mixin(RecipeBookSettings.class)
public class RecipeBookSettingsMixin {
    @Unique
    private RecipeBookSettings.TypeSettings brewinandchewin$fermenting = RecipeBookSettings.TypeSettings.DEFAULT;

    @Inject(method = "getSettings", at = @At("HEAD"), cancellable = true)
    private void brewinandchewin$getFermentingSettings(RecipeBookType type, CallbackInfoReturnable<RecipeBookSettings.TypeSettings> cir) {
        if (type == BnCRecipeBookTypes.fermenting()) {
            cir.setReturnValue(brewinandchewin$fermenting);
        }
    }

    @Inject(method = "updateSettings", at = @At("HEAD"), cancellable = true)
    private void brewinandchewin$updateFermentingSettings(RecipeBookType type, UnaryOperator<RecipeBookSettings.TypeSettings> updater, CallbackInfo ci) {
        if (type == BnCRecipeBookTypes.fermenting()) {
            brewinandchewin$fermenting = updater.apply(brewinandchewin$fermenting);
            ci.cancel();
        }
    }

    @Inject(method = "copy", at = @At("RETURN"))
    private void brewinandchewin$copyFermentingSettings(CallbackInfoReturnable<RecipeBookSettings> cir) {
        ((RecipeBookSettingsMixin) (Object) cir.getReturnValue()).brewinandchewin$fermenting = brewinandchewin$fermenting;
    }

    @Inject(method = "replaceFrom", at = @At("TAIL"))
    private void brewinandchewin$replaceFermentingSettings(RecipeBookSettings other, CallbackInfo ci) {
        brewinandchewin$fermenting = ((RecipeBookSettingsMixin) (Object) other).brewinandchewin$fermenting;
    }

}
