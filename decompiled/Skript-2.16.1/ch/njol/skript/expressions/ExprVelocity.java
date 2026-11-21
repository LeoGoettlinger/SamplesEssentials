package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;

@Name("Velocity")
@Description({"Gets or changes velocity of an entity or particle.", "Setting the velocity of a particle will remove its random dispersion and force it to be a single particle."})
@Example.Examples({@Example("set player's velocity to {_v}"), @Example("set the velocity of {_particle} to vector(0, 1, 0)"), @Example("if the vector length of the player's velocity is greater than 5:\n    send \"You're moving fast!\" to the player\n")})
@Since({"2.2-dev31"})
public class ExprVelocity extends SimplePropertyExpression<Object, Vector> {
   @Nullable
   public Vector convert(Object object) {
      if (object instanceof Entity entity) {
         return entity.getVelocity();
      } else {
         return object instanceof DirectionalEffect particleEffect && particleEffect.hasVelocity() ? Vector.fromJOML(particleEffect.velocity()) : null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, RESET, DELETE -> (Class[])CollectionUtils.array(Vector.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      assert mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET || delta != null;

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Entity entity) {
            switch (mode) {
               case ADD:
                  entity.setVelocity(entity.getVelocity().add((Vector)delta[0]));
                  break;
               case REMOVE:
                  entity.setVelocity(entity.getVelocity().subtract((Vector)delta[0]));
                  break;
               case SET:
                  entity.setVelocity((Vector)delta[0]);
                  break;
               case RESET:
               case DELETE:
                  entity.setVelocity(new Vector());
               case REMOVE_ALL:
            }
         } else if (object instanceof DirectionalEffect particleEffect) {
            switch (mode) {
               case ADD:
                  if (particleEffect.hasVelocity()) {
                     particleEffect.velocity(particleEffect.velocity().add(((Vector)delta[0]).toVector3d()));
                  }
                  break;
               case REMOVE:
                  if (particleEffect.hasVelocity()) {
                     particleEffect.velocity(particleEffect.velocity().sub(((Vector)delta[0]).toVector3d()));
                  }
                  break;
               case SET:
                  particleEffect.velocity(((Vector)delta[0]).toVector3d());
                  break;
               case RESET:
               case DELETE:
                  if (particleEffect.hasVelocity()) {
                     particleEffect.velocity(new Vector3d());
                  }
               case REMOVE_ALL:
            }
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "velocity";
   }

   @Override
   public Class<Vector> getReturnType() {
      return Vector.class;
   }

   static {
      register(ExprVelocity.class, Vector.class, "velocit(y|ies)", "entities/directionalparticles");
   }
}
