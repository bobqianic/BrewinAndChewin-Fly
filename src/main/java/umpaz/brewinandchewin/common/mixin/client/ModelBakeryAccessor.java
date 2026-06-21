
package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
    @Invoker("getModel")
    UnbakedModel brewinandchewin$getModel(ResourceLocation location);
}
