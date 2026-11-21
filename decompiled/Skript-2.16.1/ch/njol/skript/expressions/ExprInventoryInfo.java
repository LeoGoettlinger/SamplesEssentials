package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

@Name("Inventory Holder/Viewers/Rows/Slots")
@Description({"Gets the amount of rows/slots, viewers and holder of an inventory.", "", "NOTE: 'Viewers' expression returns a list of players viewing the inventory. Note that a player is considered to be viewing their own inventory and internal crafting screen even when said inventory is not open."})
@Example.Examples({@Example("event-inventory's amount of rows"), @Example("holder of player's top inventory"), @Example("{_inventory}'s viewers")})
@Since({"2.2-dev34, 2.5 (slots)"})
public class ExprInventoryInfo extends SimpleExpression<Object> {
   private static final int HOLDER = 1;
   private static final int ROWS = 2;
   private static final int SLOTS = 3;
   private Expression<Inventory> inventories;
   private int type;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.inventories = (Expression<Inventory>)exprs[0];
      this.type = parseResult.mark;
      return true;
   }

   @Override
   protected Object[] get(Event event) {
      Inventory[] inventories = this.inventories.getArray(event);
      switch (this.type) {
         case 1:
            List<InventoryHolder> holders = new ArrayList<>();

            for (Inventory inventoryx : inventories) {
               InventoryHolder holder = inventoryx.getHolder();
               if (holder != null) {
                  holders.add(holder);
               }
            }

            return holders.toArray(new InventoryHolder[0]);
         case 2:
            List<Number> rows = new ArrayList<>();

            for (Inventory inventory : inventories) {
               int size = inventory.getSize();
               if (size < 9) {
                  rows.add(1);
               } else {
                  rows.add(size / 9);
               }
            }

            return rows.toArray(new Number[0]);
         case 3:
            List<Number> sizes = new ArrayList<>();

            for (Inventory inventory : inventories) {
               sizes.add(inventory.getSize());
            }

            return sizes.toArray(new Number[0]);
         default:
            return (Object[])Array.newInstance(this.getReturnType(), 0);
      }
   }

   @Override
   public boolean isSingle() {
      return this.inventories.isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.type == 1 ? InventoryHolder.class : Number.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return (this.type == 1 ? "holder of " : (this.type == 2 ? "rows of " : "slots of ")) + this.inventories.toString(e, debug);
   }

   static {
      Skript.registerExpression(
         ExprInventoryInfo.class,
         Object.class,
         ExpressionType.PROPERTY,
         "(1¦holder[s]|2¦[amount of] rows|3¦[amount of] slots) of %inventories%",
         "%inventories%'[s] (1¦holder[s]|2¦[amount of] rows|3¦[amount of] slots)"
      );
   }
}
