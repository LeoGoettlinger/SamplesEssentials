package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Max Health")
@Description({"The maximum health of an entity, e.g. 10 for a player."})
@Example.Examples({@Example("on join:\n\tset the maximum health of the player to 100\n"), @Example("spawn a giant\nset the last spawned entity's max health to 1000\n")})
@Since({"2.0"})
@Events({"damage", "death"})
public class ExprMaxHealth extends SimplePropertyExpression<LivingEntity, Number> {
   public Number convert(LivingEntity e) {
      return HealthUtils.getMaxHealth(e);
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return "max health";
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.REMOVE_ALL ? new Class[]{Number.class} : null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      double d = delta == null ? 0.0 : ((Number)delta[0]).doubleValue();

      for (LivingEntity en : this.getExpr().getArray(e)) {
         assert en != null : this.getExpr();

         switch (mode) {
            case SET:
               HealthUtils.setMaxHealth(en, d);
               break;
            case REMOVE:
               d = -d;
            case ADD:
               HealthUtils.setMaxHealth(en, HealthUtils.getMaxHealth(en) + d);
               break;
            case RESET:
               en.resetMaxHealth();
               break;
            case DELETE:
            case REMOVE_ALL:
               assert false;
         }
      }
   }

   static {
      register(ExprMaxHealth.class, Number.class, "max[imum] health", "livingentities");
   }
}
