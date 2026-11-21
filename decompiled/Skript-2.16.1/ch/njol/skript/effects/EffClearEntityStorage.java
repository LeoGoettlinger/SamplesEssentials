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
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Clear Entity Storage")
@Description({"Clear the stored entities of an entity block storage (i.e. beehive)."})
@Example("clear the stored entities of {_beehive}")
@Since({"2.11"})
public class EffClearEntityStorage extends Effect {
   private Expression<Block> blocks;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.blocks = (Expression<Block>)exprs[0];
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (Block block : this.blocks.getArray(event)) {
         if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
            blockStorage.clearEntities();
            blockStorage.update(true, false);
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "clear the stored entities of " + this.blocks.toString(event, debug);
   }

   static {
      if (Skript.methodExists(EntityBlockStorage.class, "clearEntities")) {
         Skript.registerEffect(EffClearEntityStorage.class, "(clear|empty) the (stored entities|entity storage) of %blocks%");
      }
   }
}
