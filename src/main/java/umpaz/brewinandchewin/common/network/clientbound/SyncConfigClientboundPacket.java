package umpaz.brewinandchewin.common.network.clientbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.BnCConfiguration;
import umpaz.brewinandchewin.common.utility.FluidUnit;

public record SyncConfigClientboundPacket(BnCConfiguration.Common common) implements CustomPacketPayload {
    public static final ResourceLocation ID = BrewinAndChewin.asResource("sync_config");
    public static final Type<SyncConfigClientboundPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigClientboundPacket> STREAM_CODEC = StreamCodec.of(SyncConfigClientboundPacket::encode, SyncConfigClientboundPacket::new);

    public SyncConfigClientboundPacket(RegistryFriendlyByteBuf buf) {
        this(decodeCommon(buf));
    }

    public static void encode(RegistryFriendlyByteBuf buf, SyncConfigClientboundPacket packet) {
        encodeRoot(buf, packet.common().root());
        encodeKeg(buf, packet.common().keg());
        buf.writeBoolean(packet.common().recipeBook().enabled());
    }

    private static BnCConfiguration.Common decodeCommon(RegistryFriendlyByteBuf buf) {
        return new BnCConfiguration.Common(
                decodeRoot(buf),
                decodeKeg(buf),
                new BnCConfiguration.Common.RecipeBook(buf.readBoolean())
        );
    }

    private static BnCConfiguration.Common.Root decodeRoot(FriendlyByteBuf buf) {
        return new BnCConfiguration.Common.Root(buf.readInt(), buf.readInt(), buf.readInt());
    }

    private static void encodeRoot(FriendlyByteBuf buf, BnCConfiguration.Common.Root root) {
        buf.writeInt(root.levelChatScramble());
        buf.writeInt(root.levelSignScramble());
        buf.writeInt(root.levelNameScramble());
    }

    private static BnCConfiguration.Common.Keg decodeKeg(RegistryFriendlyByteBuf buf) {
        return new BnCConfiguration.Common.Keg(
                FluidUnit.STREAM_CODEC.decode(buf),
                buf.readLong(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    private static void encodeKeg(RegistryFriendlyByteBuf buf, BnCConfiguration.Common.Keg keg) {
        FluidUnit.STREAM_CODEC.encode(buf, keg.capacityUnit());
        buf.writeLong(keg.capacity());
        buf.writeInt(keg.cold());
        buf.writeInt(keg.chilly());
        buf.writeInt(keg.warm());
        buf.writeInt(keg.hot());
        buf.writeBoolean(keg.biomeTemp());
        buf.writeBoolean(keg.dimTemp());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
