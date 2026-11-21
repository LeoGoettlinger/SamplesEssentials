package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffGoatHorns;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;

@Name("Goat Has Horns")
@Description({"Checks to see if a goat has or does not have a left, right, or both horns."})
@Example.Examples({@Example("\tif last spawned goat does not have both horns:\n\t\tmake last spawned goat have both horns\n"), @Example("\tif {_goat} has a right horn:\n\t\tforce {_goat} to not have a right horn\n")})
@Since({"2.11"})
public class CondGoatHasHorns extends PropertyCondition<LivingEntity> {
   private EffGoatHorns.GoatHorn goatHorn = EffGoatHorns.GoatHorn.ANY;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (parseResult.hasTag("left")) {
         this.goatHorn = EffGoatHorns.GoatHorn.LEFT;
      } else if (parseResult.hasTag("right")) {
         this.goatHorn = EffGoatHorns.GoatHorn.RIGHT;
      } else if (parseResult.hasTag("both")) {
         this.goatHorn = EffGoatHorns.GoatHorn.BOTH;
      }

      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   public boolean check(LivingEntity entity) {
      if (!(entity instanceof Goat goat)) {
         return false;
      } else {
         boolean leftHorn = goat.hasLeftHorn();
         boolean rightHorn = goat.hasRightHorn();

         return switch (this.goatHorn) {
            case ANY -> leftHorn || rightHorn;
            case BOTH -> leftHorn && rightHorn;
            case LEFT -> leftHorn;
            case RIGHT -> rightHorn;
         };
      }
   }

   @Override
   protected String getPropertyName() {
      return switch (this.goatHorn) {
         case ANY -> "a horn";
         case BOTH -> "both horns";
         case LEFT -> "left horn";
         case RIGHT -> "right horn";
      };
   }

   static {
      register(
         CondGoatHasHorns.class,
         PropertyCondition.PropertyType.HAVE,
         "((any|a) horn|left:[a] left horn[s]|right:[a] right horn[s]|both:both horns)",
         "livingentities"
      );
   }
}
