package umpaz.brewinandchewin.fabric.client.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.fabric.client.BrewinAndChewinFabricClient;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

import java.util.ArrayList;
import java.util.List;

public class BnCClientPlatformHelperFabric implements BnCClientPlatformHelper {
    private static final FluidRenderHandler HONEY_RENDER_HANDLER = new AtlasFluidRenderHandler(
            BnCFluidConstants.Textures.HONEY_FLUID_STILL_TEXTURE,
            BnCFluidConstants.Textures.HONEY_FLUID_FLOWING_TEXTURE,
            BnCFluidConstants.Colors.DEFAULT
    );
    private static final FluidRenderHandler MILK_RENDER_HANDLER = new AtlasFluidRenderHandler(
            BrewinAndChewin.asResource("block/milk_still"),
            BrewinAndChewin.asResource("block/milk_flowing"),
            BnCFluidConstants.Colors.DEFAULT
    );

    @Override
    public BlockStateModel getModel(ResourceLocation modelId) {
        BlockStateModel model = BrewinAndChewinFabricClient.COASTER_MODEL_KEYS.containsKey(modelId)
                ? ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(BrewinAndChewinFabricClient.COASTER_MODEL_KEYS.get(modelId))
                : null;
        return model != null ? model : Minecraft.getInstance().getModelManager().getMissingBlockStateModel();
    }

    @Override
    public void tesselateCoasterModel(BlockAndTintGetter level, ResourceLocation modelId, BlockState state, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer, RandomSource random, long seed, int packedOverlay, int tintIndex, RenderType renderType) {
    }

    @Override
    public void renderFluidInKeg(AbstractedFluidStack stack, GuiGraphics gui, int x, int y, float alphaModifier, long capacity) {
        if (stack.isEmpty()) {
            return;
        }
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(stack.fluid());
        FluidState state = stack.fluid().defaultFluidState();
        TextureAtlasSprite[] sprites = handler == null ? null : handler.getFluidSprites(null, null, state);
        if (!hasStillSprite(sprites)) {
            handler = getFallbackRenderHandler(stack.fluid());
            sprites = handler == null ? null : handler.getFluidSprites(null, null, state);
        }
        if (hasStillSprite(sprites)) {
            TextureAtlasSprite sprite = sprites[0];
            int tintColor = handler.getFluidColor(null, null, state);

            int color = ARGB.color(alphaModifier, tintColor);

            float fill = Math.min(capacity, stack.unit().convertToLoader(stack.amount())) / (float) capacity;
            if (fill > 0.57) {
                int y1 = y + (int) (12 * (1 - ((fill - 0.57F) / .43F)));
                int y2 = y + 12;
                float topCapacity = (fill - 0.57F) / 0.43F;
                float vDistance = sprite.getV1() - sprite.getV0();
                float v0 = sprite.getV0() + (0.25F * vDistance) + (0.75F * vDistance * (1 - topCapacity));
                gui.innerBlit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x, x + 16, y1, y2, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), color);
                gui.innerBlit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), color);

            }
            int y1 = y + 12 + (int) (16 * (1 - Math.min(1, (fill / .57F))));
            int y2 = y + 12 + 16;
            float vDistance = sprite.getV1() - sprite.getV0();
            float v0 = sprite.getV0() + (vDistance * (1 - Math.min(1, (fill / .57F))));
            gui.innerBlit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x, x + 16, y1, y2, sprite.getU0(), sprite.getU1(), v0, sprite.getV1(), color);
            gui.innerBlit(RenderPipelines.GUI_TEXTURED, sprite.atlasLocation(), x + 16, x + 16 + 8, y1, y2, sprite.getU0(), sprite.getU0() + 0.5F * (sprite.getU1() - sprite.getU0()), v0, sprite.getV1(), color);
        }
    }

    private static boolean hasStillSprite(TextureAtlasSprite[] sprites) {
        return sprites != null && sprites.length > 0 && sprites[0] != null;
    }

    private static FluidRenderHandler getFallbackRenderHandler(Fluid fluid) {
        if (fluid.isSame(BnCFluids.HONEY) || fluid.isSame(BnCFluids.FLOWING_HONEY)) {
            return HONEY_RENDER_HANDLER;
        }

        Fluid createHoney = BrewinAndChewin.getHelper().getCreateHoneyFluid();
        if (createHoney != null && fluid.isSame(createHoney)) {
            return HONEY_RENDER_HANDLER;
        }

        if (fluid.isSame(BrewinAndChewin.getHelper().getMilkFluid()) ||
                fluid.isSame(BrewinAndChewin.getHelper().getFlowingMilkFluid())) {
            return MILK_RENDER_HANDLER;
        }

        return null;
    }

    private record AtlasFluidRenderHandler(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tint) implements FluidRenderHandler {
        @Override
        public TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter view, BlockPos pos, FluidState state) {
            return new TextureAtlasSprite[]{
                    getBlockSprite(stillTexture),
                    getBlockSprite(flowingTexture)
            };
        }

        @Override
        public int getFluidColor(BlockAndTintGetter view, BlockPos pos, FluidState state) {
            return tint;
        }

        private static TextureAtlasSprite getBlockSprite(ResourceLocation texture) {
            return Minecraft.getInstance().getAtlasManager().get(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        }
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
