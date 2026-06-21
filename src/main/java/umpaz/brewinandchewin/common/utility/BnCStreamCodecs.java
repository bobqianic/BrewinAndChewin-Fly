package umpaz.brewinandchewin.common.utility;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public class BnCStreamCodecs {
    public static final StreamCodec<ByteBuf, Long> LONG = new StreamCodec<>() {
        public Long decode(ByteBuf buf) {
            return buf.readLong();
        }

        public void encode(ByteBuf buf, Long l) {
            buf.writeLong(l);
        }
    };

    public static <B extends ByteBuf, F, S> StreamCodec<B, Pair<F, S>> pair(StreamCodec<B, F> firstStreamCodec, StreamCodec<B, S> secondStreamCodec) {
        return StreamCodec.composite(
                firstStreamCodec, Pair::getFirst,
                secondStreamCodec, Pair::getSecond,
                Pair::of
        );
    }

    public static final StreamCodec<ByteBuf, List<Pair<Integer, Integer>>> INT_PAIR_LIST = pair(ByteBufCodecs.INT, ByteBufCodecs.INT).apply(ByteBufCodecs.list());
    public static final StreamCodec<ByteBuf, List<Pair<Integer, Long>>> INT_LONG_PAIR_LIST = pair(ByteBufCodecs.INT, LONG).apply(ByteBufCodecs.list());

}
