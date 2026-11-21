package ch.njol.skript.util.slot;

import ch.njol.skript.registrations.Classes;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DroppedItemSlot extends Slot {
   private final Item entity;

   public DroppedItemSlot(Item item) {
      this.entity = item;
   }

   @Nullable
   @Override
   public ItemStack getItem() {
      return this.entity.getItemStack();
   }

   @Override
   public void setItem(@Nullable ItemStack item) {
      assert item != null;

      this.entity.setItemStack(item);
   }

   @Override
   public int getAmount() {
      return this.entity.getItemStack().getAmount();
   }

   @Override
   public void setAmount(int amount) {
      this.entity.getItemStack().setAmount(amount);
   }

   public Item getItemEntity() {
      return this.entity;
   }

   @Override
   public boolean isSameSlot(Slot slot) {
      return slot instanceof DroppedItemSlot droppedItemSlot && droppedItemSlot.getItemEntity().equals(this.entity);
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return Classes.toString(this.getItem());
   }
}
