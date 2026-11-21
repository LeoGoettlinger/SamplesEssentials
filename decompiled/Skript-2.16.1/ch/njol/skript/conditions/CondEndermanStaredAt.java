package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;

@Name("Enderman Has Been Stared At")
@Description({"Checks to see if an enderman has been stared at.", "This will return true as long as the entity that stared at the enderman is still alive."})
@Example("if last spawned enderman has been stared at:")
@Since({"2.11"})
public class CondEndermanStaredAt extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity instanceof Enderman enderman ? enderman.hasBeenStaredAt() : false;
   }

   @Override
   protected String getPropertyName() {
      return "stared at";
   }

   static {
      if (Skript.methodExists(Enderman.class, "hasBeenStaredAt")) {
         register(CondEndermanStaredAt.class, PropertyCondition.PropertyType.HAVE, "been stared at", "livingentities");
      }
   }
}
