package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.slot.CursorSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.Nullable;

@Name("Cursor Slot")
@Description({"The item which the player has on their inventory cursor. This slot is always empty if player has no inventory open."})
@Example.Examples({@Example("cursor slot of player is dirt"), @Example("set cursor slot of player to 64 diamonds")})
@Since({"2.2-dev17"})
public class ExprCursorSlot extends PropertyExpression<Player, Slot> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Player>)exprs[0]);
      return true;
   }

   protected Slot[] get(Event event, Player[] source) {
      return this.get(
         source, player -> event instanceof InventoryClickEvent ? new CursorSlot(player, ((InventoryClickEvent)event).getCursor()) : new CursorSlot(player)
      );
   }

   @Override
   public Class<? extends Slot> getReturnType() {
      return Slot.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "cursor slot of " + this.getExpr().toString(event, debug);
   }

   static {
      register(ExprCursorSlot.class, Slot.class, "cursor slot", "players");
   }
}
