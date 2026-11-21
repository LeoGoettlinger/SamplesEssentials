package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;

@Name("Is Screaming")
@Description({"Check whether a goat or enderman is screaming."})
@Example.Examples({@Example("\tif last spawned goat is not screaming:\n\t\tmake last spawned goat scream\n"), @Example("\tif {_enderman} is screaming:\n\t\tforce {_enderman} to stop screaming\n")})
@Since({"2.11"})
public class CondIsScreaming extends PropertyCondition<LivingEntity> {
   private static final boolean SUPPORTS_ENDERMAN = Skript.methodExists(Enderman.class, "isScreaming");

   public boolean check(LivingEntity entity) {
      if (entity instanceof Goat goat) {
         return goat.isScreaming();
      } else {
         return SUPPORTS_ENDERMAN && entity instanceof Enderman enderman ? enderman.isScreaming() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "screaming";
   }

   static {
      register(CondIsScreaming.class, "screaming", "livingentities");
   }
}
