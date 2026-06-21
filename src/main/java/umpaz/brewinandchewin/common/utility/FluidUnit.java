package umpaz.brewinandchewin.common.utility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import java.util.function.Function;
import java.util.function.IntFunction;

public enum FluidUnit implements StringRepresentable {
    LITER("liters", l -> l + " L", l -> l + " liters",1),
    MILLIBUCKET("millibuckets", l -> l + " mB", l -> l + " millibuckets",1),
    DROPLET("droplets", l -> l + " d", l -> l + " droplets",81);

    private final String name;
    private final Function<String, String> shortFormFormatFunc;
    private final Function<String, String> longFormFormatFunc;
    private final long oneL;

    public static final Codec<FluidUnit> CODEC = StringRepresentable.fromEnum(FluidUnit::values);
    public static final IntFunction<FluidUnit> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, FluidUnit> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    FluidUnit(String name, Function<String, String> shortFormFormatFunc, Function<String, String> longFormFormatFunc, long oneL) {
        this.name = name;
        this.shortFormFormatFunc = shortFormFormatFunc;
        this.longFormFormatFunc = longFormFormatFunc;
        this.oneL = oneL;
    }

    public long convert(long value, FluidUnit unit) {
        return convert(value, this, unit);
    }

    public long convertToLoader(long value) {
        return convertToLoader(value, this);
    }

    public static FluidUnit getOpposite(FluidUnit unit) {
        if (unit == DROPLET)
            return LITER;
        return DROPLET;
    }

    public static FluidUnit getLoaderUnit() {
        return DROPLET;
    }

    public static long convertToLoader(long value, FluidUnit unit) {
        return convert(value, unit, getLoaderUnit());
    }

    public static long convert(long value, FluidUnit originalUnit, FluidUnit newUnit) {
        if (originalUnit.oneL == newUnit.oneL)
            return value;
        return value / originalUnit.oneL * newUnit.oneL;
    }

    public String shortFormat(String value) {
        return shortFormFormatFunc.apply(value);
    }

    public String longFormat(String value) {
        return longFormFormatFunc.apply(value);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
