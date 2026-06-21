package umpaz.brewinandchewin.common.network.serverbound;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.container.KegMenu;
import umpaz.brewinandchewin.common.crafting.KegFermentingRecipe;
import umpaz.brewinandchewin.common.utility.BnCStreamCodecs;
import umpaz.brewinandchewin.integration.jei.transfer.FermentingTransferServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record JEITransferKegRecipeServerboundPacket(ResourceLocation recipeId,
                                                    List<Pair<Integer, Integer>> resultSlots,
                                                    List<Pair<Integer, Long>> fluidSlots,
                                                    List<Pair<Integer, Long>> emptyingSlots,
                                                    List<Integer> craftingSlots,
                                                    List<Integer> inventorySlots,
                                                    boolean maxTransfer) implements CustomPacketPayload {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("jei_transfer_keg_recipe");
    public static final CustomPacketPayload.Type<JEITransferKegRecipeServerboundPacket> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, JEITransferKegRecipeServerboundPacket> STREAM_CODEC = StreamCodec.of(JEITransferKegRecipeServerboundPacket::encode, JEITransferKegRecipeServerboundPacket::new);

    public JEITransferKegRecipeServerboundPacket(RegistryFriendlyByteBuf buf) {
        this(
                buf.readResourceLocation(),
                BnCStreamCodecs.INT_PAIR_LIST.decode(buf),
                BnCStreamCodecs.INT_LONG_PAIR_LIST.decode(buf),
                BnCStreamCodecs.INT_LONG_PAIR_LIST.decode(buf),
                ByteBufCodecs.INT.apply(ByteBufCodecs.list()).decode(buf),
                ByteBufCodecs.INT.apply(ByteBufCodecs.list()).decode(buf),
                buf.readBoolean()
        );
    }

    public static void encode(FriendlyByteBuf buf, JEITransferKegRecipeServerboundPacket packet) {
        buf.writeResourceLocation(packet.recipeId);
        BnCStreamCodecs.INT_PAIR_LIST.encode(buf, packet.resultSlots);
        BnCStreamCodecs.INT_LONG_PAIR_LIST.encode(buf, packet.fluidSlots);
        BnCStreamCodecs.INT_LONG_PAIR_LIST.encode(buf, packet.emptyingSlots);
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()).encode(buf, packet.craftingSlots);
        ByteBufCodecs.INT.apply(ByteBufCodecs.list()).encode(buf, packet.inventorySlots);
        buf.writeBoolean(packet.maxTransfer);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer sender) {
        sender.level().getServer().execute(() -> {
            var recipe = sender.level().getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, recipeId()));
            if (recipe.isEmpty() || !(recipe.get().value() instanceof KegFermentingRecipe kegFermentingRecipe))
                return;
            AbstractContainerMenu menu = sender.containerMenu;
            if (!(menu instanceof KegMenu)) {
                return;
            }
            Optional<TransferOperations> transferOperations = TransferOperations.readFromIntegers(resultSlots(), fluidSlots(), emptyingSlots(), menu);
            Optional<List<Slot>> craftingSlots = readSlots(craftingSlots(), menu);
            Optional<List<Slot>> inventorySlots = readSlots(inventorySlots(), menu);
            if (transferOperations.isEmpty() || craftingSlots.isEmpty() || inventorySlots.isEmpty()) {
                return;
            }
            FermentingTransferServer.setItems(
                    sender,
                    kegFermentingRecipe,
                    transferOperations.get(),
                    craftingSlots.get(),
                    inventorySlots.get(),
                    maxTransfer()
            );
        });
    }

    private static Optional<List<Slot>> readSlots(List<Integer> slotIndexes, AbstractContainerMenu menu) {
        List<Slot> slots = new ArrayList<>(slotIndexes.size());
        for (int slotIndex : slotIndexes) {
            Optional<Slot> slot = getSlot(slotIndex, menu);
            if (slot.isEmpty()) {
                return Optional.empty();
            }
            slots.add(slot.get());
        }
        return Optional.of(slots);
    }

    private static Optional<Slot> getSlot(int slotIndex, AbstractContainerMenu menu) {
        if (slotIndex < 0 || slotIndex >= menu.slots.size()) {
            return Optional.empty();
        }
        return Optional.of(menu.getSlot(slotIndex));
    }

    public static class TransferOperations {
        public final List<Pair<Slot, Slot>> results = new ArrayList<>();
        public final List<Pair<Slot, Long>> fluidResults = new ArrayList<>();
        public final List<Pair<Slot, Long>> emptyingResults = new ArrayList<>();

        public static Optional<TransferOperations> readFromIntegers(List<Pair<Integer, Integer>> resultSlots,
                                                                    List<Pair<Integer, Long>> fluidSlots,
                                                                    List<Pair<Integer, Long>> emptyingSlots,
                                                                    AbstractContainerMenu menu) {
            TransferOperations operations = new TransferOperations();
            for (Pair<Integer, Integer> resultSlot : resultSlots) {
                int inventorySlotIndex = resultSlot.getFirst();
                int craftingSlotIndex = resultSlot.getSecond();
                Optional<Slot> inventorySlot = getSlot(inventorySlotIndex, menu);
                Optional<Slot> craftingSlot = getSlot(craftingSlotIndex, menu);
                if (inventorySlot.isEmpty() || craftingSlot.isEmpty()) {
                    return Optional.empty();
                }
                operations.results.add(Pair.of(inventorySlot.get(), craftingSlot.get()));
            }
            for (Pair<Integer, Long> fluidSlot : fluidSlots) {
                int fluidSlotIndex = fluidSlot.getFirst();
                long fluidAmount = fluidSlot.getSecond();
                Optional<Slot> slot = getSlot(fluidSlotIndex, menu);
                if (slot.isEmpty()) {
                    return Optional.empty();
                }
                operations.fluidResults.add(Pair.of(slot.get(), fluidAmount));
            }
            for (Pair<Integer, Long> emptyingSlot : emptyingSlots) {
                int emptyingSlotIndex = emptyingSlot.getFirst();
                long fluidAmount = emptyingSlot.getSecond();
                Optional<Slot> slot = getSlot(emptyingSlotIndex, menu);
                if (slot.isEmpty()) {
                    return Optional.empty();
                }
                operations.emptyingResults.add(Pair.of(slot.get(), fluidAmount));
            }
            return Optional.of(operations);
        }
    }
}
