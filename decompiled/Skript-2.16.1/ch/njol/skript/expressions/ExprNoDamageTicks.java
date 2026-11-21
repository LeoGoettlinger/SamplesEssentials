package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("No Damage Ticks")
@Description({"The number of ticks that an entity is invulnerable to damage for."})
@Example("on damage:\n\tset victim's invulnerability ticks to 20 #Victim will not take damage for the next second\n")
@Since({"2.5, 2.11 (deprecated)"})
@Deprecated(
   since = "2.11.0",
   forRemoval = true
)
public class ExprNoDamageTicks extends SimplePropertyExpression<LivingEntity, Long> {
   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      ScriptWarning.printDeprecationWarning("This expression is deprecated. Please use 'invulnerability time' instead of 'invulnerability ticks'.");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   public Long convert(LivingEntity entity) {
      return (long)entity.getNoDamageTicks();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE, RESET, ADD, REMOVE -> (Class[])CollectionUtils.array(Number.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      int providedTicks = 0;
      if (delta != null && delta[0] instanceof Number number) {
         providedTicks = number.intValue();
      }

      for (LivingEntity entity : this.getExpr().getArray(event)) {
         switch (mode) {
            case SET:
            case DELETE:
            case RESET:
               entity.setNoDamageTicks(providedTicks);
               break;
            case ADD: {
               int current = entity.getNoDamageTicks();
               int value = Math2.fit(0, current + providedTicks, Integer.MAX_VALUE);
               entity.setNoDamageTicks(value);
               break;
            }
            case REMOVE: {
               int current = entity.getNoDamageTicks();
               int value = Math2.fit(0, current - providedTicks, Integer.MAX_VALUE);
               entity.setNoDamageTicks(value);
            }
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "no damage ticks";
   }

   @Override
   public Class<Long> getReturnType() {
      return Long.class;
   }

   static {
      registerDefault(ExprNoDamageTicks.class, Long.class, "(invulnerability|invincibility|no damage) tick[s]", "livingentities");
   }
}
