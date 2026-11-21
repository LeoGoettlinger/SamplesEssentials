package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Sleeping")
@Description({"Checks whether an entity is sleeping."})
@Example.Examples({@Example("if player is sleeping:\n\tmake player wake up without spawn location update\n"), @Example("if last spawned fox is sleeping:\n\tmake last spawned fox stop sleeping\n")})
@Since({"1.4.4, 2.11 (living entities)"})
public class CondIsSleeping extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity.isSleeping();
   }

   @Override
   protected String getPropertyName() {
      return "sleeping";
   }

   static {
      register(CondIsSleeping.class, "sleeping", "livingentities");
   }
}
