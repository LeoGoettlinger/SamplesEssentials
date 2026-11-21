package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Goat Horns")
@Description({"Make a goat have or not have a left, right, or both horns."})
@Example.Examples({@Example("remove the left horn of last spawned goat"), @Example("regrow {_goat}'s horns"), @Example("remove both horns of all goats")})
@Since({"2.11"})
public class EffGoatHorns extends Effect {
   private Expression<LivingEntity> entities;
   private EffGoatHorns.GoatHorn goatHorn = EffGoatHorns.GoatHorn.LEFT;
   private boolean remove;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (parseResult.hasTag("right")) {
         this.goatHorn = EffGoatHorns.GoatHorn.RIGHT;
      } else if (parseResult.hasTag("both")) {
         this.goatHorn = EffGoatHorns.GoatHorn.BOTH;
      }

      this.entities = (Expression<LivingEntity>)exprs[0];
      this.remove = matchedPattern <= 1;
      return true;
   }

   @Override
   protected void execute(Event event) {
      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Goat goat) {
            if (this.goatHorn != EffGoatHorns.GoatHorn.RIGHT) {
               goat.setLeftHorn(this.remove);
            }

            if (this.goatHorn != EffGoatHorns.GoatHorn.LEFT) {
               goat.setRightHorn(this.remove);
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.remove) {
         builder.append("remove");
      } else {
         builder.append("regrow");
      }
      builder.append(switch (this.goatHorn) {
         case LEFT -> "the left horn";
         case RIGHT -> "the right horn";
         case BOTH -> "both horns";
         case ANY -> "any horn";
      });
      builder.append("of", this.entities);
      return builder.toString();
   }

   static {
      Skript.registerEffect(
         EffGoatHorns.class,
         "remove [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
         "remove %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)",
         "(regrow|replace) [the] (left horn[s]|right:right horn[s]|both:both horns) of %livingentities%",
         "(regrow|replace) %livingentities%'[s] (left horn[s]|right:right horn[s]|both:horns)"
      );
   }

   public static enum GoatHorn {
      LEFT,
      RIGHT,
      BOTH,
      ANY;
   }
}
