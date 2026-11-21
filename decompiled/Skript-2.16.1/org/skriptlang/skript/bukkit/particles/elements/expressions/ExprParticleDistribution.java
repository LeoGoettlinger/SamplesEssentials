package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Distribution")
@Description({"Determines the normal distribution that particles may be drawn within.\nThe distribution is defined by a vector of x, y, and z standard deviations.\n\nParticles will be randomly drawn based on these values, clustering towards the center. 68% of particles will be within 1 standard deviation, 95% within 2, and 99.7% within three.\nThe area the particles will spawn in can be roughly estimated as being within 2 times the standard deviation in each axis.\n\nFor example, a distribution of 1, 2, and 1 would spawn particles within roughly 2 blocks on the x and z axes, and within 4 blocks on the y axis.\n\nPlease note that distributions only take effect if the particle count is greater than 0!\nParticles with counts of 0 do not have distributions.\nIf the particle count is 0, the offset is treated differently depending on the particle.\n\nMore detailed information on particle behavior can be found at <a href=\"https://docs.papermc.io/paper/dev/particles/#count-argument-behavior\">Paper's particle documentation</a>.\n"})
@Example("set the particle distribution of {_my-particle} to vector(1, 2, 1)")
@Since({"2.14"})
public class ExprParticleDistribution extends SimplePropertyExpression<ParticleEffect, Vector> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprParticleDistribution.class, Vector.class, "particle distribution", "particles", false)
               .supplier(ExprParticleDistribution::new))
            .build()
      );
   }

   @Nullable
   public Vector convert(ParticleEffect from) {
      return from.isUsingNormalDistribution() ? Vector.fromJOML(from.distribution()) : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> new Class[]{Vector.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ParticleEffect[] particleEffect = this.getExpr().getArray(event);
      if (particleEffect.length != 0) {
         Vector3d vectorDelta = delta == null ? new Vector3d() : ((Vector)delta[0]).toVector3d();
         switch (mode) {
            case SET:
            case RESET:
               for (ParticleEffect effect : particleEffect) {
                  effect.distribution(vectorDelta);
               }
               break;
            case REMOVE:
               vectorDelta.mul(-1.0);
            case ADD:
               for (ParticleEffect effect : particleEffect) {
                  effect.distribution(vectorDelta.add(effect.offset()));
               }
         }
      }
   }

   @Override
   public Class<? extends Vector> getReturnType() {
      return Vector.class;
   }

   @Override
   protected String getPropertyName() {
      return "particle distribution";
   }
}
