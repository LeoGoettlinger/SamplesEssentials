package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Sea Pickles")
@Description({"An expression to obtain or modify data relating to the pickles of a sea pickle block."})
@Example("on block break:\n\ttype of block is sea pickle\n\tsend \"Wow! This stack of sea pickles contained %event-block's sea pickle count% pickles!\"\n\tsend \"It could've contained a maximum of %event-block's maximum sea pickle count% pickles!\"\n\tsend \"It had to have contained at least %event-block's minimum sea pickle count% pickles!\"\n\tcancel event\n\tset event-block's sea pickle count to event-block's maximum sea pickle count\n\tsend \"This bad boy is going to hold so many pickles now!!\"\n")
@Since({"2.7"})
public class ExprSeaPickles extends SimplePropertyExpression<Block, Integer> {
   private boolean minimum;
   private boolean maximum;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.minimum = parseResult.hasTag("min");
      this.maximum = parseResult.hasTag("max");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Integer convert(Block block) {
      if (!(block.getBlockData() instanceof SeaPickle pickleData)) {
         return null;
      } else if (this.maximum) {
         return pickleData.getMaximumPickles();
      } else {
         return this.minimum ? pickleData.getMinimumPickles() : pickleData.getPickles();
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.minimum && !this.maximum) {
         switch (mode) {
            case SET:
            case ADD:
            case REMOVE:
            case RESET:
            case DELETE:
               return CollectionUtils.array(Number.class);
            default:
               return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE) {
         int change = delta != null ? ((Number)delta[0]).intValue() : 0;
         if (mode == Changer.ChangeMode.REMOVE) {
            change *= -1;
         }

         for (Block block : this.getExpr().getArray(event)) {
            if (!(block.getBlockData() instanceof SeaPickle pickleData)) {
               return;
            }

            int newPickles = change;
            switch (mode) {
               case ADD:
               case REMOVE:
                  newPickles = change + pickleData.getPickles();
               case SET:
                  if (newPickles != 0) {
                     newPickles = Math.max(pickleData.getMinimumPickles(), newPickles);
                     newPickles = Math.min(pickleData.getMaximumPickles(), newPickles);
                  }
                  break;
               case RESET:
                  newPickles = pickleData.getMinimumPickles();
            }

            if (newPickles != 0) {
               pickleData.setPickles(newPickles);
               block.setBlockData(pickleData);
            } else {
               block.setType(pickleData.isWaterlogged() ? Material.WATER : Material.AIR);
            }
         }
      }
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.maximum ? "maximum " : (this.minimum ? "minimum " : "")) + "sea pickle count";
   }

   static {
      register(ExprSeaPickles.class, Integer.class, "[:(min|max)[imum]] [sea] pickle(s| (count|amount))", "blocks");
   }
}
