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

public class CopyDrinkFunction extends LootItemConditionalFunction
{
    public static final MapCodec<CopyDrinkFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).apply(inst, CopyDrinkFunction::new));

    public static final Identifier ID = BrewinAndChewin.asResource("copy_drink");

    private CopyDrinkFunction(Optional<Holder<LootItemCondition>> condition) {
        super(condition);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyDrinkFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        BlockEntity tile = context.getOptional(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof KegBlockEntity kegTile) {
            TypedEntityData<?> existingData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            CompoundTag tag = kegTile.writePreservedData(existingData != null ? existingData.copyTagWithoutId() : new CompoundTag(), context.getLevel().registryAccess());
            if (!tag.isEmpty()) {
                stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BnCBlockEntityTypes.KEG, tag));
            }
        }
        return stack;
    }

    @Override
    public MapCodec<CopyDrinkFunction> codec() {
        return CODEC;
    }

}
