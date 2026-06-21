package umpaz.brewinandchewin.common.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class BnCCompatTags {

    public static final TagKey<Item> FOODS_EDIBLE_WHEN_PLACED = compatItemTag("c", "foods/edible_when_placed");
    public static final TagKey<Item> ORIGINS_IGNORE_DIET = compatItemTag("origins", "ignore_diet");
    public static final TagKey<Item> ORIGINS_MEAT = compatItemTag("origins", "meat");
    public static final TagKey<Fluid> HONEY_FLUID = compatFluidTag("c", "honey");

    private static TagKey<Item> compatItemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static TagKey<Fluid> compatFluidTag(String namespace, String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
