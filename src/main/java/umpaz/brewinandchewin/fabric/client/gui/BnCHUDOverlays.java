package umpaz.brewinandchewin.fabric.client.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.registry.ModEffects;

import java.util.Random;

public class BnCHUDOverlays {
    public static int foodIconsOffset;
    private static final ResourceLocation NOURISHMENT_ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("farmersdelight", "textures/gui/fd_icons.png");
    private static final int NOURISHMENT_ICONS_TEXTURE_WIDTH = 256;
    private static final int NOURISHMENT_ICONS_TEXTURE_HEIGHT = 256;

    public static final ResourceLocation FOOD_EMPTY_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_empty_intoxication");
    public static final ResourceLocation FOOD_HALF_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_half_intoxication");
    public static final ResourceLocation FOOD_FULL_INTOXICATION_TEXTURE = BrewinAndChewin.asResource("hud/food_full_intoxication");
    public static final ResourceLocation FOOD_EMPTY_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    public static final ResourceLocation FOOD_HALF_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_half");
    public static final ResourceLocation FOOD_FULL_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_full");
    public static final ResourceLocation FOOD_EMPTY_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    public static final ResourceLocation FOOD_HALF_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    public static final ResourceLocation FOOD_FULL_HUNGER_TEXTURE = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");

    private static final ResourceLocation NAUSEA_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/nausea.png");

    private static float tipsyTransparencyModifier = 0.0F;

    public static void init() {
        HudRenderCallback.EVENT.register(IntoxicationOverlay.INSTANCE::render);
    }

