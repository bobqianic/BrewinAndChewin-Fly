
package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCRecipeBookTypes;

public record SendRecipeBookValuesClientboundPacket(boolean open, boolean filtering) implements CustomPacketPayload {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("send_recipe_book_values");
    public static final Type<SendRecipeBookValuesClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SendRecipeBookValuesClientboundPacket> STREAM_CODEC = StreamCodec.of(SendRecipeBookValuesClientboundPacket::encode, SendRecipeBookValuesClientboundPacket::new);

    public SendRecipeBookValuesClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    public static void encode(FriendlyByteBuf buf, SendRecipeBookValuesClientboundPacket packet) {
        buf.writeBoolean(packet.open);
        buf.writeBoolean(packet.filtering);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            if (BnCRecipeBookTypes.fermenting() == null) {
                return;
            }
            ClientRecipeBook recipeBook = Minecraft.getInstance().player.getRecipeBook();
            recipeBook.setOpen(BnCRecipeBookTypes.fermenting(), open);
            recipeBook.setFiltering(BnCRecipeBookTypes.fermenting(), filtering);
        });
    }
}
