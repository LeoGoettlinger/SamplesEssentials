package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Name("Damage Value/Durability")
@Description({"The damage value/durability of an item."})
@Example.Examples({@Example("set damage value of player's tool to 10"), @Example("reset the durability of {_item}"), @Example("set durability of player's held item to 0")})
@Since({"1.2, 2.7 (durability reversed)"})
public class ExprDurability extends SimplePropertyExpression<Object, Integer> {
   private boolean durability;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.durability = parseResult.mark == 1;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Integer convert(Object object) {
      ItemStack itemStack = ItemUtils.asItemStack(object);
      if (itemStack == null) {
         return null;
      } else {
         int damage = ItemUtils.getDamage(itemStack);
         return this.convertToDamage(itemStack, damage);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case ADD:
         case REMOVE:
         case DELETE:
         case RESET:
            return CollectionUtils.array(Number.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int change = delta == null ? 0 : ((Number)delta[0]).intValue();
      if (mode == Changer.ChangeMode.REMOVE) {
         change = -change;
      }

      for (Object object : this.getExpr().getArray(event)) {
         ItemStack itemStack = ItemUtils.asItemStack(object);
         if (itemStack != null) {
            ItemUtils.setDamage(itemStack, this.convertToDamage(itemStack, switch (mode) {
               case SET -> change;
               case ADD, REMOVE -> {
                  int current = this.convertToDamage(itemStack, ItemUtils.getDamage(itemStack));
                  yield current + change;
               }
               default -> 0;
            }));
            if (object instanceof Slot) {
               ((Slot)object).setItem(itemStack);
            } else if (object instanceof ItemType) {
               ((ItemType)object).setItemMeta(itemStack.getItemMeta());
            }
         }
      }
   }

   private int convertToDamage(ItemStack itemStack, int value) {
      if (!this.durability) {
         return value;
      } else {
         int maxDurability = ItemUtils.getMaxDamage(itemStack);
         return maxDurability == 0 ? 0 : maxDurability - value;
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String getPropertyName() {
      return this.durability ? "durability" : "damage";
   }

   static {
      register(ExprDurability.class, Integer.class, "(damage[s] [value[s]]|1:durabilit(y|ies))", "itemtypes/itemstacks/slots");
   }
}
