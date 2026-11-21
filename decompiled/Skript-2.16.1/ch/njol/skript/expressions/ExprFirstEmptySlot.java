package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("First Empty Slot in Inventory")
@Description({"Returns the first empty slot in an inventory. If no empty slot is found, it returns nothing."})
@Example.Examples({@Example("set the first empty slot in player's inventory to 5 diamonds"), @Example("if the first empty slot in player's inventory is not set:\n\tmessage \"No empty slot available in your inventory!\" to player\n")})
@Since({"2.12"})
@Keywords({"full", "inventory", "empty", "air", "slot"})
public class ExprFirstEmptySlot extends SimplePropertyExpression<Inventory, Slot> {
   @Nullable
   public Slot convert(Inventory from) {
      int slotIndex = from.firstEmpty();
      return slotIndex == -1 ? null : new InventorySlot(from, slotIndex);
   }

   @Override
   public Class<? extends Slot> getReturnType() {
      return Slot.class;
   }

   @Override
   protected String getPropertyName() {
      return "first empty slot";
   }

   static {
      List<String> patterns = new ArrayList<>(Arrays.asList(getPatterns("first empty slot[s]", "inventories")));
      patterns.add("[the] first empty slot[s] in %inventories%");
      Skript.registerExpression(ExprFirstEmptySlot.class, Slot.class, ExpressionType.PROPERTY, patterns.toArray(new String[0]));
   }
}
