package umpaz.brewinandchewin.fabric.mixin.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void brewinandchewin$scrambleName(T entity, S renderState, float partialTick, CallbackInfo ci) {
        if (renderState.nameTag != null) {
            renderState.nameTag = BnCClientTextUtils.nameTagRenderer(renderState.nameTag);
        }
    }
}
