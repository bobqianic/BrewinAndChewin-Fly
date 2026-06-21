package umpaz.brewinandchewin.common.loot.condition;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.access.LootParamsParamSetAccess;
import umpaz.brewinandchewin.common.mixin.LootContextAccessor;
import umpaz.brewinandchewin.common.mixin.LootParamsAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class AreaLocationCheckCondition implements LootItemCondition {
    public static final MapCodec<AreaLocationCheckCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LootItemCondition.DIRECT_CODEC.listOf().fieldOf("terms").forGetter(cond -> cond.terms),
            ExtraCodecs.POSITIVE_INT.fieldOf("range").forGetter(cond -> cond.range)
    ).apply(inst, AreaLocationCheckCondition::new));

    public static final ResourceLocation ID = BrewinAndChewin.asResource("area_location_check");
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    protected final List<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;
    private final int range;

    protected AreaLocationCheckCondition(List<LootItemCondition> predicates, int range) {
        this.terms = predicates;
        this.composedPredicate = Util.allOf(terms);
        this.range = range;
    }

    public LootItemConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        Vec3 vec3 = context.getOptionalParameter(LootContextParams.ORIGIN);
        if (vec3 == null) {
            return false;
        }
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    Vec3 offset = vec3.add(x, y, z);
                    LootParams.Builder paramBuilder = new LootParams.Builder(context.getLevel());
                    LootParams originalParams = ((LootContextAccessor)context).brewinandchewin$getParams();
                    for (ContextKey<?> key : ((LootParamsParamSetAccess) originalParams).brewinandchewin$getParamSet().allowed()) {
                        copyParameter(paramBuilder, originalParams, key);
                    }
                    paramBuilder.withParameter(LootContextParams.ORIGIN, offset);
                    if (context.hasParameter(LootContextParams.BLOCK_STATE))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_STATE, context.getLevel().getBlockState(BlockPos.containing(offset)));
                    if (context.hasParameter(LootContextParams.BLOCK_ENTITY))
                        paramBuilder.withOptionalParameter(LootContextParams.BLOCK_ENTITY, context.getLevel().getBlockEntity(BlockPos.containing(offset)));
                    LootContext newCtx = new LootContext.Builder(paramBuilder.create(((LootParamsParamSetAccess) originalParams).brewinandchewin$getParamSet())).create(Optional.empty());
                    if (composedPredicate.test(newCtx))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN);
    }

    @Override
    public void validate(ValidationContext pContext) {
        LootItemCondition.super.validate(pContext);

        for(int i = 0; i < terms.size(); ++i)
            this.terms.get(i).validate(pContext.forChild(new ProblemReporter.IndexedFieldPathElement("term", i)));

    }

    public static LootItemCondition.Builder checkArea(int range, LootItemCondition.Builder... predicateBuilder) {
        return () -> new AreaLocationCheckCondition(Arrays.stream(predicateBuilder).map(Builder::build).toList(), range);
    }

    private static <T> void copyParameter(LootParams.Builder paramBuilder, LootParams originalParams, ContextKey<T> key) {
        T value = ((LootParamsAccessor) originalParams).brewinandchewin$getParams().getOptional(key);
        if (value != null) {
            paramBuilder.withParameter(key, value);
        }
    }
}
