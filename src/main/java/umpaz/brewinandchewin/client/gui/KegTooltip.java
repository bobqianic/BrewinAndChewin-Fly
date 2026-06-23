package umpaz.brewinandchewin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.client.utility.BnCFluidItemDisplays;
import umpaz.brewinandchewin.common.utility.AbstractedFluidStack;
import umpaz.brewinandchewin.common.utility.FluidUnit;
import vectorwing.farmersdelight.common.utility.TextUtils;

public class KegTooltip implements ClientTooltipComponent {
   private static final int ITEM_SIZE = 16;
   private static final int MARGIN = 4;
   private static final int TOOLTIP_GRAY = 0xFFAAAAAA;

   private final int textSpacing = Minecraft.getInstance().font.lineHeight + 1;
   private final AbstractedFluidStack mealStack;
   private final long capacity;

   public KegTooltip(KegTooltipComponent tooltip) {
      this.mealStack = tooltip.mealStack;
      this.capacity = tooltip.capacity;
   }

   @Override
   public int getHeight(Font font) {
      return mealStack.isEmpty() ? textSpacing : textSpacing + ITEM_SIZE;
   }

   @Override
   public int getWidth( Font font ) {
      if ( !mealStack.isEmpty() ) {
         return Math.max(font.width(getAmountText()), font.width(BrewinAndChewin.getHelper().getFluidDisplayName(mealStack)) + ITEM_SIZE + MARGIN);
      }
      else {
         return font.width(TextUtils.getTranslation("tooltip.cooking_pot.empty"));
      }
   }

   @Override
   public void renderImage(Font font, int mouseX, int mouseY, int width, int height, GuiGraphics gui) {
      if (mealStack.isEmpty()) return;

      ItemStack itemDisplay = BnCFluidItemDisplays.getFluidItemDisplay(Minecraft.getInstance().level.registryAccess(), mealStack);
      gui.renderItem(itemDisplay, mouseX, mouseY + textSpacing);
   }

   @Override
   public void renderText(GuiGraphics gui, Font font, int x, int y) {
      if (!mealStack.isEmpty()) {
         gui.drawString(font, Component.literal(getAmountText()), x, y, TOOLTIP_GRAY);
         gui.drawString(font, BrewinAndChewin.getHelper().getFluidDisplayName(mealStack), x + ITEM_SIZE + MARGIN, y + textSpacing + MARGIN, -1);
      }
      else {
         MutableComponent textEmpty = TextUtils.getTranslation("tooltip.cooking_pot.empty");
         gui.drawString(font, textEmpty, x, y, TOOLTIP_GRAY);
      }
   }

   private String getAmountText() {
      long amount = mealStack.unit().convert(mealStack.amount(), FluidUnit.MILLIBUCKET);
      long displayedCapacity = FluidUnit.convert(capacity, FluidUnit.getLoaderUnit(), FluidUnit.MILLIBUCKET);
      return FluidUnit.MILLIBUCKET.shortFormat(amount + "/" + displayedCapacity);
   }

   public record KegTooltipComponent(AbstractedFluidStack mealStack, long capacity) implements TooltipComponent {}
}
