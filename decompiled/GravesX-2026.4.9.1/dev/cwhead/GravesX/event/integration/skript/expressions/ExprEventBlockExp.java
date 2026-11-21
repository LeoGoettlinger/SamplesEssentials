package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.GraveBreakEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEventBlockExp extends SimpleExpression<Integer> {
   @Nullable
   protected Integer[] get(Event e) {
      return e instanceof GraveBreakEvent ? new Integer[]{((GraveBreakEvent)e).getBlockExp()} : null;
   }

   public void set(Event e, @Nullable Integer exp) {
      if (e instanceof GraveBreakEvent && exp != null) {
         ((GraveBreakEvent)e).setBlockExp(exp);
      }
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event block experience";
   }
}
