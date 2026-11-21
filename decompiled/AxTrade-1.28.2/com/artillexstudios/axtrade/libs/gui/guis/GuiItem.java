package com.artillexstudios.axtrade.libs.gui.guis;

import com.artillexstudios.axtrade.libs.gui.components.GuiAction;
import com.artillexstudios.axtrade.libs.gui.components.util.ItemNbt;
import com.google.common.base.Preconditions;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiItem {
   private final UUID uuid = UUID.randomUUID();
   private GuiAction<InventoryClickEvent> action;
   private ItemStack itemStack;

   public GuiItem(@NotNull ItemStack itemStack, @Nullable GuiAction<InventoryClickEvent> action) {
      Preconditions.checkNotNull(itemStack, "The ItemStack for the GUI Item cannot be null!");
      this.action = action;
      this.setItemStack(itemStack);
   }

   public GuiItem(@NotNull ItemStack itemStack) {
      this(itemStack, null);
   }

   public GuiItem(@NotNull Material material) {
      this(new ItemStack(material), null);
   }

   public GuiItem(@NotNull Material material, @Nullable GuiAction<InventoryClickEvent> action) {
      this(new ItemStack(material), action);
   }

   @NotNull
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public void setItemStack(@NotNull ItemStack itemStack) {
      Preconditions.checkNotNull(itemStack, "The ItemStack for the GUI Item cannot be null!");
      if (itemStack.getType() != Material.AIR) {
         this.itemStack = ItemNbt.setString(itemStack.clone(), "mf-gui", this.uuid.toString());
      } else {
         this.itemStack = itemStack.clone();
      }
   }

   @Nullable
   public GuiAction<InventoryClickEvent> getAction() {
      return this.action;
   }

   public void setAction(@Nullable GuiAction<InventoryClickEvent> action) {
      this.action = action;
   }

   @NotNull
   UUID getUuid() {
      return this.uuid;
   }
}
