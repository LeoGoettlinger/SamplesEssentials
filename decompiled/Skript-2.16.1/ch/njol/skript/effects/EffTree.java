package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.TreeSpecies;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Tree")
@Description({"Creates a tree.", "This may require that there is enough space above the given location and that the block below is dirt/grass, but it is possible that the tree will just grow anyways, possibly replacing every block in its path."})
@Example("grow a tall redwood tree above the clicked block")
@Since({"1.0"})
public class EffTree extends Effect {
   private Expression<Location> blocks;
   private Expression<TreeSpecies> type;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.type = (Expression<TreeSpecies>)exprs[0];
      this.blocks = Direction.combine((Expression<? extends Direction>)exprs[1], (Expression<? extends Location>)exprs[2]);
      return true;
   }

   @Override
   public void execute(Event e) {
      TreeSpecies type = this.type.getSingle(e);
      if (type != null) {
         for (Location l : this.blocks.getArray(e)) {
            assert l != null : this.blocks;

            type.grow(l.getBlock());
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "grow tree of type " + this.type.toString(e, debug) + " " + this.blocks.toString(e, debug);
   }

   static {
      Skript.registerEffect(
         EffTree.class,
         "(grow|create|generate) tree [of type %treetype%] %directions% %locations%",
         "(grow|create|generate) %treetype% %directions% %locations%"
      );
   }
}
