package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

@Name("Is Tameable")
@Description({"Check if an entity is tameable."})
@Example("on damage:\n\tif victim is tameable:\n\t\tcancel event\n")
@Since({"2.5"})
public class CondIsTameable extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity instanceof Tameable;
   }

   @Override
   protected String getPropertyName() {
      return "tameable";
   }

   static {
      register(CondIsTameable.class, "tameable", "livingentities");
   }
}
