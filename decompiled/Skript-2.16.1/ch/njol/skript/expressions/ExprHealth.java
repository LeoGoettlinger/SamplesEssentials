package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Health")
@Description({"The health of a creature, e.g. a player, mob, villager, etc. The minimum value is 0, and the maximum is the creature's max health (e.g. 10 for players)."})
@Example("message \"You have %health% HP left.\"")
@Since({"1.0"})
@Events({"damage"})
public class ExprHealth extends PropertyExpression<LivingEntity, Number> {
   @Override
   public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends LivingEntity>)vars[0]);
      return true;
   }

   protected Number[] get(Event e, LivingEntity[] source) {
      return this.get(source, HealthUtils::getHealth);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the health of " + this.getExpr().toString(e, debug);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.REMOVE_ALL ? null : CollectionUtils.array(Number.class);
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      double d = delta == null ? 0.0 : ((Number)delta[0]).doubleValue();
      switch (mode) {
         case DELETE:
         case SET:
            for (LivingEntity entity : this.getExpr().getArray(e)) {
               assert entity != null : this.getExpr();

               HealthUtils.setHealth(entity, d);
            }
            break;
         case REMOVE:
            d = -d;
         case ADD:
            for (LivingEntity entity : this.getExpr().getArray(e)) {
               assert entity != null : this.getExpr();

               HealthUtils.heal(entity, d);
            }
            break;
         case RESET:
            for (LivingEntity entity : this.getExpr().getArray(e)) {
               assert entity != null : this.getExpr();

               HealthUtils.setHealth(entity, HealthUtils.getMaxHealth(entity));
            }
            break;
         case REMOVE_ALL:
            assert false;
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   static {
      register(ExprHealth.class, Number.class, "health", "livingentities");
   }
}
