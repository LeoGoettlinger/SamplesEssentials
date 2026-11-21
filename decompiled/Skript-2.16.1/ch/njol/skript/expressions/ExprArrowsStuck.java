package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Arrows Stuck")
@Description({"The number of arrows stuck in a living entity."})
@Example("set arrows stuck in player to 5")
@Since({"2.5"})
public class ExprArrowsStuck extends SimplePropertyExpression<LivingEntity, Long> {
   public Long convert(LivingEntity le) {
      return (long)le.getArrowsStuck();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.REMOVE_ALL ? null : CollectionUtils.array(Number.class);
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int d = delta == null ? 0 : ((Number)delta[0]).intValue();

      for (LivingEntity le : this.getExpr().getArray(e)) {
         switch (mode) {
            case ADD:
               int r1 = le.getArrowsStuck() + d;
               if (r1 < 0) {
                  r1 = 0;
               }

               le.setArrowsStuck(r1);
               break;
            case SET:
               le.setArrowsStuck(d);
               break;
            case DELETE:
            case RESET:
               le.setArrowsStuck(0);
               break;
            case REMOVE:
               int r2 = le.getArrowsStuck() - d;
               if (r2 < 0) {
                  r2 = 0;
               }

               le.setArrowsStuck(r2);
               break;
            case REMOVE_ALL:
               assert false;
         }
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "arrows stuck";
   }

   static {
      if (Skript.methodExists(LivingEntity.class, "getArrowsStuck")) {
         Skript.registerExpression(ExprArrowsStuck.class, Long.class, ExpressionType.PROPERTY, "[number of] arrow[s] stuck in %livingentities%");
      }
   }
}
