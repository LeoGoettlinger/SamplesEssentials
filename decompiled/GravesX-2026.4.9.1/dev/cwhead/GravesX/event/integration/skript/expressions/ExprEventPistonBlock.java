package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.GravePistonExtendEvent;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEventPistonBlock extends SimpleExpression<Block> {
   @Nullable
   protected Block[] get(Event e) {
      return e instanceof GravePistonExtendEvent ? new Block[]{((GravePistonExtendEvent)e).getPistonBlock()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends Block> getReturnType() {
      return Block.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event block";
   }
}
