package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;

@Name("Is Invisible")
@Description({"Checks whether a living entity is invisible."})
@Example("target entity is invisible")
@Since({"2.7"})
public class CondIsInvisible extends PropertyCondition<LivingEntity> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends LivingEntity>)exprs[0]);
      this.setNegated(matchedPattern == 1 ^ parseResult.hasTag("visible"));
      return true;
   }

   public boolean check(LivingEntity livingEntity) {
      return livingEntity.isInvisible();
   }

   @Override
   protected String getPropertyName() {
      return "invisible";
   }

   static {
      register(CondIsInvisible.class, "(invisible|:visible)", "livingentities");
   }
}
