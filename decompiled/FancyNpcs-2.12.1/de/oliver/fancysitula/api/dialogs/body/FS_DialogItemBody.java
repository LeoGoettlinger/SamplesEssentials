package de.oliver.fancysitula.api.dialogs.body;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class FS_DialogItemBody implements FS_DialogBody {
   private ItemStack item;
   @Nullable
   private FS_DialogTextBody description;
   private boolean showDecorations;
   private boolean showTooltip;
   private int width;
   private int height;

   public FS_DialogItemBody(ItemStack item, @Nullable FS_DialogTextBody description, boolean showDecorations, boolean showTooltip, int width, int height) {
      this.item = item;
      this.description = description;
      this.showDecorations = showDecorations;
      this.showTooltip = showTooltip;
      this.width = width;
      this.height = height;
   }

   public ItemStack getItem() {
      return this.item;
   }

   public void setItem(ItemStack item) {
      this.item = item;
   }

   @Nullable
   public FS_DialogTextBody getDescription() {
      return this.description;
   }

   public void setDescription(@Nullable FS_DialogTextBody description) {
      this.description = description;
   }

   public boolean isShowDecorations() {
      return this.showDecorations;
   }

   public void setShowDecorations(boolean showDecorations) {
      this.showDecorations = showDecorations;
   }

   public boolean isShowTooltip() {
      return this.showTooltip;
   }

   public void setShowTooltip(boolean showTooltip) {
      this.showTooltip = showTooltip;
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int width) {
      this.width = width;
   }

   public int getHeight() {
      return this.height;
   }

   public void setHeight(int height) {
      this.height = height;
   }
}
