package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.sections.EffSecShoot;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

@Name("Shooter")
@Description({"The shooter of a projectile."})
@Example("shooter is a skeleton")
@Since({"1.3.7, 2.11 (entity shoot bow event)"})
public class ExprShooter extends PropertyExpression<Projectile, LivingEntity> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Projectile>)exprs[0]);
      return true;
   }

   @Nullable
   protected LivingEntity[] get(Event event, Projectile[] source) {
      if (event instanceof EffSecShoot.ShootEvent shootEvent && this.getExpr().isDefault() && !(shootEvent.getProjectile() instanceof Projectile)) {
         return new LivingEntity[]{shootEvent.getShooter()};
      } else {
         return event instanceof EntityShootBowEvent shootBowEvent && this.getExpr().isDefault()
            ? new LivingEntity[]{shootBowEvent.getEntity()}
            : this.get(source, projectile -> (projectile != null ? projectile.getShooter() : null) instanceof LivingEntity livingShooter ? livingShooter : null);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? new Class[]{LivingEntity.class} : super.acceptChange(mode);
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.SET) {
         assert delta != null;

         ProjectileSource source = (ProjectileSource)delta[0];

         for (Projectile projectile : this.getExpr().getArray(event)) {
            assert projectile != null : this.getExpr();

            projectile.setShooter(source);
         }
      } else {
         super.change(event, delta, mode);
      }
   }

   @Override
   public Class<LivingEntity> getReturnType() {
      return LivingEntity.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the shooter" + (this.getExpr().isDefault() ? "" : " of " + this.getExpr().toString(event, debug));
   }

   static {
      Skript.registerExpression(ExprShooter.class, LivingEntity.class, ExpressionType.SIMPLE, "[the] shooter [of %projectile%]");
   }
}
