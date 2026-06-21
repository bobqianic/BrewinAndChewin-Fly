package umpaz.brewinandchewin.common;

import net.minecraft.world.inventory.RecipeBookType;
import org.jetbrains.annotations.Nullable;

public class BnCRecipeBookTypes {
    private static final String FERMENTING_NAME = "BREWINANDCHEWIN_FERMENTING";
    @Nullable
    private static final RecipeBookType FERMENTING = resolve(FERMENTING_NAME);

    @Nullable
    public static RecipeBookType fermenting() {
        return FERMENTING;
    }

    public static RecipeBookType fermentingOrFallback() {
        return FERMENTING != null ? FERMENTING : RecipeBookType.CRAFTING;
    }

    public static boolean hasFermenting() {
        return FERMENTING != null;
    }

    @Nullable
    private static RecipeBookType resolve(String name) {
        try {
            return RecipeBookType.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
