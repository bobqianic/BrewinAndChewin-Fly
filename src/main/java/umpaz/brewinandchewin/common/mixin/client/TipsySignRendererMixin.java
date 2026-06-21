package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

@Mixin(AbstractSignRenderer.class)
public class TipsySignRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void brewinandchewin$scrambleSignText(SignBlockEntity signBlockEntity, SignRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        if (renderState.frontText != null) {
            renderState.frontText = BnCClientTextUtils.signRenderer(renderState.frontText);
        }
        if (renderState.backText != null) {
            renderState.backText = BnCClientTextUtils.signRenderer(renderState.backText);
        }
    }
}
