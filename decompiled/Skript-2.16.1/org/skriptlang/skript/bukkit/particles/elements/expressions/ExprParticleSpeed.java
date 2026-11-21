package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Speed/Extra Value")
@Description({"Determines the specific 'speed' or 'extra' value of a particle.\nThis value is used in different ways depending on the particle, but in general it:\n* acts as the speed at which the particle moves if the particle count is greater than 0.\n* acts as a multiplier to the particle's offset if the particle count is 0.\n\nMore detailed information on particle behavior can be found at <a href=\"https://docs.papermc.io/paper/dev/particles/#count-argument-behavior\">Paper's particle documentation</a>.\n"})
@Example.Examples({@Example("set the extra value of {_my-flame-particle} to 2"), @Example("set the particle speed of {_my-flame-particle} to 0")})
@Since({"2.14"})
public class ExprParticleSpeed extends SimplePropertyExpression<ParticleEffect, Number> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprParticleSpeed.class, Number.class, "(particle speed [value]|extra value)", "particles", false)
               .supplier(ExprParticleSpeed::new))
            .build()
      );
   }

   @Nullable
   public Number convert(ParticleEffect from) {
      return from.extra();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ParticleEffect[] particleEffect = this.getExpr().getArray(event);
      if (particleEffect.length != 0) {
         double extraDelta = delta == null ? 0.0 : ((Number)delta[0]).doubleValue();
         switch (mode) {
            case SET:
            case RESET:
               for (ParticleEffect effect : particleEffect) {
                  effect.extra(extraDelta);
               }
               break;
            case REMOVE:
               extraDelta = -extraDelta;
            case ADD:
               for (ParticleEffect effect : particleEffect) {
                  effect.extra(effect.extra() + extraDelta);
               }
         }
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return "particle speed";
   }
}
