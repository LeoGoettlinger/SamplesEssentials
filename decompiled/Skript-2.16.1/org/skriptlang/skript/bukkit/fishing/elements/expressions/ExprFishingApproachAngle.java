package org.skriptlang.skript.bukkit.fishing.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fishing Approach Angle")
@Description({"Returns the angle at which the fish will approach the fishing hook, after the wait time.", "The angle is in degrees, with 0 being positive Z, 90 being negative X, 180 being negative Z, and 270 being positive X.", "By default, returns a value between 0 and 360 degrees."})
@Example("on fish approach:\n\tif any:\n\t\tmaximum fishing approach angle is bigger than 300.5 degrees\n\t\tmin fishing approach angle is smaller than 59.5 degrees\n\tthen:\n\t\tcancel event\n")
@Events({"Fishing"})
@Since({"2.10"})
public class ExprFishingApproachAngle extends SimpleExpression<Float> {
   private static final float DEFAULT_MINIMUM_DEGREES = 0.0F;
   private static final float DEFAULT_MAXIMUM_DEGREES = 360.0F;
   private boolean isMin;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprFishingApproachAngle.class, Float.class
                     )
                     .addPatterns(new String[]{"(min:min[imum]|max[imum]) fish[ing] approach[ing] angle"}))
                  .supplier(ExprFishingApproachAngle::new))
               .priority(EventValueExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!this.getParser().isCurrentEvent(PlayerFishEvent.class)) {
         Skript.error("The 'fishing approach angle' expression can only be used in a fishing event.");
         return false;
      } else {
         this.isMin = parseResult.hasTag("min");
         return true;
      }
   }

   @Nullable
   protected Float[] get(Event event) {
      if (event instanceof PlayerFishEvent fishEvent) {
         return this.isMin ? new Float[]{fishEvent.getHook().getMinLureAngle()} : new Float[]{fishEvent.getHook().getMaxLureAngle()};
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET, ADD, REMOVE -> new Class[]{Float.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof PlayerFishEvent fishEvent) {
         FishHook hook = fishEvent.getHook();
         float angle = mode == Changer.ChangeMode.RESET ? (this.isMin ? 0.0F : 360.0F) : (Float)delta[0];
         switch (mode) {
            case SET:
            case RESET:
               if (this.isMin) {
                  hook.setMinLureAngle(this.clamp(angle));
               } else {
                  hook.setMaxLureAngle(this.clamp(angle));
               }
               break;
            case ADD:
               if (this.isMin) {
                  hook.setMinLureAngle(this.clamp(hook.getMinLureAngle() + angle));
               } else {
                  hook.setMaxLureAngle(this.clamp(hook.getMaxLureAngle() + angle));
               }
               break;
            case REMOVE:
               if (this.isMin) {
                  hook.setMinLureAngle(this.clamp(hook.getMinLureAngle() - angle));
               } else {
                  hook.setMaxLureAngle(this.clamp(hook.getMaxLureAngle() - angle));
               }
         }
      }
   }

   private float clamp(float value) {
      return Math.min(Math.max(value, 0.0F), 360.0F);
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Float> getReturnType() {
      return Float.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return (this.isMin ? "minimum" : "maximum") + " fishing approach angle";
   }
}
