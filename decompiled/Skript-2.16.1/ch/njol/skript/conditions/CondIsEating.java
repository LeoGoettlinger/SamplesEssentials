package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;

@Name("Is Eating")
@Description({"Whether a panda or horse type (horse, camel, donkey, llama, mule) is eating."})
@Example("if last spawned panda is eating:\n\tforce last spawned panda to stop eating\n")
@Since({"2.11"})
public class CondIsEating extends PropertyCondition<LivingEntity> {
   private static final boolean SUPPORTS_HORSES = Skript.methodExists(AbstractHorse.class, "isEating");

   public boolean check(LivingEntity entity) {
      if (entity instanceof Panda panda) {
         return panda.isEating();
      } else {
         return SUPPORTS_HORSES && entity instanceof AbstractHorse horse ? horse.isEating() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "eating";
   }

   static {
      register(CondIsEating.class, "eating", "livingentities");
   }
}
