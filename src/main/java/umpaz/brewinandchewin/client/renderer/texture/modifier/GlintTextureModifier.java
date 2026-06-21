
package umpaz.brewinandchewin.client.renderer.texture.modifier;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.BrewinAndChewin;

public class GlintTextureModifier implements TextureModifier {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("glint");
    public static final MapCodec<GlintTextureModifier> CODEC = MapCodec.unit(GlintTextureModifier::new);

    public RenderType renderType(BlockAndTintGetter level, BlockState state, BlockPos pos, ItemStack stack, RenderType previous) {
        return RenderType.glint();
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public MapCodec<? extends TextureModifier> codec() {
        return CODEC;
    }
}
