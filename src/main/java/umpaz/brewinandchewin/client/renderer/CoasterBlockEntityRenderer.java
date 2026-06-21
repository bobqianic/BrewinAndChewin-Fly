package umpaz.brewinandchewin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.client.renderer.texture.modifier.TextureModifier;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.common.block.CoasterBlock;
import umpaz.brewinandchewin.common.block.entity.CoasterBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CoasterBlockEntityRenderer implements BlockEntityRenderer<CoasterBlockEntity, CoasterBlockEntityRenderer.CoasterRenderState> {
    private static final float MODEL_SCALE = 1.0F;
    private static final float MODEL_Y_OFFSET = 1.0F / 16.0F;
    private static final float ITEM_Y_OFFSET = 1.25F / 16.0F;
    private static final float ITEM_SCALE = 0.5F;
    private static final Map<ResourceLocation, List<ModelEntry>> ITEM_TO_MODELS = new HashMap<>();

    private final ItemModelResolver itemModelResolver;

    public CoasterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static void resetCache() {
        ITEM_TO_MODELS.clear();
    }

    public static List<ModelEntry> getModelEntries(ResourceLocation itemId) {
        return ITEM_TO_MODELS.get(itemId);
    }

    public static void addToModelMap(ResourceLocation itemId, List<ModelEntry> models) {
        ITEM_TO_MODELS.put(itemId, models);
    }

    @Override
    public CoasterRenderState createRenderState() {
        return new CoasterRenderState();
    }

    @Override
    public void submit(CoasterRenderState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(RotationSegment.convertToDegrees(renderState.blockState.getValue(CoasterBlock.ROTATION))));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        if (!renderState.blockState.getValue(CoasterBlock.INVISIBLE)) {
            BlockStateModel coasterModel = BrewinAndChewinClient.getHelper().getModel(BrewinAndChewin.asResource("block/coaster"));
            nodeCollector.submitBlockModel(poseStack, RenderType.cutout(), coasterModel, 1.0F, 1.0F, 1.0F, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }

        for (DisplayedItem displayedItem : renderState.items) {
            List<ModelEntry> modelEntries = getModelEntries(BuiltInRegistries.ITEM.getKey(displayedItem.stack().getItem()));
            if (modelEntries != null) {
                poseStack.pushPose();
                poseStack.translate(0.5F, MODEL_Y_OFFSET, 0.5F);
                poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
                poseStack.translate(-0.5F, 0.0F, -0.5F);
                for (ModelEntry modelEntry : modelEntries) {
                    RenderType renderType = RenderType.cutout();
                    int color = 0XFFFFFFFF;
                    for (TextureModifier modifier : modelEntry.modifiers()) {
                        renderType = modifier.renderType(renderState.level, renderState.blockState, renderState.blockPos, displayedItem.stack(), renderType);
                        color = modifier.color(renderState.level, renderState.blockState, renderState.blockPos, displayedItem.stack(), color);
                    }
                    poseStack.pushPose();
                    poseStack.translate(modelEntry.offsetX() / 16.0F, modelEntry.offsetY() / 16.0F, modelEntry.offsetZ() / 16.0F);
                    nodeCollector.submitBlockModel(poseStack, renderType, BrewinAndChewinClient.getHelper().getModel(modelEntry.model()), ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
                poseStack.popPose();
                continue;
            }

            if (displayedItem.itemRenderState().isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5F, ITEM_Y_OFFSET, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            displayedItem.itemRenderState().submit(poseStack, nodeCollector, renderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public void extractRenderState(CoasterBlockEntity blockEntity, CoasterRenderState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.level = blockEntity.getLevel();
        renderState.items.clear();
        int seed = (int) blockEntity.getBlockPos().asLong();
        int index = 0;
        for (ItemStack stack : blockEntity.getItems()) {
            if (!stack.isEmpty()) {
                ItemStackRenderState itemRenderState = new ItemStackRenderState();
                this.itemModelResolver.updateForTopItem(itemRenderState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, seed + index);
                renderState.items.add(new DisplayedItem(stack.copy(), itemRenderState));
                ++index;
            }
        }
    }

    public static class CoasterRenderState extends BlockEntityRenderState {
        public final List<DisplayedItem> items = new ArrayList<>();
        public @Nullable net.minecraft.world.level.Level level;
    }

    public record DisplayedItem(ItemStack stack, ItemStackRenderState itemRenderState) {
    }

    public record ModelEntry(ResourceLocation model, List<? extends TextureModifier> modifiers, List<Float> offset) {
        private static final List<Float> DEFAULT_OFFSET = List.of(0.0F, 0.0F, 0.0F);
        private static final Codec<ModelEntry> DIRECT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(ModelEntry::model),
                BnCTextureModifiers.CODEC.listOf().optionalFieldOf("texture_modifiers", List.of()).forGetter(modelEntry -> (List) modelEntry.modifiers()),
                Codec.FLOAT.listOf().optionalFieldOf("offset", DEFAULT_OFFSET).forGetter(ModelEntry::offset)
        ).apply(inst, ModelEntry::new));
        public static final Codec<List<ModelEntry>> LIST_CODEC = Codec.either(ResourceLocation.CODEC, DIRECT_CODEC.listOf())
                .xmap(either -> either.map(resourceLocation -> List.of(new ModelEntry(resourceLocation, List.of(), DEFAULT_OFFSET)), Function.identity()), modelEntry -> {
                    if (modelEntry.size() == 1 && modelEntry.getFirst().modifiers().isEmpty() && modelEntry.getFirst().hasDefaultOffset())
                        return Either.left(modelEntry.getFirst().model());
                    return Either.right(modelEntry);
                });

        private boolean hasDefaultOffset() {
            return offsetX() == 0.0F && offsetY() == 0.0F && offsetZ() == 0.0F;
        }

        private float offsetX() {
            return offset.size() > 0 ? offset.get(0) : 0.0F;
        }

        private float offsetY() {
            return offset.size() > 1 ? offset.get(1) : 0.0F;
        }

        private float offsetZ() {
            return offset.size() > 2 ? offset.get(2) : 0.0F;
        }
    }
}
