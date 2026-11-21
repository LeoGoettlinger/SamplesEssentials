package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Camel;
import org.bukkit.entity.LivingEntity;

@Name("Camel Is Dashing")
@Description({"Checks whether a camel is currently using its dash ability."})
@Example("if last spawned camel is dashing:\n\tkill last spawned camel\n")
@Since({"2.11"})
public class CondIsDashing extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity instanceof Camel camel ? camel.isDashing() : false;
   }

   @Override
   protected String getPropertyName() {
      return "dashing";
   }

   static {
      register(CondIsDashing.class, "dashing", "livingentities");
   }
}
