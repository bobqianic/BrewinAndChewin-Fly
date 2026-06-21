package umpaz.brewinandchewin.fabric.container;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.fabric.utility.AmountedFluidVariant;

public class KegFluidTankFabric extends SingleFluidStorage implements AbstractedFluidTank {
    private static final long LEGACY_TRANSFER_GRANULARITY = 250L;
    private static final long TINY_FLUID_REMAINDER = FluidUnit.MILLIBUCKET.convertToLoader(1L);

    private final long capacity;

    public KegFluidTankFabric(long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return getFluidCapacity();
    }

    @Override
    public long getFluidCapacity(int slot) {
        return capacity;
    }

    @Override
    public AbstractedFluidStack getAbstractedFluid() {
        normalizeStorageState();
        return new AbstractedFluidStack(variant.getFluid(), getAmount(), variant.getComponentMap(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, getAmount(), FluidUnit.DROPLET));
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        if (variant.isBlank()) {
            amount = 0;
        } else if (getAmount() <= 0) {
            variant = FluidVariant.blank();
        }
    }

    @Override
    public void setAbstractedFluid(AbstractedFluidStack stack) {
        normalizeStorageState();
        try (Transaction t = Transaction.openOuter()) {
            if (!variant.isBlank()) {
                extract(variant, capacity, t);
            }
            if (stack.isEmpty()) {
                t.commit();
                return;
            }
            AmountedFluidVariant variant = unwrapFluid(stack);
            insert(variant.variant(), FluidUnit.convertToLoader(stack.amount(), stack.unit()), t);
            t.commit();
        }
    }

    @Override
    public AbstractedFluidStack fill(AbstractedFluidStack fluidStack, boolean simulate) {
        normalizeStorageState();
        if (fluidStack.isEmpty())
            return AbstractedFluidStack.EMPTY;

        long newAmount = fluidStack.unit().convertToLoader(fluidStack.amount());
        try {
            Transaction t = Transaction.openOuter();
            FluidVariant variant = FluidVariant.of(fluidStack.fluid(), fluidStack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY);
            long newFill = insert(variant, newAmount, t);
            if (!simulate)
                t.commit();
            t.close();
            return new AbstractedFluidStack(variant.getFluid(), newFill, variant.getComponentMap(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, newFill, FluidUnit.DROPLET));
        } catch (Exception e) {
            BrewinAndChewin.LOG.error("Failed to fill keg with {} of fluid {}.", fluidStack.fluid(), fluidStack.unit().shortFormat(String.valueOf(fluidStack.unit().convertToLoader(fluidStack.amount()))));
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
        normalizeStorageState();
        long newMax = unit.convertToLoader(maxDrain);
        try {
            Transaction t = Transaction.openOuter();
            FluidVariant extractedVariant = variant;
            long extractedAmount = extract(extractedVariant, newMax, t);
            AbstractedFluidStack stack = new AbstractedFluidStack(extractedVariant.getFluid(), extractedAmount, extractedVariant.getComponentMap(), FluidUnit.DROPLET, new AmountedFluidVariant(extractedVariant, extractedAmount, FluidUnit.DROPLET));
            if (!simulate)
                t.commit();
            t.close();
            if (!simulate)
                normalizeStorageState();
            return stack;
        } catch (Exception e) {
            BrewinAndChewin.LOG.error("Failed to extract {} from keg.", unit.shortFormat(String.valueOf(unit.convertToLoader(maxDrain))));
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        readData(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
        normalizeLoadedAmount();
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider provider) {
        normalizeStorageState();
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        writeData(output);
        return output.buildResult();
    }

    @Override
    public boolean isEmpty() {
        normalizeStorageState();
        return isResourceBlank() || getAmount() <= 0;
    }

    private AmountedFluidVariant unwrapFluid(AbstractedFluidStack stack)  {
        if (stack.loaderSpecific() instanceof AmountedFluidVariant fluidVariant)
            return fluidVariant;

        if (stack.isEmpty())
            return AmountedFluidVariant.EMPTY;

        return new AmountedFluidVariant(FluidVariant.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY), stack.unit().convertToLoader(stack.amount()), FluidUnit.DROPLET);
    }

    private void normalizeLoadedAmount() {
        if (normalizeStorageState()) {
            return;
        }

        if (amount < TINY_FLUID_REMAINDER) {
            amount = 0;
            variant = FluidVariant.blank();
            return;
        }

        long legacyCapacity = FluidUnit.convert(capacity, FluidUnit.getLoaderUnit(), FluidUnit.MILLIBUCKET);
        if (amount <= legacyCapacity && amount % LEGACY_TRANSFER_GRANULARITY == 0) {
            amount = Math.min(capacity, FluidUnit.MILLIBUCKET.convertToLoader(amount));
        }
    }

    private boolean normalizeStorageState() {
        if (variant.isBlank() || amount <= 0) {
            amount = 0;
            variant = FluidVariant.blank();
            return true;
        }
        return false;
    }
}
