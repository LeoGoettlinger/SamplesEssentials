package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Arrow Knockback Strength")
@Description({"An arrow's knockback strength."})
@Example("on shoot:\n\tevent-projectile is an arrow\n\tset arrow knockback strength of event-projectile to 10\n")
@Since({"2.5.1"})
public class ExprArrowKnockbackStrength extends SimplePropertyExpression<Projectile, Long> {
   static final boolean abstractArrowExists = Skript.classExists("org.bukkit.entity.AbstractArrow");

   @Nullable
   public Long convert(Projectile arrow) {
      if (abstractArrowExists) {
         return arrow instanceof AbstractArrow ? (long)((AbstractArrow)arrow).getKnockbackStrength() : null;
      } else {
         return arrow instanceof Arrow ? (long)((Arrow)arrow).getKnockbackStrength() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      switch (mode) {
         case SET:
         case ADD:
         case RESET:
         case REMOVE:
            return CollectionUtils.array(Number.class);
         default:
            return null;
      }
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int strength = delta != null ? ((Number)delta[0]).intValue() : 0;
      switch (mode) {
         case SET:
         case RESET:
            for (Projectile entity : this.getExpr().getArray(e)) {
               if (abstractArrowExists) {
                  if (entity instanceof AbstractArrow) {
                     ((AbstractArrow)entity).setKnockbackStrength(strength);
                  }
               } else if (entity instanceof Arrow) {
                  ((Arrow)entity).setKnockbackStrength(strength);
               }
            }
            break;
         case ADD:
            if (abstractArrowExists) {
               for (Projectile entityx : this.getExpr().getArray(e)) {
                  if (entityx instanceof AbstractArrow abstractArrow) {
                     int dmg = abstractArrow.getKnockbackStrength() + strength;
                     if (dmg < 0) {
                        return;
                     }

                     abstractArrow.setKnockbackStrength(dmg);
                  }
               }
            } else {
               for (Projectile entityxx : this.getExpr().getArray(e)) {
                  if (entityxx instanceof Arrow arrow) {
                     int dmg = arrow.getKnockbackStrength() + strength;
                     if (dmg < 0) {
                        return;
                     }

                     arrow.setKnockbackStrength(dmg);
                  }
               }
            }
            break;
         case REMOVE:
            if (abstractArrowExists) {
               for (Projectile entityxxx : this.getExpr().getArray(e)) {
                  if (entityxxx instanceof AbstractArrow abstractArrow) {
                     int dmg = abstractArrow.getKnockbackStrength() - strength;
                     if (dmg < 0) {
                        dmg = 0;
                     }

                     abstractArrow.setKnockbackStrength(dmg);
                  }
               }
            } else {
               for (Projectile entityxxxx : this.getExpr().getArray(e)) {
                  if (entityxxxx instanceof Arrow arrow) {
                     int dmg = arrow.getKnockbackStrength() - strength;
                     if (dmg < 0) {
                        return;
                     }

                     arrow.setKnockbackStrength(dmg);
                  }
               }
            }
            break;
         default:
            assert false;
      }
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   protected String getPropertyName() {
      return "projectile knockback strength";
   }

   static {
      register(ExprArrowKnockbackStrength.class, Long.class, "arrow knockback strength", "projectiles");
   }
}
