package umpaz.brewinandchewin.common.mixin;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.storage.loot.LootParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LootParams.class)
public interface LootParamsAccessor {
    @Accessor("params")
    ContextMap brewinandchewin$getParams();
}
