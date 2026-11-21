package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Range")
@Description({"The range of a beacon's effects, in blocks."})
@Example("if the beacon tier of the clicked block is 4:\n\tset the beacon effect range of the clicked block to 100\n")
@Since({"2.10"})
public class ExprBeaconRange extends SimplePropertyExpression<Block, Double> {
   @Nullable
   public Double convert(Block block) {
      return block.getState() instanceof Beacon beacon ? beacon.getEffectRange() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> (Class[])CollectionUtils.array(Double.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.RESET) {
         for (Block block : this.getExpr().getArray(event)) {
            if (block.getState() instanceof Beacon beacon) {
               beacon.resetEffectRange();
               beacon.update(true);
            }
         }
      } else {
         assert delta != null;

         double range = ((Number)delta[0]).doubleValue();

         for (Block blockx : this.getExpr().getArray(event)) {
            if (blockx.getState() instanceof Beacon beacon) {
               switch (mode) {
                  case SET:
                     beacon.setEffectRange(Math2.fit(0.0, range, Double.MAX_VALUE));
                     break;
                  case ADD:
                     beacon.setEffectRange(Math2.fit(0.0, beacon.getEffectRange() + range, Double.MAX_VALUE));
                     break;
                  case REMOVE:
                     beacon.setEffectRange(Math2.fit(0.0, beacon.getEffectRange() - range, Double.MAX_VALUE));
                     break;
                  default:
                     throw new IllegalStateException();
               }

               beacon.update(true);
            }
         }
      }
   }

   @Override
   public Class<? extends Double> getReturnType() {
      return Double.class;
   }

   @Override
   protected String getPropertyName() {
      return "beacon range";
   }

   static {
      if (Skript.methodExists(Beacon.class, "getEffectRange")) {
         register(ExprBeaconRange.class, Double.class, "beacon [effect] range", "blocks");
      }
   }
}
