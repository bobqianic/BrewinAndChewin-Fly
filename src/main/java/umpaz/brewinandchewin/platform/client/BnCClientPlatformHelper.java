package umpaz.brewinandchewin.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;

import java.util.List;

public interface BnCClientPlatformHelper {
    BlockStateModel getModel(ResourceLocation modelId);

    void tesselateCoasterModel(BlockAndTintGetter level,
                               ResourceLocation modelId,
                               BlockState state,
                               BlockPos pos,
                               PoseStack poseStack,
                               MultiBufferSource buffer,
                               RandomSource random,
                               long seed,
                               int packedOverlay,
                               int tintIndex,
                               RenderType renderType);

    void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphics graphics, int x, int y, float alphaModifier, long capacity);

    default <T extends Recipe<?>> List<RecipeHolder<T>> getSynchronizedRecipes(RecipeType<T> recipeType) {
        return List.of();
    }
}
