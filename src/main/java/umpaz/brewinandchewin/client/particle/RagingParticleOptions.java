package umpaz.brewinandchewin.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

import java.util.function.Function;

public abstract class RagingParticleOptions extends ScalableParticleOptionsBase {
    public RagingParticleOptions(float size) {
        super(size);
    }

    public static <T extends RagingParticleOptions> MapCodec<T> createCodec(Function<T, Float> sizeFunction, Function<Float, T> constructor) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.floatRange(0.0F, 1.0F).fieldOf("size").forGetter(sizeFunction)
        ).apply(inst, constructor));
    }

    public static <T extends RagingParticleOptions> StreamCodec<RegistryFriendlyByteBuf, T> createStreamCodec(Function<Float, T> constructor) {
        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale,
                constructor
        );
    }

    public static class StageOne extends RagingParticleOptions {
        public static final MapCodec<StageOne> CODEC = createCodec(RagingParticleOptions::getScale, StageOne::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, StageOne> STREAM_CODEC = createStreamCodec(StageOne::new);

        public StageOne(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_1;
        }
    }

    public static class StageTwo extends RagingParticleOptions {
        public static final MapCodec<StageTwo> CODEC = createCodec(StageTwo::getScale, StageTwo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, StageTwo> STREAM_CODEC = createStreamCodec(StageTwo::new);

        public StageTwo(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_2;
        }
    }

    public static class StageThree extends RagingParticleOptions {
        public static final MapCodec<StageThree> CODEC = createCodec(StageThree::getScale, StageThree::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, StageThree> STREAM_CODEC = createStreamCodec(StageThree::new);

        public StageThree(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_3;
        }
    }

    public static class StageFour extends RagingParticleOptions {
        public static final MapCodec<StageFour> CODEC = createCodec(StageFour::getScale, StageFour::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, StageFour> STREAM_CODEC = createStreamCodec(StageFour::new);

        public StageFour(float size) {
            super(size);
        }

        @Override
        public ParticleType<?> getType() {
            return BnCParticleTypes.RAGING_STAGE_4;
        }
    }
}
