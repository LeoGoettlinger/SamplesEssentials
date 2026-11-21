package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Item")
@Description({"The item involved in an event, e.g. in a drop, dispense, pickup or craft event."})
@Example("on dispense:\n\titem is a clock\n\tset the time to 6:00\n")
@Since({"unknown (before 2.1)"})
public class ExprItem extends EventValueExpression<ItemStack> {
   @Nullable
   private EventValueExpression<Item> item;
   @Nullable
   private EventValueExpression<Slot> slot;

   public ExprItem() {
      super(ItemStack.class);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.RESET) {
         return null;
      } else {
         this.item = new EventValueExpression<>(Item.class);
         if (this.item.init()) {
            return new Class[]{ItemType.class};
         } else {
            this.item = null;
            this.slot = new EventValueExpression<>(Slot.class);
            if (this.slot.init()) {
               return new Class[]{ItemType.class};
            } else {
               this.slot = null;
               return null;
            }
         }
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert mode != Changer.ChangeMode.RESET;

      ItemType itemType = delta == null ? null : (ItemType)delta[0];
      Item item = this.item != null ? this.item.getSingle(event) : null;
      Slot slot = this.slot != null ? this.slot.getSingle(event) : null;
      if (item != null || slot != null) {
         ItemStack itemstack = item != null ? item.getItemStack() : (slot != null ? slot.getItem() : null);
         switch (mode) {
            case SET:
               assert itemType != null;

               itemstack = itemType.getRandom();
               break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
               assert itemType != null;

               if (itemType.isOfType(itemstack)) {
                  if (mode == Changer.ChangeMode.ADD) {
                     itemstack = itemType.addTo(itemstack);
                  } else if (mode == Changer.ChangeMode.REMOVE) {
                     itemstack = itemType.removeFrom(itemstack);
                  } else {
                     itemstack = itemType.removeAll(itemstack);
                  }
               }
               break;
            case DELETE:
               itemstack = null;
               if (item != null) {
                  item.remove();
               }
               break;
            case RESET:
               assert false;
         }

         if (item != null && itemstack != null) {
            item.setItemStack(itemstack);
         } else if (slot != null) {
            slot.setItem(itemstack);
         } else {
            assert false;
         }
      }
   }

   static {
      register(ExprItem.class, ItemStack.class, "item");
   }
}
