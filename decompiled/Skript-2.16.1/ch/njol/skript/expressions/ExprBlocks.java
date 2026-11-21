package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.AABB;
import ch.njol.skript.util.BlockLineIterator;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import com.google.common.collect.Lists;
import java.util.Iterator;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Blocks")
@Description({"Blocks relative to other blocks or between other blocks.", "Can be used to get blocks relative to other blocks or for looping.", "Blocks from/to and between will return a straight line whereas blocks within will return a cuboid."})
@Example.Examples({@Example("loop blocks above the player:"), @Example("loop blocks between the block below the player and the targeted block:"), @Example("set the blocks below the player, the victim and the targeted block to air"), @Example("set all blocks within {loc1} and {loc2} to stone"), @Example("set all blocks within chunk at player to air")})
@Since({"1.0, 2.5.1 (within/cuboid/chunk)"})
public class ExprBlocks extends SimpleExpression<Block> {
   private static final boolean SUPPORTS_WORLD_LOADED = Skript.methodExists(Location.class, "isWorldLoaded");
   @Nullable
   private Expression<Direction> direction;
   @Nullable
   private Expression<Location> end;
   @Nullable
   private Expression<Chunk> chunk;
   private Expression<?> from;
   private int pattern;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.pattern = matchedPattern;
      switch (matchedPattern) {
         case 0:
            this.direction = (Expression<Direction>)exprs[0];
            this.from = exprs[1];
            break;
         case 1:
            this.from = exprs[0];
            this.direction = (Expression<Direction>)exprs[1];
            break;
         case 2:
         case 3:
         case 4:
            this.from = exprs[0];
            this.end = (Expression<Location>)exprs[1];
            break;
         case 5:
            this.chunk = (Expression<Chunk>)exprs[0];
            break;
         default:
            assert false : matchedPattern;

            return false;
      }

      return true;
   }

   @Nullable
   protected Block[] get(Event event) {
      if (this.direction != null && !this.from.isSingle()) {
         Direction direction = this.direction.getSingle(event);
         return direction == null
            ? new Block[0]
            : this.from
               .stream(event)
               .filter(Location.class::isInstance)
               .map(Location.class::cast)
               .filter(location -> SUPPORTS_WORLD_LOADED ? location.isWorldLoaded() : location.getChunk().isLoaded())
               .map(direction::getRelative)
               .map(Location::getBlock)
               .toArray(Block[]::new);
      } else {
         Iterator<Block> iterator = this.iterator(event);
         return iterator == null ? new Block[0] : Lists.newArrayList(iterator).toArray(new Block[0]);
      }
   }

   @Nullable
   @Override
   public Iterator<Block> iterator(Event event) {
      try {
         if (this.chunk != null) {
            Chunk chunk = this.chunk.getSingle(event);
            return chunk != null ? new AABB(chunk).iterator() : null;
         } else if (this.direction != null) {
            if (!this.from.isSingle()) {
               return new ArrayIterator<Block>(this.get(event));
            } else {
               Object object = this.from.getSingle(event);
               if (object == null) {
                  return null;
               } else {
                  Location location = object instanceof Location ? (Location)object : ((Block)object).getLocation().add(0.5, 0.5, 0.5);
                  Direction direction = this.direction.getSingle(event);
                  if (direction != null && location.getWorld() != null) {
                     Vector vector = object != location ? direction.getDirection((Block)object) : direction.getDirection(location);
                     if (vector.getX() == 0.0 && vector.getY() == 0.0 && vector.getZ() == 0.0) {
                        return null;
                     } else {
                        int distance = SkriptConfig.maxTargetBlockDistance.value() - 1;
                        if (this.direction instanceof ExprDirection) {
                           Expression<Number> numberExpression = ((ExprDirection)this.direction).amount;
                           if (numberExpression != null) {
                              Number number = numberExpression.getSingle(event);
                              if (number != null) {
                                 distance = number.intValue();
                              }
                           }
                        }

                        return new BlockLineIterator(location, vector, (double)distance);
                     }
                  } else {
                     return null;
                  }
               }
            }
         } else {
            Location loc = (Location)this.from.getSingle(event);
            if (loc == null) {
               return null;
            } else {
               assert this.end != null;

               Location loc2 = this.end.getSingle(event);
               if (loc2 == null || loc2.getWorld() != loc.getWorld()) {
                  return null;
               } else {
                  return (Iterator<Block>)(this.pattern == 4 ? new AABB(loc, loc2).iterator() : new BlockLineIterator(loc.getBlock(), loc2.getBlock()));
               }
            }
         }
      } catch (IllegalStateException var9) {
         if (var9.getMessage().equals("Start block missed in BlockIterator")) {
            return null;
         } else {
            throw var9;
         }
      }
   }

   @Override
   public Class<? extends Block> getReturnType() {
      return Block.class;
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.chunk != null) {
         return "blocks within chunk " + this.chunk.toString(event, debug);
      } else if (this.pattern == 4) {
         assert this.end != null;

         return "blocks within " + this.from.toString(event, debug) + " and " + this.end.toString(event, debug);
      } else if (this.end != null) {
         return "blocks from " + this.from.toString(event, debug) + " to " + this.end.toString(event, debug);
      } else {
         assert this.direction != null;

         return "blocks " + this.direction.toString(event, debug) + " " + this.from.toString(event, debug);
      }
   }

   static {
      Skript.registerExpression(
         ExprBlocks.class,
         Block.class,
         ExpressionType.COMBINED,
         "[(all [[of] the]|the)] blocks %direction% [%locations%]",
         "[(all [[of] the]|the)] blocks from %location% [on] %direction%",
         "[(all [[of] the]|the)] blocks from %location% to %location%",
         "[(all [[of] the]|the)] blocks between %location% and %location%",
         "[(all [[of] the]|the)] blocks within %location% and %location%",
         "[(all [[of] the]|the)] blocks (in|within) %chunk%"
      );
   }
}
