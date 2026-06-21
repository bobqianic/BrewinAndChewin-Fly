package umpaz.brewinandchewin.fabric.container;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import umpaz.brewinandchewin.common.block.entity.container.SidedKegWrapper;
import vectorwing.farmersdelight.refabricated.inventory.ItemHandler;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

public class SidedKegWrapperFabric extends SidedKegWrapper implements ItemHandler {
    public SidedKegWrapperFabric(AbstractedItemHandler itemHandler, @Nullable Direction side) {
        super(itemHandler, side);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return ((ItemHandler)itemHandler).getSlot(slot);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long inserted = 0;
        Iterator<SingleStackStorage> itr = getInsertableSlotsFor(resource);
        while (itr.hasNext()) {
            SingleStackStorage slot = itr.next();
            inserted += slot.insert(resource, maxAmount - inserted, transaction);
            if (inserted >= maxAmount)
                break;
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long extracted = 0;
        for (SingleStackStorage slot : getSlotsContaining(resource, true)) {
            extracted += slot.extract(resource, maxAmount - extracted, transaction);
            if (extracted >= maxAmount)
                break;
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        ItemStackHandler handler = ((ItemStackHandler)itemHandler);
        return side != null && !side.equals(Direction.UP) ?
                handler.getSlots().subList(4, 6).stream()
                        .map(storageView -> (StorageView<ItemVariant>)storageView)
                        .iterator() :
                handler.getSlots().subList(0, 4).stream()
                        .map(storageView -> (StorageView<ItemVariant>)storageView)
                        .iterator();
    }

    public SortedSet<SingleStackStorage> getSlotsContaining(ItemVariant resource, boolean output) {
        ItemStackHandler handler = ((ItemStackHandler)itemHandler);
        List<SingleSlotStorage<ItemVariant>> slots = side != null && !side.equals(Direction.UP) ?
                List.of(output ? handler.getSlots().get(5) : handler.getSlots().get(4)) :
                handler.getSlots().subList(0, 4);
        return slots.stream()
                .map(storageView -> (SingleStackStorage)storageView).filter(storageView -> storageView.getResource().equals(resource))
                .collect(Collectors.toCollection(ObjectLinkedOpenHashSet::new));
    }

    public Iterator<SingleStackStorage> getInsertableSlotsFor(ItemVariant resource) {
        ItemStackHandler handler = ((ItemStackHandler)itemHandler);
        List<SingleSlotStorage<ItemVariant>> slots = side != null && !side.equals(Direction.UP) ?
                List.of(handler.getSlots().get(4)) :
                handler.getSlots().subList(0, 4);
        return slots.stream()
                .filter((views) -> views.isResourceBlank() || views.getResource().equals(resource))
                .map(storageView -> (SingleStackStorage)storageView).filter(storageView -> storageView.getResource().equals(resource))
                .iterator();
    }

    private boolean isValidSlot(int slot, boolean extract) {
        if (side == null || side.equals(Direction.UP)) {
            return slot < 3;
        } else {
            return extract ? slot == 5 : slot == 4;
        }
    }
}
