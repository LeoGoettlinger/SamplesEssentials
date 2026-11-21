package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Is Swimming")
@Description({"Checks whether a living entity is swimming."})
@Example("player is swimming")
@Since({"2.3"})
public class CondIsSwimming extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity.isSwimming();
   }

   @Override
   protected String getPropertyName() {
      return "swimming";
   }

   static {
      register(CondIsSwimming.class, "swimming", "livingentities");
   }
}
