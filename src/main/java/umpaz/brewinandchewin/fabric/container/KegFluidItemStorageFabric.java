package umpaz.brewinandchewin.fabric.container;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.common.container.AbstractedFluidTank;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import umpaz.brewinandchewin.fabric.utility.AmountedFluidVariant;

public class KegFluidItemStorageFabric implements AbstractedFluidTank {
    private final Storage<FluidVariant> storage;
    private final ContainerItemContext context;
    private final SingleVariantStorage<ItemVariant> itemSlot;
    private long capacity = -1L;

    public KegFluidItemStorageFabric(ItemStack stack) {
        itemSlot = new SingleVariantStorage<>() {
            @Override
            protected ItemVariant getBlankVariant() {
                return ItemVariant.blank();
            }

            @Override
            protected long getCapacity(ItemVariant variant) {
                return stack.getMaxStackSize();
            }
        };
        itemSlot.variant = ItemVariant.of(stack);
        itemSlot.amount = stack.getCount();
        context = ContainerItemContext.ofSingleSlot(itemSlot);
        storage = FluidStorage.ITEM.find(stack, context);
    }

    @Override
    public long getFluidCapacity(int slot) {
        if (capacity < 0) {
            for (StorageView<FluidVariant> view : storage) {
                capacity = view.getCapacity();
                break;
            }
            if (capacity < 0)
                capacity = 0;
        }
        return capacity;
    }


    @Override
    public AbstractedFluidStack getAbstractedFluid() {
        for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
            FluidVariant variant = view.getResource();
            AmountedFluidVariant amounted = new AmountedFluidVariant(variant, view.getAmount(), FluidUnit.DROPLET);
            return new AbstractedFluidStack(variant.getFluid(), view.getAmount(), variant.getComponentMap(), FluidUnit.DROPLET, amounted);
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public void setAbstractedFluid(AbstractedFluidStack stack) {
        if (stack.isEmpty())
            return;

        if (storage.supportsInsertion()) {
            try (Transaction t = Transaction.openOuter()) {
                FluidVariant variant = getVariant(stack);
                if (variant.isBlank())
                    return;
                storage.insert(variant, stack.unit().convertToLoader(stack.amount()), t);
                t.commit();
            }
        }
    }

    @Override
    public AbstractedFluidStack fill(AbstractedFluidStack stack, boolean simulate) {
        if (stack.isEmpty())
            return AbstractedFluidStack.EMPTY;

        if (storage.supportsInsertion()) {
            try (Transaction t = Transaction.openOuter()) {
                FluidVariant variant = getVariant(stack);
                long amount = stack.unit().convertToLoader(stack.amount());
                if (variant.isBlank() || amount <= 0)
                    return AbstractedFluidStack.EMPTY;
                long newFill = storage.insert(variant, amount, t);
                if (!simulate)
                    t.commit();
                if (newFill <= 0)
                    return AbstractedFluidStack.EMPTY;
                return new AbstractedFluidStack(variant.getFluid(), newFill, variant.getComponentMap(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, newFill, FluidUnit.DROPLET));
            }
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public AbstractedFluidStack drain(int slot, long maxDrain, FluidUnit unit, boolean simulate) {
        if (storage.supportsExtraction()) {
            try (Transaction t = Transaction.openOuter()) {
                StorageView<FluidVariant> fluidView = null;
                for (StorageView<FluidVariant> view : storage.nonEmptyViews()) {
                    fluidView = view;
                    break;
                }
                if (fluidView == null)
                    return AbstractedFluidStack.EMPTY;

                FluidVariant variant = fluidView.getResource();
                long extractedAmount = storage.extract(variant, unit.convertToLoader(maxDrain), t);
                AbstractedFluidStack stack = new AbstractedFluidStack(variant.getFluid(), extractedAmount, variant.getComponentMap(), FluidUnit.DROPLET, new AmountedFluidVariant(variant, extractedAmount, FluidUnit.DROPLET));
                if (!simulate)
                    t.commit();
                return stack;
            }
        }
        return AbstractedFluidStack.EMPTY;
    }

    @Override
    public ItemStack getContainer() {
        if (context.getItemVariant().isBlank())
            return ItemStack.EMPTY;
        return context.getItemVariant().toStack((int) context.getAmount());
    }

    @Override
    public boolean isEmpty() {
        return !storage.nonEmptyIterator().hasNext();
    }

    @Override
    public boolean isFluidValid(int slot, AbstractedFluidStack stack) {
        if (stack.isEmpty() || !storage.supportsInsertion())
            return false;

        try (Transaction t = Transaction.openOuter()) {
            FluidVariant variant = getVariant(stack);
            if (variant.isBlank())
                return false;
            return storage.insert(variant, getFluidCapacity(), t) > 0;
        }
    }

    private FluidVariant getVariant(AbstractedFluidStack stack) {
        return FluidVariant.of(stack.fluid(), stack.components() instanceof PatchedDataComponentMap patched ? patched.asPatch() : DataComponentPatch.EMPTY);
    }
}
