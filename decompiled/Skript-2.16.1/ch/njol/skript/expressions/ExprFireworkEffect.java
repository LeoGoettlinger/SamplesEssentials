package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Firework Effect")
@Description({"Represents a 'firework effect' which can be used in the <a href='#EffFireworkLaunch'>launch firework</a> effect."})
@Example.Examples({@Example("launch flickering trailing burst firework colored blue and green at player"), @Example("launch trailing flickering star colored purple, yellow, blue, green and red fading to pink at target entity"), @Example("launch ball large colored red, purple and white fading to light green and black at player's location with duration 1")})
@Since({"2.4"})
public class ExprFireworkEffect extends SimpleExpression<FireworkEffect> {
   private Expression<Type> type;
   private Expression<Color> color;
   private Expression<Color> fade;
   private boolean flicker;
   private boolean trail;
   private boolean hasFade;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.flicker = parseResult.mark == 2 || parseResult.mark > 3;
      this.trail = parseResult.mark >= 3;
      this.hasFade = matchedPattern == 1;
      this.type = (Expression<Type>)exprs[0];
      this.color = (Expression<Color>)exprs[1];
      if (this.hasFade) {
         this.fade = (Expression<Color>)exprs[2];
      }

      return true;
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends FireworkEffect> getReturnType() {
      return FireworkEffect.class;
   }

   @Nullable
   protected FireworkEffect[] get(Event e) {
      Type type = this.type.getSingle(e);
      if (type == null) {
         return null;
      } else {
         Builder builder = FireworkEffect.builder().with(type);

         for (Color colour : this.color.getArray(e)) {
            if (colour instanceof ColorRGB) {
               builder.withColor(colour.asBukkitColor());
            } else {
               builder.withColor(colour.asDyeColor().getFireworkColor());
            }
         }

         if (this.hasFade) {
            for (Color colourx : this.fade.getArray(e)) {
               if (colourx instanceof ColorRGB) {
                  builder.withFade(colourx.asBukkitColor());
               } else {
                  builder.withFade(colourx.asDyeColor().getFireworkColor());
               }
            }
         }

         builder.flicker(this.flicker);
         builder.trail(this.trail);
         return CollectionUtils.array(builder.build());
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "Firework effect " + this.type.toString(e, debug) + " with color " + this.color.toString(e, debug);
   }

   static {
      Skript.registerExpression(
         ExprFireworkEffect.class,
         FireworkEffect.class,
         ExpressionType.COMBINED,
         "(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors%",
         "(1¦|2¦flickering|3¦trailing|4¦flickering trailing|5¦trailing flickering) %fireworktype% [firework [effect]] colo[u]red %colors% fad(e|ing) [to] %colors%"
      );
   }
}
