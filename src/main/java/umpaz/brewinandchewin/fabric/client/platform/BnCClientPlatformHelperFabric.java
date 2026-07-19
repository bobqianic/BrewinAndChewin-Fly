package umpaz.brewinandchewin.fabric.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.fabric.client.BrewinAndChewinFabricClient;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

import java.util.ArrayList;
import java.util.List;

public class BnCClientPlatformHelperFabric implements BnCClientPlatformHelper {
    @Override
    public BlockStateModel getModel(Identifier modelId) {
        BlockStateModel model = BrewinAndChewinFabricClient.COASTER_MODEL_KEYS.containsKey(modelId)
                ? ((FabricModelManager) Minecraft.getInstance().getModelManager()).getModel(BrewinAndChewinFabricClient.COASTER_MODEL_KEYS.get(modelId))
                : null;
        return model != null ? model : Minecraft.getInstance().getModelManager().getBlockStateModelSet().missingModel();
    }

    @Override
    public void tesselateCoasterModel(BlockAndTintGetter level, Identifier modelId, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, RandomSource random, long seed, int packedOverlay, int tintIndex, RenderType renderType) {
    }

    @Override
    public void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphicsExtractor gui, int x, int y, float alphaModifier, long capacity) {
        if (stack.isEmpty()) {
            return;
        }
        FluidState state = stack.fluid().defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        TextureAtlasSprite sprite = model.stillMaterial().sprite();
        int tintColor = model.tintSource() == null ? -1 : model.tintSource().color(state.createLegacyBlock());
        int color = ARGB.color(alphaModifier, tintColor);
        float fill = Math.min(capacity, stack.unit().convertToLoader(stack.amount())) / (float) capacity;
        if (fill > 0.57F) {
            int y1 = y + (int) (12 * (1 - ((fill - 0.57F) / 0.43F)));
            blitFluidSprite(gui, sprite, x, y1, 16, y + 12 - y1, color);
            blitFluidSprite(gui, sprite, x + 16, y1, 8, y + 12 - y1, color);
        }
        int y1 = y + 12 + (int) (16 * (1 - Math.min(1, fill / 0.57F)));
        blitFluidSprite(gui, sprite, x, y1, 16, y + 28 - y1, color);
        blitFluidSprite(gui, sprite, x + 16, y1, 8, y + 28 - y1, color);
    }

    private static void blitFluidSprite(GuiGraphicsExtractor gui, TextureAtlasSprite sprite, int x, int y, int width, int height, int color) {
        if (height <= 0) {
            return;
        }
        gui.enableScissor(x, y, x + width, y + height);
        gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + height - 16, 16, 16, color);
        gui.disableScissor();
    }

    @Override
    public <T extends Recipe<?>> List<RecipeHolder<T>> getSynchronizedRecipes(RecipeType<T> recipeType) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return List.of();
        }
        List<RecipeHolder<T>> results = new ArrayList<>();
        for (RecipeHolder<?> recipe : ((FabricRecipeManager) connection.recipes()).getSynchronizedRecipes().recipes()) {
            if (recipe.value().getType() == recipeType) {
                results.add((RecipeHolder<T>) recipe);
            }
        }
        return results;
    }
}
