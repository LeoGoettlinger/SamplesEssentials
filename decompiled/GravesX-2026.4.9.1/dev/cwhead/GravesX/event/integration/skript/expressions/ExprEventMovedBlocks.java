package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import dev.cwhead.GravesX.event.GravePistonExtendEvent;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEventMovedBlocks extends SimpleExpression<List> {
   @Nullable
   protected List[] get(Event e) {
      return e instanceof GravePistonExtendEvent
         ? new List[]{((GravePistonExtendEvent)e).getMovedBlocks().stream().filter(block -> block.getType() != Material.AIR).collect(Collectors.toList())}
         : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends List> getReturnType() {
      return List.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event moved blocks";
   }
}
