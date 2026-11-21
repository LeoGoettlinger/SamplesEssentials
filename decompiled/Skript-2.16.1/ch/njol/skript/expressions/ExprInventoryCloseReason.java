package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.jetbrains.annotations.Nullable;

@Name("Inventory Close Reason")
@Description({"The <a href='/#inventoryclosereason'>inventory close reason</a> of an <a href='/#inventory_close'>inventory close event</a>."})
@Example("on inventory close:\n\tinventory close reason is teleport\n\tsend \"Your inventory closed due to teleporting!\" to player\n")
@Events({"Inventory Close"})
@Since({"2.8.0"})
public class ExprInventoryCloseReason extends EventValueExpression<Reason> implements EventRestrictedSyntax {
   public ExprInventoryCloseReason() {
      super(Reason.class);
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{InventoryCloseEvent.class};
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "inventory close reason";
   }

   static {
      Skript.registerExpression(ExprInventoryCloseReason.class, Reason.class, ExpressionType.SIMPLE, "[the] inventory clos(e|ing) (reason|cause)");
   }
}
