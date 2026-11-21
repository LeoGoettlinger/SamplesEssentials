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

@Name("Particle Count")
@Description({"Sets how many particles to draw.\nParticle count has an influence on how the 'offset' and 'extra' values of a particle apply.\nOffsets are treated as distributions if particle count is greater than 0.\nOffsets are treated as velocity or some other special behavior if particle count is 0.\n\nThis means that setting the particle count may change how your particle behaves. Be careful!\n\nMore detailed information on particle behavior can be found at <a href=\"https://docs.papermc.io/paper/dev/particles/#count-argument-behavior\">Paper's particle documentation</a>.\n"})
@Example("draw 7 blue dust particles at player")
@Since({"2.14"})
public class ExprParticleCount extends SimplePropertyExpression<ParticleEffect, Number> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprParticleCount.class, Number.class, "particle count", "particles", false)
               .supplier(ExprParticleCount::new))
            .build()
      );
   }

   public Number convert(ParticleEffect from) {
      return from.count();
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
         int countDelta = delta == null ? 0 : ((Number)delta[0]).intValue();
         switch (mode) {
            case SET:
            case RESET:
               countDelta = Math.clamp((long)countDelta, 0, 1000);

               for (ParticleEffect effect : particleEffect) {
                  effect.count(countDelta);
               }
               break;
            case REMOVE:
               countDelta = -countDelta;
            case ADD:
               for (ParticleEffect effect : particleEffect) {
                  effect.count(Math.clamp((long)(effect.count() + countDelta), 0, 1000));
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
      return "particle count";
   }
}
