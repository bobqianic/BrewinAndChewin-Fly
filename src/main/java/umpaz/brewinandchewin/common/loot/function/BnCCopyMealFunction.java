package umpaz.brewinandchewin.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;

import java.util.List;

public class BnCCopyMealFunction extends LootItemConditionalFunction {
    public static final MapCodec<BnCCopyMealFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).apply(inst, BnCCopyMealFunction::new));

    public static final ResourceLocation ID = BrewinAndChewin.asResource("copy_meal");
    public static final LootItemFunctionType<BnCCopyMealFunction> TYPE = new LootItemFunctionType<>(CODEC);

    private BnCCopyMealFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(BnCCopyMealFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity tile = context.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof KegBlockEntity kegTile) {
            TypedEntityData<?> existingData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag tag = existingData != null ? existingData.copyTagWithoutId() : new CompoundTag();
            kegTile.writePreservedData(tag, context.getLevel().registryAccess());
            if (tag.isEmpty()) {
                stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            } else {
                stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BnCBlockEntityTypes.KEG, tag));
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType<BnCCopyMealFunction> getType() {
        return TYPE;
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        Builder() {
        }

        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new BnCCopyMealFunction(getConditions());
        }
    }
}
