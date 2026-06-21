package umpaz.brewinandchewin.common.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.entity.player.StackedContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
    @Accessor("stackedContents") @Mutable @Final
    void brewinandchewin$setStackedContents(StackedContents value);
}
