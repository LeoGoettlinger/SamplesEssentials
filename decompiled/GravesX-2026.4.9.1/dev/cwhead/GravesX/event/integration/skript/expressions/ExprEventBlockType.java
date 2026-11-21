package dev.cwhead.GravesX.event.integration.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.ranull.graves.data.BlockData;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEventBlockType extends SimpleExpression<BlockData.BlockType> {
   @Nullable
   protected BlockData.BlockType[] get(Event e) {
      return e instanceof GraveEvent ? new BlockData.BlockType[]{((GraveEvent)e).getBlockType()} : null;
   }

   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
      return true;
   }

   public Class<? extends BlockData.BlockType> getReturnType() {
      return BlockData.BlockType.class;
   }

   public boolean isSingle() {
      return true;
   }

   public String toString(@Nullable Event e, boolean debug) {
      return "event block type";
   }
}
