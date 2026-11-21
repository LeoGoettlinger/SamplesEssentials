package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import io.papermc.paper.entity.Leashable;
import org.bukkit.entity.Entity;

@Name("Is Leashed")
@Description({"Checks to see if an entity is currently leashed."})
@Example("target entity is leashed")
@Since({"2.5, 2.16 (all leashable entities)"})
public class CondLeashed extends PropertyCondition<Entity> {
   public boolean check(Entity entity) {
      return entity instanceof Leashable leashable && leashable.isLeashed();
   }

   @Override
   protected String getPropertyName() {
      return "leashed";
   }

   static {
      register(CondLeashed.class, PropertyCondition.PropertyType.BE, "leashed", "entities");
   }
}
