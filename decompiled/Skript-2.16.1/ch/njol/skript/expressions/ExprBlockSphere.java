package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockSphereIterator;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.util.coll.iterator.IteratorIterable;
import java.util.ArrayList;
import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Block Sphere")
@Description({"All blocks in a sphere around a center, mostly useful for looping."})
@Example("loop blocks in radius 5 around the player:\n\tset loop-block to air\n")
@Since({"1.0"})
public class ExprBlockSphere extends SimpleExpression<Block> {
   private Expression<Number> radius;
   private Expression<Location> center;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.radius = (Expression<Number>)exprs[matchedPattern];
      this.center = (Expression<Location>)exprs[1 - matchedPattern];
      return true;
   }

   @Override
   public Iterator<Block> iterator(Event e) {
      Location l = this.center.getSingle(e);
      Number r = this.radius.getSingle(e);
      return (Iterator<Block>)(l != null && r != null ? new BlockSphereIterator(l, r.doubleValue()) : new EmptyIterator<>());
   }

   @Nullable
   protected Block[] get(Event e) {
      Number r = this.radius.getSingle(e);
      if (r == null) {
         return new Block[0];
      } else {
         ArrayList<Block> list = new ArrayList<>((int)(4.60766922526503 * Math.pow(r.doubleValue(), 3.0)));

         for (Block b : new IteratorIterable<>(this.iterator(e))) {
            list.add(b);
         }

         return list.toArray(new Block[list.size()]);
      }
   }

   @Override
   public Class<? extends Block> getReturnType() {
      return Block.class;
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the blocks in radius " + this.radius + " around " + this.center.toString(e, debug);
   }

   @Override
   public boolean isLoopOf(String s) {
      return s.equalsIgnoreCase("block");
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   static {
      Skript.registerExpression(
         ExprBlockSphere.class,
         Block.class,
         ExpressionType.COMBINED,
         "[(all [[of] the]|the)] blocks in radius %number% [(of|around) %location%]",
         "[(all [[of] the]|the)] blocks around %location% in radius %number%"
      );
   }
}