    public abstract static class BaseOverlay {
        public abstract void render(GuiGraphics gui, DeltaTracker delta);

        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, DeltaTracker delta) {
            return !minecraft.options.hideGui && minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        }
    }

    public static class TipsyOverlay extends BaseOverlay {
        public static final TipsyOverlay INSTANCE = new TipsyOverlay();

        protected TipsyOverlay() {}

        public void render(GuiGraphics gui, DeltaTracker delta) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (shouldRenderOverlay(mc, player, gui, delta)) {
                MobEffectInstance effect = player.getEffect(BnCEffects.TIPSY);
                float distortionScale = mc.options.screenEffectScale().get().floatValue();
                float tipsyScale = Math.min((1 + effect.getAmplifier()) / 10.0F * 0.4F, 0.4F);
                if (distortionScale < 1.0F && tipsyScale > 0.0F) {
                    renderTipsyOverlay(gui, (1.0F - distortionScale) * tipsyScale * tipsyTransparencyModifier);
                    float partialTickModifier = delta.getGameTimeDeltaTicks() * (effect.endsWithin(60) ? -0.006F : 0.007F);
                    tipsyTransparencyModifier = Mth.clamp(tipsyTransparencyModifier + partialTickModifier, 0.0F, 1.0F);
                } else
                    tipsyTransparencyModifier = 0.0F;
            } else
                tipsyTransparencyModifier = 0.0F;
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics gui, DeltaTracker delta) {
            return super.shouldRenderOverlay(minecraft, player, gui, delta) && player != null && !player.hasEffect(MobEffects.NAUSEA) && player.hasEffect(BnCEffects.TIPSY);
        }
    }

    public static class IntoxicationOverlay extends BaseOverlay {
        public static final IntoxicationOverlay INSTANCE = new IntoxicationOverlay();

        protected IntoxicationOverlay() {}

        public void render(GuiGraphics gui, DeltaTracker deltaTracker) {
            if (!BnCConfiguration.CLIENT_CONFIG.get().intoxicationFoodOverlay())
                return;

            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;

            if (!shouldRenderOverlay(minecraft, player, gui, deltaTracker))
                return;
            int top = foodIconsOffset;
            int right = minecraft.getWindow().getGuiScaledWidth() / 2 + 91;

            drawIntoxicationOverlay(player, minecraft, gui, right, top);
        }

        @Override
        public boolean shouldRenderOverlay(Minecraft minecraft, Player player, GuiGraphics guiGraphics, DeltaTracker guiTicks) {
            return super.shouldRenderOverlay(minecraft, player, guiGraphics, guiTicks) && player != null && player.hasEffect(BnCEffects.INTOXICATION);
        }
    }

    public static void renderTipsyOverlay(GuiGraphics guiGraphics, float scalar) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int alpha = Math.round(Mth.clamp(scalar, 0.0F, 1.0F) * 255.0F);
        int red = Math.round(Mth.clamp(scalar, 0.0F, 1.0F) * 255.0F);
        int green = Math.round(Mth.clamp(0.55F * scalar, 0.0F, 1.0F) * 255.0F);
        int blue = Math.round(Mth.clamp(0.08F * scalar, 0.0F, 1.0F) * 255.0F);
        int color = alpha << 24 | red << 16 | green << 8 | blue;
        guiGraphics.blit(RenderPipelines.GUI_NAUSEA_OVERLAY, NAUSEA_LOCATION, 0, 0, 0.0F, 0.0F, width, height, width, height, color);
    }

    public static void drawIntoxicationOverlay(Player player, Minecraft minecraft, GuiGraphics graphics, int right, int top) {
        int ticks = minecraft.gui.getGuiTicks();
        Random rand = new Random();
        rand.setSeed(ticks * 312871L);

        for (int i = 0; i < 10; ++i) {
            int x = (right - i * 8 - 9) + (int) (Mth.cos((ticks + i * 2) * 0.20F) * 2f);
            int y = top + (int) (Mth.sin((ticks + i * 2) * 0.25F) * 2f);

            float effectiveHungerOfBar = (float) player.getFoodData().getFoodLevel() / 2.0F - (float) i;
            boolean hasFullFoodIcon = effectiveHungerOfBar >= 1.0F;
            boolean hasHalfFoodIcon = effectiveHungerOfBar >= 0.5F && effectiveHungerOfBar < 1.0F;

            if (player.hasEffect(ModEffects.NOURISHMENT)) {
                boolean isPlayerHealingWithSaturationAndNourishment =
                                isNaturalRegenerationEnabled(minecraft)
                                && player.isHurt()
                                && player.getFoodData().getFoodLevel() >= 18;
                int naturalHealingOffset = isPlayerHealingWithSaturationAndNourishment ? 18 : 0;

                graphics.blit(RenderPipelines.GUI_TEXTURED, NOURISHMENT_ICONS_TEXTURE, x, y, 0, 0, 9, 9, NOURISHMENT_ICONS_TEXTURE_WIDTH, NOURISHMENT_ICONS_TEXTURE_HEIGHT);

                if (hasFullFoodIcon) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, NOURISHMENT_ICONS_TEXTURE, x, y, 18 + naturalHealingOffset, 0, 9, 9, NOURISHMENT_ICONS_TEXTURE_WIDTH, NOURISHMENT_ICONS_TEXTURE_HEIGHT);
                } else if (hasHalfFoodIcon) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, NOURISHMENT_ICONS_TEXTURE, x, y, 9 + naturalHealingOffset, 0, 9, 9, NOURISHMENT_ICONS_TEXTURE_WIDTH, NOURISHMENT_ICONS_TEXTURE_HEIGHT);
                }
                continue;
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFoodEmptySprite(player), x, y, 9, 9);
            if (hasFullFoodIcon) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFoodSprite(player, false), x, y, 9, 9);
            } else if (hasHalfFoodIcon) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFoodSprite(player, true), x, y, 9, 9);
            }

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_EMPTY_INTOXICATION_TEXTURE, x, y, 9, 9);

            if (hasFullFoodIcon) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_FULL_INTOXICATION_TEXTURE, x, y, 9, 9);
            } else if (hasHalfFoodIcon) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, FOOD_HALF_INTOXICATION_TEXTURE, x, y, 9, 9);
            }
        }
    }

    private static ResourceLocation getFoodEmptySprite(Player player) {
        if (player.hasEffect(MobEffects.HUNGER))
            return FOOD_EMPTY_HUNGER_TEXTURE;
        return FOOD_EMPTY_TEXTURE;
    }

    private static ResourceLocation getFoodSprite(Player player, boolean half) {
        if (player.hasEffect(MobEffects.HUNGER))
            return half ? FOOD_HALF_HUNGER_TEXTURE : FOOD_FULL_HUNGER_TEXTURE;
        return half ? FOOD_HALF_TEXTURE : FOOD_FULL_TEXTURE;
    }

    private static boolean isNaturalRegenerationEnabled(Minecraft minecraft) {
        MinecraftServer server = minecraft.getSingleplayerServer();
        return server == null || server.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
    }
}
