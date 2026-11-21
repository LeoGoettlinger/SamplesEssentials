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

@Name("Projectile Critical State")
@Description({"A projectile's critical state. The only currently accepted projectiles are arrows and tridents."})
@Example("on shoot:\n\tevent-projectile is an arrow\n\tset projectile critical mode of event-projectile to true\n")
@Since({"2.5.1"})
public class ExprProjectileCriticalState extends SimplePropertyExpression<Projectile, Boolean> {
   private static final boolean abstractArrowExists = Skript.classExists("org.bukkit.entity.AbstractArrow");

   @Nullable
   public Boolean convert(Projectile arrow) {
      if (abstractArrowExists) {
         return arrow instanceof AbstractArrow ? ((AbstractArrow)arrow).isCritical() : null;
      } else {
         return arrow instanceof Arrow ? ((Arrow)arrow).isCritical() : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         boolean state = (Boolean)delta[0];

         for (Projectile entity : this.getExpr().getAll(e)) {
            if (abstractArrowExists && entity instanceof AbstractArrow) {
               ((AbstractArrow)entity).setCritical(state);
            } else if (entity instanceof Arrow) {
               ((Arrow)entity).setCritical(state);
            }
         }
      }
   }

   @Override
   public Class<? extends Boolean> getReturnType() {
      return Boolean.class;
   }

   @Override
   protected String getPropertyName() {
      return "critical arrow state";
   }

   static {
      register(ExprProjectileCriticalState.class, Boolean.class, "(projectile|arrow) critical (state|ability|mode)", "projectiles");
   }
}
