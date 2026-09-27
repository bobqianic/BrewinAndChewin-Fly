package umpaz.brewinandchewin.common.loot.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;
import umpaz.brewinandchewin.common.registry.BnCBlockEntityTypes;

import java.util.Optional;

public class BnCCopyMealFunction extends LootItemConditionalFunction {
    public static final MapCodec<BnCCopyMealFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).apply(inst, BnCCopyMealFunction::new));

    public static final Identifier ID = BrewinAndChewin.asResource("copy_meal");

    private BnCCopyMealFunction(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(BnCCopyMealFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity tile = context.getOptional(LootContextParams.BLOCK_ENTITY);
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
    public MapCodec<BnCCopyMealFunction> codec() {
        return CODEC;
    }

}
