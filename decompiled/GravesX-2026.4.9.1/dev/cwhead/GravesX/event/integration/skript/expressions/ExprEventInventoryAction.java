package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.GraveItemTakeEvent;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.Nullable;

public class ExprEventInventoryAction extends SimpleExpression<InventoryAction> {
   @Nullable
   protected InventoryAction[] get(Event e) {
      return e instanceof GraveItemTakeEvent event ? new InventoryAction[]{event.getAction()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends InventoryAction> getReturnType() {
      return InventoryAction.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event inventory action";
   }
}
