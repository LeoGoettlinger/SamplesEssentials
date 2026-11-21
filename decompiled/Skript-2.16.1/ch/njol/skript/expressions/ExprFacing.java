package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Facing")
@Description({"The facing of an entity or block, i.e. exactly north, south, east, west, up or down (unlike <a href='#ExprDirection'>direction</a> which is the exact direction, e.g. '0.5 south and 0.7 east')"})
@Example("# makes a bridge\nloop blocks from the block below the player in the horizontal facing of the player:\n\tset loop-block to cobblestone\n")
@Since({"1.4"})
public class ExprFacing extends SimplePropertyExpression<Object, Direction> {
   private boolean horizontal;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.horizontal = parseResult.mark == 1;
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Direction convert(Object o) {
      if (o instanceof Block) {
         BlockData data = ((Block)o).getBlockData();
         return data instanceof Directional ? new Direction(((Directional)data).getFacing(), 1.0) : null;
      } else if (o instanceof LivingEntity) {
         return new Direction(Direction.getFacing(((LivingEntity)o).getLocation(), this.horizontal), 1.0);
      } else {
         assert false;

         return null;
      }
   }

   @Override
   protected String getPropertyName() {
      return (this.horizontal ? "horizontal " : "") + "facing";
   }

   @Override
   public Class<Direction> getReturnType() {
      return Direction.class;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.getExpr().canReturn(Block.class)) {
         return null;
      } else {
         return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Direction.class) : null;
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) throws UnsupportedOperationException {
      assert mode == Changer.ChangeMode.SET;

      assert delta != null;

      Block b = (Block)this.getExpr().getSingle(e);
      if (b != null) {
         BlockData data = b.getBlockData();
         if (data instanceof Directional) {
            ((Directional)data).setFacing(toBlockFace(((Direction)delta[0]).getDirection(b)));
            b.setBlockData(data, false);
         }
      }
   }

   private static BlockFace toBlockFace(Vector dir) {
      BlockFace r = null;
      double d = Double.MAX_VALUE;

      for (BlockFace f : BlockFace.values()) {
         double a = Math.pow(f.getModX() - dir.getX(), 2.0) + Math.pow(f.getModY() - dir.getY(), 2.0) + Math.pow(f.getModZ() - dir.getZ(), 2.0);
         if (a < d) {
            d = a;
            r = f;
         }
      }

      assert r != null;

      return r;
   }

   static {
      register(ExprFacing.class, Direction.class, "(1¦horizontal|) facing", "livingentities/blocks");
   }
}
