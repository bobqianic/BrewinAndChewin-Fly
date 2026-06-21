package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.block.LargeKegFootprintBlock;
import umpaz.brewinandchewin.common.registry.BnCBlocks;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow
    @Nullable
    private ClientLevel level;

    @ModifyVariable(method = "destroyBlockProgress", at = @At("HEAD"), argsOnly = true, index = 2)
    private BlockPos brewinandchewin$redirectLargeKegFootprintProgress(BlockPos pos) {
        if (level == null) {
            return pos;
        }

        BlockState state = level.getBlockState(pos);
        if (!state.is(BnCBlocks.LARGE_KEG_FOOTPRINT)) {
            return pos;
        }

        BlockPos kegPos = LargeKegFootprintBlock.getKegPos(pos, state);
        return level.getBlockState(kegPos).is(BnCBlocks.LARGE_KEG) ? kegPos : pos;
    }
}
