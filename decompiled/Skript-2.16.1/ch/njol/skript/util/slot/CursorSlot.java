package ch.njol.skript.util.slot;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CursorSlot extends Slot {
   @Nullable
   private final ItemStack eventItemStack;
   private final Player player;

   public CursorSlot(Player player) {
      this(player, null);
   }

   public CursorSlot(Player player, @Nullable ItemStack eventItemStack) {
      this.eventItemStack = eventItemStack;
      this.player = player;
   }

   public Player getPlayer() {
      return this.player;
   }

   @Nullable
   @Override
   public ItemStack getItem() {
      return this.eventItemStack != null ? this.eventItemStack : this.player.getItemOnCursor();
   }

   @Override
   public void setItem(@Nullable ItemStack item) {
      this.player.setItemOnCursor(item);
      PlayerUtils.updateInventory(this.player);
   }

   @Override
   public int getAmount() {
      return this.getItem().getAmount();
   }

   @Override
   public void setAmount(int amount) {
      this.getItem().setAmount(amount);
   }

   public boolean isInventoryClick() {
      return this.eventItemStack != null;
   }

   @Override
   public boolean isSameSlot(Slot slot) {
      return slot instanceof CursorSlot cursorSlot && cursorSlot.getPlayer().equals(this.player) && cursorSlot.isInventoryClick() == this.isInventoryClick();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "cursor slot of " + Classes.toString(this.player);
   }
}
