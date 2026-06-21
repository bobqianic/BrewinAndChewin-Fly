package umpaz.brewinandchewin.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;
import umpaz.brewinandchewin.common.registry.BnCBlocks;

import java.util.function.Supplier;

public class IceCrateBlock extends Block {
    private static final VoxelShape FOG_AREA = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    private static final int MIN_COOL_DELAY = 8;
    private static final int MAX_COOL_DELAY = 30;
    private static final int TEMPORARY_WATER_CLEANUP_DELAY = 20;

    public IceCrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        scheduleCoolingTick(level, pos);
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.is(BnCBlocks.TEMPORARY_WATER) || belowState.is(Blocks.WATER)) {
            level.removeBlock(belowPos, false);
        } else {
            coolBlockBelow(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        scheduleCoolingTick(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        if (facing == Direction.DOWN) {
            if (isCoolableHazard(facingState)) {
                scheduledTickAccess.scheduleTick(currentPos, this, Mth.nextInt(random, MIN_COOL_DELAY, MAX_COOL_DELAY));
            } else if (facingState.is(BnCBlocks.TEMPORARY_WATER) || facingState.is(Blocks.WATER)) {
                scheduledTickAccess.scheduleTick(currentPos, this, TEMPORARY_WATER_CLEANUP_DELAY);
            }
        }
        return super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0 && isSpaceAbove(level, pos)) {
            Supplier<Vec3> supplier = () -> new Vec3(Mth.nextDouble(random, -0.005F, 0.005F), Mth.nextDouble(random, -0.025F, -0.01F), Mth.nextDouble(random, -0.005F, 0.005F));
            for (Direction direction : Direction.values()) {
                ParticleUtils.spawnParticlesOnBlockFace(level, pos, BnCParticleTypes.FOG, (UniformInt.of(1, 1)), direction, supplier, (direction == Direction.UP ? 0.25D : 0.55D));
            }
        }
    }

    public boolean isSpaceAbove(Level level, BlockPos pos) {
        if (level != null) {
            BlockState above = level.getBlockState(pos.above());
            return !Shapes.joinIsNotEmpty(FOG_AREA, above.getShape(level, pos.above()), BooleanOp.AND);
        } else {
            return true;
        }
    }

    private void coolBlockBelow(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (belowState.is(Blocks.FIRE) || belowState.is(Blocks.SOUL_FIRE)) {
                level.setBlockAndUpdate(belowPos, BnCBlocks.TEMPORARY_WATER.defaultBlockState());
                level.scheduleTick(pos, this, TEMPORARY_WATER_CLEANUP_DELAY);
                level.playSound(null, belowPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.0F);
            } else if (belowState.getFluidState().is(FluidTags.LAVA)) {
                level.setBlockAndUpdate(belowPos, getCooledLavaState(belowState));
                level.playSound(null, belowPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }

    private BlockState getCooledLavaState(BlockState state) {
        return state.getFluidState().isSource() ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
    }

    private void scheduleCoolingTick(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            BlockState belowState = level.getBlockState(pos.below());
            if (isCoolableHazard(belowState)) {
                level.scheduleTick(pos, this, Mth.nextInt(level.getRandom(), MIN_COOL_DELAY, MAX_COOL_DELAY));
            } else if (belowState.is(BnCBlocks.TEMPORARY_WATER) || belowState.is(Blocks.WATER)) {
                level.scheduleTick(pos, this, TEMPORARY_WATER_CLEANUP_DELAY);
            }
        }
    }

    private boolean isCoolableHazard(BlockState state) {
        return state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.getFluidState().is(FluidTags.LAVA);
    }
}
