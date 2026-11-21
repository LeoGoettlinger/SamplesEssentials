package ch.njol.skript.conditions;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.lang.util.SkriptQueue;

@Name("Is Empty")
@Description({"Checks whether an inventory, an inventory slot, a queue, or a text is empty."})
@Example("player's inventory is empty")
@Since({"unknown (before 2.1)"})
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public class CondIsEmpty extends PropertyCondition<Object> {
   @Override
   public boolean check(Object object) {
      if (object instanceof String string) {
         return string.isEmpty();
      } else if (object instanceof SkriptQueue queue) {
         return queue.isEmpty();
      } else if (object instanceof Inventory inventory) {
         for (ItemStack s : inventory.getContents()) {
            if (s != null && s.getType() != Material.AIR) {
               return false;
            }
         }

         return true;
      } else if (!(object instanceof Slot slot)) {
         if (object instanceof AnyAmount numbered) {
            return numbered.isEmpty();
         } else {
            assert false;

            return false;
         }
      } else {
         ItemStack item = slot.getItem();
         return item == null || item.getType() == Material.AIR;
      }
   }

   @Override
   protected String getPropertyName() {
      return "empty";
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         register(CondIsEmpty.class, "empty", "inventories/slots/strings/numbered");
      }
   }
}
