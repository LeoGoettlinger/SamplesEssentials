package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Item Amount")
@Description({"The amount of an <a href='#itemstack'>item stack</a>."})
@Example("send \"You have got %item amount of player's tool% %player's tool% in your hand!\" to player")
@Since({"2.2-dev24"})
public class ExprItemAmount extends SimplePropertyExpression<Object, Long> {
   public Long convert(Object item) {
      if (item instanceof ItemType) {
         return (long)((ItemType)item).getAmount();
      } else {
         return item instanceof Slot ? (long)((Slot)item).getAmount() : (long)((ItemStack)item).getAmount();
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case ADD:
         case RESET:
         case DELETE:
         case REMOVE:
            return CollectionUtils.array(Long.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int amount = delta != null ? ((Number)delta[0]).intValue() : 0;
      switch (mode) {
         case RESET:
         case DELETE:
            amount = 1;
         case SET:
            for (Object obj : this.getExpr().getArray(event)) {
               if (obj instanceof ItemType) {
                  ((ItemType)obj).setAmount(amount);
               } else if (obj instanceof Slot) {
                  ((Slot)obj).setAmount(amount);
               } else {
                  ((ItemStack)obj).setAmount(amount);
               }
            }
            break;
         case REMOVE:
            amount = -amount;
         case ADD:
            for (Object objx : this.getExpr().getArray(event)) {
               if (objx instanceof ItemType item) {
                  item.setAmount(item.getAmount() + amount);
               } else if (objx instanceof Slot slot) {
                  slot.setAmount(slot.getAmount() + amount);
               } else {
                  ItemStack item = (ItemStack)objx;
                  item.setAmount(item.getAmount() + amount);
               }
            }
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "item amount";
   }

   static {
      register(ExprItemAmount.class, Long.class, "item[[ ]stack] (amount|size|number)", "slots/itemtypes/itemstacks");
   }
}
