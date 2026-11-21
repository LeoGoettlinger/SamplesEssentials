package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import org.bukkit.event.Event;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;

public class ExprEventInventoryView extends SimpleExpression<InventoryView> {
   @Nullable
   protected InventoryView[] get(Event e) {
      return e instanceof GravePlayerEvent ? new InventoryView[]{((GravePlayerEvent)e).getInventoryView()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends InventoryView> getReturnType() {
      return InventoryView.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event inventory view";
   }
}
