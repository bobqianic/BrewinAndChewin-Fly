package umpaz.brewinandchewin.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BnCClientSetup;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeCache;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.fluid.BnCFluidConstants;
import umpaz.brewinandchewin.common.network.clientbound.*;
import umpaz.brewinandchewin.common.registry.BnCFluids;
import umpaz.brewinandchewin.common.registry.BnCMenuTypes;
import umpaz.brewinandchewin.fabric.client.gui.BnCHUDOverlays;
import umpaz.brewinandchewin.fabric.client.integration.IntoxicationAppleSkinCompatFabric;
import umpaz.brewinandchewin.fabric.client.platform.BnCClientPlatformHelperFabric;
import umpaz.brewinandchewin.fabric.registry.BnCFluidsImpl;
import umpaz.brewinandchewin.fabric.registry.BnCLootModificationEvents;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BrewinAndChewinFabricClient implements ClientModInitializer {
    public static final Map<Identifier, ExtraModelKey<BlockStateModel>> COASTER_MODEL_KEYS = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        BrewinAndChewinClient.init(new BnCClientPlatformHelperFabric());
        BrewinAndChewin.isClient = true;

        BnCHUDOverlays.init();
        if (FabricLoader.getInstance().isModLoaded("appleskin"))
            IntoxicationAppleSkinCompatFabric.init();

        MenuScreens.register(BnCMenuTypes.KEG, KegScreen::new);
        BnCClientSetup.registerBlockEntityRenderers(BlockEntityRenderers::register);
        BnCClientSetup.registerParticles((particleType, spriteParticleRegistration) -> ParticleProviderRegistry.getInstance().register(particleType, provider -> spriteParticleRegistration.create(provider)));
        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (KegTooltip.KegTooltipComponent.class.isAssignableFrom(data.getClass())) {
                return new KegTooltip((KegTooltip.KegTooltipComponent) data);
            }
            return null;
        });
        BnCClientSetup.registerReloadListeners(preparableReloadListener -> {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return preparableReloadListener.getId();
                }

                @Override
                public CompletableFuture<Void> reload(SharedState sharedState, Executor prepareExecutor, PreparationBarrier preparationBarrier, Executor applyExecutor) {
                    return preparableReloadListener.reload(sharedState, prepareExecutor, preparationBarrier, applyExecutor);
                }
            });
        });
        BnCClientSetup.registerColorHandlers(BlockColorRegistry::register);
        PreparableModelLoadingPlugin.register((sharedState, executor) -> BnCClientSetup.getModels(sharedState.resourceManager(), executor), (data, context) -> {
            COASTER_MODEL_KEYS.clear();
            for (Identifier model : data) {
                Identifier coasterModel = model.withPath(path -> "brewinandchewin/coaster/" + path);
                ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(coasterModel::toString);
                COASTER_MODEL_KEYS.putIfAbsent(model, key);
                context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(model));
            }
            Identifier coasterModelId = BrewinAndChewin.asResource("block/coaster");
            ExtraModelKey<BlockStateModel> coasterKey = ExtraModelKey.create(coasterModelId::toString);
            COASTER_MODEL_KEYS.putIfAbsent(coasterModelId, coasterKey);
            context.addModel(coasterKey, SimpleUnbakedExtraModel.blockStateModel(coasterModelId));
        });
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, chatMessage, profile, bound, timestamp) -> {
            if (chatMessage != null) {
                BnCClientTextUtils.setupChatMessage(chatMessage);
                PlayerChatMessage tipsyMessage = BnCClientTextUtils.getTipsyMessage();
                if (tipsyMessage != null && bound.chatType().isBound()) {
                    BnCClientTextUtils.clearTipsyMessage();
                    MutableComponent boundChat = BnCClientTextUtils.getStyledChatPrefix(bound, bound.decorate(Component.literal("")).copy());
                    MutableComponent newMessage = tipsyMessage.decoratedContent().copy().withStyle(bound.chatType().value().chat().style());

                    Minecraft.getInstance().gui.getChat().addPlayerMessage(boundChat.append(newMessage.copy().withStyle(bound.chatType().value().chat().style())), tipsyMessage.signature(), GuiMessageTag.chatModified(chatMessage.signedContent()));
                    Minecraft.getInstance().getNarrator().sayChatQueued(boundChat.append(newMessage.copy().withStyle(bound.chatType().value().narration().style())));

                    if (BnCClientTextUtils.clearDelayAmount <= 0) {
                        BnCClientTextUtils.tipsyMessageLevel = 0;
                        BnCClientTextUtils.randomSeed = 0L;
                        BnCClientTextUtils.generatedRandom = false;
                    } else {
                        --BnCClientTextUtils.clearDelayAmount;
                    }
                    return false;
                }
            }
            BnCClientTextUtils.clearDelayAmount = 0;
            BnCClientTextUtils.tipsyMessageLevel = 0;
            BnCClientTextUtils.randomSeed = 0L;
            BnCClientTextUtils.generatedRandom = false;
            return true;
        });
        registerNetwork();
        registerRecipeSync();
        registerFluidRenderers();
    }

    private static void registerNetwork() {
        ClientPlayNetworking.registerGlobalReceiver(ClearKegFluidContainerComponentsClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(MakeNextPlayerChatTipsyClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SendRecipeBookValuesClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncConfigClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncNumbedHeartsClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncRagingStacksClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> BnCConfiguration.resetSyncedCommonConfig());
    }

    private static void registerRecipeSync() {
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> BnCClientRecipeCache.setRecipes(List.copyOf(recipes.recipes())));
    }

    public static void registerFluidRenderers() {
        if (BnCFluidsImpl.isBnCMilk()) {
            FluidRenderingRegistry.register(BnCFluidsImpl.MILK, BnCFluidsImpl.FLOWING_MILK,
                    createFluidModel(BrewinAndChewin.asResource("block/milk_still"), BrewinAndChewin.asResource("block/milk_flowing"), -1));
        }
        if (!BrewinAndChewin.getHelper().isModLoaded("create"))
            FluidRenderingRegistry.register(BnCFluids.HONEY, BnCFluids.FLOWING_HONEY, createHoneyModel(BnCFluidConstants.Colors.DEFAULT));

        FluidRenderingRegistry.register(BnCFluids.BEER, BnCFluids.FLOWING_BEER, createWaterModel(BnCFluidConstants.Colors.BEER));
        FluidRenderingRegistry.register(BnCFluids.VODKA, BnCFluids.FLOWING_VODKA, createWaterModel(BnCFluidConstants.Colors.VODKA));
        FluidRenderingRegistry.register(BnCFluids.MEAD, BnCFluids.FLOWING_MEAD, createHoneyModel(BnCFluidConstants.Colors.MEAD));
        FluidRenderingRegistry.register(BnCFluids.EGG_GROG, BnCFluids.FLOWING_EGG_GROG, createWaterModel(BnCFluidConstants.Colors.EGG_GROG));
        FluidRenderingRegistry.register(BnCFluids.STRONGROOT_ALE, BnCFluids.FLOWING_STRONGROOT_ALE, createWaterModel(BnCFluidConstants.Colors.STRONGROOT_ALE));
        FluidRenderingRegistry.register(BnCFluids.RICE_WINE, BnCFluids.FLOWING_RICE_WINE, createWaterModel(BnCFluidConstants.Colors.RICE_WINE));
        FluidRenderingRegistry.register(BnCFluids.GLITTERING_GRENADINE, BnCFluids.FLOWING_GLITTERING_GRENADINE, createWaterModel(BnCFluidConstants.Colors.GLITTERING_GRENADINE));
        FluidRenderingRegistry.register(BnCFluids.STEEL_TOE_STOUT, BnCFluids.FLOWING_STEEL_TOE_STOUT, createWaterModel(BnCFluidConstants.Colors.STEEL_TOE_STOUT));
        FluidRenderingRegistry.register(BnCFluids.DREAD_NOG, BnCFluids.FLOWING_DREAD_NOG, createWaterModel(BnCFluidConstants.Colors.DREAD_NOG));
        FluidRenderingRegistry.register(BnCFluids.KOMBUCHA, BnCFluids.FLOWING_KOMBUCHA, createWaterModel(BnCFluidConstants.Colors.KOMBUCHA));
        FluidRenderingRegistry.register(BnCFluids.SACCHARINE_RUM, BnCFluids.FLOWING_SACCHARINE_RUM, createWaterModel(BnCFluidConstants.Colors.SACCHARINE_RUM));
        FluidRenderingRegistry.register(BnCFluids.PALE_JANE, BnCFluids.FLOWING_PALE_JANE, createWaterModel(BnCFluidConstants.Colors.PALE_JANE));
        FluidRenderingRegistry.register(BnCFluids.SALTY_FOLLY, BnCFluids.FLOWING_SALTY_FOLLY, createWaterModel(BnCFluidConstants.Colors.SALTY_FOLLY));
        FluidRenderingRegistry.register(BnCFluids.BLOODY_MARY, BnCFluids.FLOWING_BLOODY_MARY, createWaterModel(BnCFluidConstants.Colors.BLOODY_MARY));
        FluidRenderingRegistry.register(BnCFluids.RED_RUM, BnCFluids.FLOWING_RED_RUM, createWaterModel(BnCFluidConstants.Colors.RED_RUM));
        FluidRenderingRegistry.register(BnCFluids.WITHERING_DROSS, BnCFluids.FLOWING_WITHERING_DROSS, createWaterModel(BnCFluidConstants.Colors.WITHERING_DROSS));

        FluidRenderingRegistry.register(BnCFluids.FLAXEN_CHEESE, BnCFluids.FLOWING_FLAXEN_CHEESE,
                createFluidModel(BnCFluidConstants.Textures.FLAXEN_STILL_TEXTURE, BnCFluidConstants.Textures.FLAXEN_FLOWING_TEXTURE, -1));
        FluidRenderingRegistry.register(BnCFluids.SCARLET_CHEESE, BnCFluids.FLOWING_SCARLET_CHEESE,
                createFluidModel(BnCFluidConstants.Textures.SCARLET_STILL_TEXTURE, BnCFluidConstants.Textures.SCARLET_FLOWING_TEXTURE, -1));
    }

    private static FluidModel.Unbaked createHoneyModel(int color) {
        return createFluidModel(BnCFluidConstants.Textures.HONEY_FLUID_STILL_TEXTURE, BnCFluidConstants.Textures.HONEY_FLUID_FLOWING_TEXTURE, color);
    }

    private static FluidModel.Unbaked createWaterModel(int color) {
        return createFluidModel(BnCFluidConstants.Textures.FLUID_STILL_TEXTURE, BnCFluidConstants.Textures.FLUID_FLOWING_TEXTURE, color);
    }

    private static FluidModel.Unbaked createFluidModel(Identifier stillTexture, Identifier flowingTexture, int color) {
        return new FluidModel.Unbaked(new Material(stillTexture), new Material(flowingTexture), null, BlockTintSources.constant(color));
    }
}
