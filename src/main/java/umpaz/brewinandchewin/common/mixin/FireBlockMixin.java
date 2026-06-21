package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.block.KegBlock;
import umpaz.brewinandchewin.common.block.entity.KegBlockEntity;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @ModifyVariable(method = "getBurnOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), argsOnly = true)
    private BlockState brewinandchewin$filledKegsDoNotBurn(BlockState state) {
        if (state.hasProperty(KegBlock.HAS_LIQUID) && state.getValue(KegBlock.HAS_LIQUID)) {
            return state.setValue(KegBlock.WATERLOGGED, true);
        }
        return state;
    }

    @ModifyVariable(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), argsOnly = true)
    private BlockState brewinandchewin$filledKegsDoNotIgnite(BlockState state) {
        if (state.hasProperty(KegBlock.HAS_LIQUID) && state.getValue(KegBlock.HAS_LIQUID)) {
            return state.setValue(KegBlock.WATERLOGGED, true);
        }
        return state;
    }

    @WrapOperation(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean brewinandchewin$markBurnedKeg(Level level, BlockPos pos, boolean moving, Operation<Boolean> original) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof KegBlockEntity keg) {
            keg.markBurnedByFire();
        }
        return original.call(level, pos, moving);
    }
}
