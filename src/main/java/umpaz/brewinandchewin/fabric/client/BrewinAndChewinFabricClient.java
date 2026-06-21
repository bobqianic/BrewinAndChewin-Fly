package umpaz.brewinandchewin.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.BnCClientSetup;
import umpaz.brewinandchewin.client.BrewinAndChewinClient;
import umpaz.brewinandchewin.client.gui.KegScreen;
import umpaz.brewinandchewin.client.gui.KegTooltip;
import umpaz.brewinandchewin.client.utility.BnCClientRecipeCache;
import umpaz.brewinandchewin.client.utility.BnCClientTextUtils;
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
    public static final Map<ResourceLocation, ExtraModelKey<BlockStateModel>> COASTER_MODEL_KEYS = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        BrewinAndChewinClient.init(new BnCClientPlatformHelperFabric());
        BrewinAndChewin.isClient = true;

        BnCHUDOverlays.init();
        if (FabricLoader.getInstance().isModLoaded("appleskin"))
            IntoxicationAppleSkinCompatFabric.init();

        MenuScreens.register(BnCMenuTypes.KEG, KegScreen::new);
        BnCClientSetup.registerBlockEntityRenderers(BlockEntityRenderers::register);
        BnCClientSetup.registerParticles((particleType, spriteParticleRegistration) -> ParticleFactoryRegistry.getInstance().register(particleType, provider -> spriteParticleRegistration.create(provider)));
        TooltipComponentCallback.EVENT.register(data -> {
            if (KegTooltip.KegTooltipComponent.class.isAssignableFrom(data.getClass())) {
                return new KegTooltip((KegTooltip.KegTooltipComponent) data);
            }
            return null;
        });
        BnCClientSetup.registerReloadListeners(preparableReloadListener -> {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return preparableReloadListener.getId();
                }

                @Override
                public CompletableFuture<Void> reload(SharedState sharedState, Executor prepareExecutor, PreparationBarrier preparationBarrier, Executor applyExecutor) {
                    return preparableReloadListener.reload(sharedState, prepareExecutor, preparationBarrier, applyExecutor);
                }
            });
        });
        BnCClientSetup.registerColorHandlers(ColorProviderRegistry.BLOCK::register);
        PreparableModelLoadingPlugin.register((sharedState, executor) -> BnCClientSetup.getModels(sharedState.resourceManager(), executor), (data, context) -> {
            COASTER_MODEL_KEYS.clear();
            for (ResourceLocation model : data) {
                ResourceLocation coasterModel = model.withPath(path -> "brewinandchewin/coaster/" + path);
                ExtraModelKey<BlockStateModel> key = ExtraModelKey.create(coasterModel::toString);
                COASTER_MODEL_KEYS.putIfAbsent(model, key);
                context.addModel(key, SimpleUnbakedExtraModel.blockStateModel(model));
            }
            ResourceLocation coasterModelId = BrewinAndChewin.asResource("block/coaster");
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

                    Minecraft.getInstance().gui.getChat().addMessage(boundChat.append(newMessage.copy().withStyle(bound.chatType().value().chat().style())), tipsyMessage.signature(), GuiMessageTag.chatModified(chatMessage.signedContent()));
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
        ClientPlayNetworking.registerGlobalReceiver(SyncNumbedHeartsClientboundPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(SyncRagingStacksClientboundPacket.TYPE, (payload, context) -> payload.handle());
    }

    private static void registerRecipeSync() {
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> BnCClientRecipeCache.setRecipes(List.copyOf(recipes.recipes())));
    }

    public static void registerFluidRenderers() {
        if (BnCFluidsImpl.isBnCMilk()) {
            FluidRenderHandlerRegistry.INSTANCE.register(BnCFluidsImpl.MILK, BnCFluidsImpl.FLOWING_MILK,
                    new SimpleFluidRenderHandler(
                            BrewinAndChewin.asResource("block/milk_still"),
                            BrewinAndChewin.asResource("block/milk_flowing")
                    ));
        }
        if (!BrewinAndChewin.getHelper().isModLoaded("create"))
            FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.HONEY, BnCFluids.FLOWING_HONEY, createHoneyRenderHandler(BnCFluidConstants.Colors.DEFAULT));

        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.BEER, BnCFluids.FLOWING_BEER, createWaterExtension(BnCFluidConstants.Colors.BEER));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.VODKA, BnCFluids.FLOWING_VODKA, createWaterExtension(BnCFluidConstants.Colors.VODKA));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.MEAD, BnCFluids.FLOWING_MEAD, createHoneyRenderHandler(BnCFluidConstants.Colors.MEAD));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.EGG_GROG, BnCFluids.FLOWING_EGG_GROG, createWaterExtension(BnCFluidConstants.Colors.EGG_GROG));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.STRONGROOT_ALE, BnCFluids.FLOWING_STRONGROOT_ALE, createWaterExtension(BnCFluidConstants.Colors.STRONGROOT_ALE));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.RICE_WINE, BnCFluids.FLOWING_RICE_WINE, createWaterExtension(BnCFluidConstants.Colors.RICE_WINE));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.GLITTERING_GRENADINE, BnCFluids.FLOWING_GLITTERING_GRENADINE, createWaterExtension(BnCFluidConstants.Colors.GLITTERING_GRENADINE));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.STEEL_TOE_STOUT, BnCFluids.FLOWING_STEEL_TOE_STOUT, createWaterExtension(BnCFluidConstants.Colors.STEEL_TOE_STOUT));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.DREAD_NOG, BnCFluids.FLOWING_DREAD_NOG, createWaterExtension(BnCFluidConstants.Colors.DREAD_NOG));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.KOMBUCHA, BnCFluids.FLOWING_KOMBUCHA, createWaterExtension(BnCFluidConstants.Colors.KOMBUCHA));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.SACCHARINE_RUM, BnCFluids.FLOWING_SACCHARINE_RUM, createWaterExtension(BnCFluidConstants.Colors.SACCHARINE_RUM));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.PALE_JANE, BnCFluids.FLOWING_PALE_JANE, createWaterExtension(BnCFluidConstants.Colors.PALE_JANE));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.SALTY_FOLLY, BnCFluids.FLOWING_SALTY_FOLLY, createWaterExtension(BnCFluidConstants.Colors.SALTY_FOLLY));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.BLOODY_MARY, BnCFluids.FLOWING_BLOODY_MARY, createWaterExtension(BnCFluidConstants.Colors.BLOODY_MARY));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.RED_RUM, BnCFluids.FLOWING_RED_RUM, createWaterExtension(BnCFluidConstants.Colors.RED_RUM));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.WITHERING_DROSS, BnCFluids.FLOWING_WITHERING_DROSS, createWaterExtension(BnCFluidConstants.Colors.WITHERING_DROSS));

        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.FLAXEN_CHEESE, BnCFluids.FLOWING_FLAXEN_CHEESE,
                new SimpleFluidRenderHandler(
                        BnCFluidConstants.Textures.FLAXEN_STILL_TEXTURE,
                        BnCFluidConstants.Textures.FLAXEN_FLOWING_TEXTURE
                ));
        FluidRenderHandlerRegistry.INSTANCE.register(BnCFluids.SCARLET_CHEESE, BnCFluids.FLOWING_SCARLET_CHEESE,
                new SimpleFluidRenderHandler(
                        BnCFluidConstants.Textures.SCARLET_STILL_TEXTURE,
                        BnCFluidConstants.Textures.SCARLET_FLOWING_TEXTURE
                ));
    }

    private static FluidRenderHandler createHoneyRenderHandler(int color) {
        return new SimpleFluidRenderHandler(
                BnCFluidConstants.Textures.HONEY_FLUID_STILL_TEXTURE,
                BnCFluidConstants.Textures.HONEY_FLUID_FLOWING_TEXTURE,
                color
        );
    }
    
    private static FluidRenderHandler createWaterExtension(int color) {
        return new SimpleFluidRenderHandler(
                BnCFluidConstants.Textures.FLUID_STILL_TEXTURE,
                BnCFluidConstants.Textures.FLUID_FLOWING_TEXTURE, 
                color
        );
    }
}
