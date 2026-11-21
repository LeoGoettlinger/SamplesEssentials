package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Persistent")
@Description({"Make entities, players, or leaves be persistent.", "Persistence of entities is whether they are retained through server restarts.", "Persistence of leaves is whether they should decay when not connected to a log block within 6 meters.", "Persistence of players is if the player's playerdata should be saved when they leave the server. Players' persistence is reset back to 'true' when they join the server.", "Passengers inherit the persistence of their vehicle, meaning a persistent zombie put on a non-persistent chicken will become non-persistent. This does not apply to players.", "By default, all entities are persistent."})
@Example.Examples({@Example("prevent all entities from persisting"), @Example("force {_leaves} to persist"), @Example("command /kickcheater <cheater: player>:\n\tpermission: op\n\ttrigger:\n\t\tprevent {_cheater} from persisting\n\t\tkick {_cheater}\n")})
@Since({"2.11"})
public class EffPersistent extends Effect {
   private Expression<?> source;
   private boolean persist;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.source = exprs[0];
      if (matchedPattern < 2) {
         this.persist = !parseResult.hasTag("not");
      } else {
         this.persist = false;
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Object object : this.source.getArray(event)) {
         if (object instanceof Entity entity) {
            entity.setPersistent(this.persist);
         } else if (object instanceof Block block && block.getBlockData() instanceof Leaves leaves) {
            leaves.setPersistent(this.persist);
            block.setBlockData(leaves);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.persist ? "make " + this.source.toString(event, debug) + " persistent" : "prevent " + this.source.toString(event, debug) + " from persisting";
   }

   static {
      Skript.registerEffect(
         EffPersistent.class,
         "make %entities/blocks% [:not] persist[ent]",
         "force %entities/blocks% to [:not] persist",
         "prevent %entities/blocks% from persisting"
      );
   }
}
