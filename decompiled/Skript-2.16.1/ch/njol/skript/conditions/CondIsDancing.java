package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Piglin;

@Name("Is Dancing")
@Description({"Checks to see if an entity is dancing, such as allays, parrots, or piglins."})
@Example("if last spawned allay is dancing:\n\tbroadcast \"Dance Party!\"\n")
@Since({"2.11"})
public class CondIsDancing extends PropertyCondition<LivingEntity> {
   private static final boolean SUPPORTS_PIGLINS = Skript.methodExists(Piglin.class, "isDancing");

   public boolean check(LivingEntity entity) {
      if (entity instanceof Allay allay) {
         return allay.isDancing();
      } else if (entity instanceof Parrot parrot) {
         return parrot.isDancing();
      } else {
         return SUPPORTS_PIGLINS && entity instanceof Piglin piglin ? piglin.isDancing() : false;
      }
   }

   @Override
   protected String getPropertyName() {
      return "dancing";
   }

   static {
      register(CondIsDancing.class, "dancing", "livingentities");
   }
}
