package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Chunk")
@Description({"Returns the <a href='#chunk'>chunk</a> of a block, location or entity is in, or a list of the loaded chunks of a world."})
@Example.Examples({@Example("add the chunk at the player to {protected chunks::*}"), @Example("set {_chunks::*} to the loaded chunks of the player's world")})
@Since({"2.0, 2.8.0 (loaded chunks)"})
public class ExprChunk extends SimpleExpression<Chunk> {
   private int pattern;
   private Expression<Location> locations;
   private Expression<World> worlds;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.pattern = matchedPattern;
      if (this.pattern == 0) {
         this.locations = (Expression<Location>)exprs[1];
         if (exprs[0] != null) {
            this.locations = Direction.combine((Expression<? extends Direction>)exprs[0], this.locations);
         }
      } else if (this.pattern == 1) {
         this.locations = (Expression<Location>)exprs[0];
      } else {
         this.worlds = (Expression<World>)exprs[0];
      }

      return true;
   }

   protected Chunk[] get(Event event) {
      return this.pattern != 2
         ? this.locations.stream(event).map(Location::getChunk).toArray(Chunk[]::new)
         : this.worlds.stream(event).flatMap(world -> Arrays.stream(world.getLoadedChunks())).toArray(Chunk[]::new);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.RESET ? new Class[0] : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert mode == Changer.ChangeMode.RESET;

      for (Chunk chunk : this.get(event)) {
         chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
      }
   }

   @Override
   public boolean isSingle() {
      return this.pattern == 2 ? false : this.locations.isSingle();
   }

   @Override
   public Class<? extends Chunk> getReturnType() {
      return Chunk.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.pattern == 2 ? "loaded chunks of " + this.worlds.toString(event, debug) : "chunk at " + this.locations.toString(event, debug);
   }

   static {
      Skript.registerExpression(
         ExprChunk.class,
         Chunk.class,
         ExpressionType.COMBINED,
         "[(all [[of] the]|the)] chunk[s] (of|%-directions%) %locations%",
         "%locations%'[s] chunk[s]",
         "[(all [[of] the]|the)] loaded chunks (of|in) %worlds%"
      );
   }
}
