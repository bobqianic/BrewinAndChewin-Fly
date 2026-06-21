package umpaz.brewinandchewin.fabric.container;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import umpaz.brewinandchewin.common.container.AbstractedItemHandler;
import vectorwing.farmersdelight.refabricated.inventory.ItemStackHandler;

public class KegItemHandlerFabric extends ItemStackHandler implements AbstractedItemHandler {
    public KegItemHandlerFabric(int size) {
        super(size);
    }

    public void readFromNbt(CompoundTag tag, HolderLookup.Provider provider) {
        deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }
    public CompoundTag writeToNbt(HolderLookup.Provider provider) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        serialize(output);
        return output.buildResult();
    }
}
