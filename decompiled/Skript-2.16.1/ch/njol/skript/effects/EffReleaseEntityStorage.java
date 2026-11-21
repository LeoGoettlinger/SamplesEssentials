package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Release From Entity Storage")
@Description({"Releases the stored entities in an entity block storage (i.e. beehive).", "When using beehives, providing a timespan will prevent the released bees from re-entering the beehive for that amount of time.", "Due to unstable behaviour on older versions, this effect requires Minecraft version 1.21+."})
@Example.Examples({@Example("release the stored entities of {_beehive}"), @Example("release the entity storage of {_hive} for 5 seconds")})
@RequiredPlugins({"Minecraft 1.21"})
@Since({"2.11"})
public class EffReleaseEntityStorage extends Effect {
   private Expression<Block> blocks;
   @Nullable
   private Expression<Timespan> timespan;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.blocks = (Expression<Block>)exprs[0];
      if (exprs[1] != null) {
         this.timespan = (Expression<Timespan>)exprs[1];
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      Integer ticks = null;
      if (this.timespan != null) {
         Timespan time = this.timespan.getSingle(event);
         if (time != null) {
            ticks = (int)time.getAs(Timespan.TimePeriod.TICK);
         }
      }

      for (Block block : this.blocks.getArray(event)) {
         if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
            List<? extends Entity> released = blockStorage.releaseEntities();
            if (ticks != null) {
               for (Entity entity : released) {
                  if (entity instanceof Bee bee) {
                     bee.setCannotEnterHiveTicks(ticks);
                  }
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("release the stored entities of", this.blocks);
      if (this.timespan != null) {
         builder.append("for", this.timespan);
      }

      return builder.toString();
   }

   static {
      if (Skript.isRunningMinecraft(1, 21, 0)) {
         Skript.registerEffect(EffReleaseEntityStorage.class, "(release|evict) [the] (stored entities|entity storage) of %blocks% [for %-timespan%]");
      }
   }
}
