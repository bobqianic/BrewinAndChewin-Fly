package umpaz.brewinandchewin.fabric.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import umpaz.brewinandchewin.fabric.client.gui.BnCHUDOverlays;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getFoodData()Lnet/minecraft/world/food/FoodData;"), cancellable = true)
    private void brewinandchewin$dontRenderFoodWhenIntoxicated(GuiGraphics guiGraphics, Player player, int i, int j, CallbackInfo ci) {
        BnCHUDOverlays.foodIconsOffset = i;
        if (player.hasEffect(BnCEffects.INTOXICATION))
            ci.cancel();
    }
}
