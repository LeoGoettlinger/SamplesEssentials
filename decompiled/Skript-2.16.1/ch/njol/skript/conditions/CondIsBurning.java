package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Entity;

@Name("Is Burning")
@Description({"Checks whether an entity is on fire, e.g. a zombie due to being in sunlight, or any entity after falling into lava."})
@Example("# increased attack against burning targets\nvictim is burning:\n\tincrease damage by 2\n")
@Since({"1.4.4"})
public class CondIsBurning extends PropertyCondition<Entity> {
   public boolean check(Entity entity) {
      return entity.getFireTicks() > 0;
   }

   @Override
   protected String getPropertyName() {
      return "burning";
   }

   static {
      register(CondIsBurning.class, "(burning|ignited|on fire)", "entities");
   }
}
