package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

@Name("Last Damage Cause")
@Description({"Cause of last damage done to an entity"})
@Example("set last damage cause of event-entity to fire tick")
@Since({"2.2-Fixes-V10"})
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, DamageCause> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.setExpr((Expression<? extends LivingEntity>)exprs[0]);
      return true;
   }

   protected DamageCause[] get(Event e, LivingEntity[] source) {
      return this.get(source, entity -> {
         EntityDamageEvent dmgEvt = entity.getLastDamageCause();
         return dmgEvt == null ? DamageCause.CUSTOM : dmgEvt.getCause();
      });
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "the damage cause " + this.getExpr().toString(e, debug);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.REMOVE_ALL ? null : CollectionUtils.array(DamageCause.class);
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      DamageCause d = delta == null ? DamageCause.CUSTOM : (DamageCause)delta[0];

      assert d != null;

      switch (mode) {
         case DELETE:
         case RESET:
            for (LivingEntity entity : this.getExpr().getArray(e)) {
               assert entity != null : this.getExpr();

               HealthUtils.setDamageCause(entity, DamageCause.CUSTOM);
            }
            break;
         case SET:
            for (LivingEntity entity : this.getExpr().getArray(e)) {
               assert entity != null : this.getExpr();

               HealthUtils.setDamageCause(entity, d);
            }
            break;
         case REMOVE_ALL:
            assert false;
      }
   }

   @Override
   public Class<DamageCause> getReturnType() {
      return DamageCause.class;
   }

   static {
      register(ExprLastDamageCause.class, DamageCause.class, "last damage (cause|reason|type)", "livingentities");
   }
}
