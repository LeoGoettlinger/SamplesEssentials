package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;

@Name("Is Charging Fireball")
@Description({"Check whether a ghast is charging a fireball."})
@Example("if last spawned ghast is charging fireball:\n\tkill last spawned ghast\n")
@Since({"2.11"})
public class CondIsChargingFireball extends PropertyCondition<LivingEntity> {
   public boolean check(LivingEntity entity) {
      return entity instanceof Ghast ghast ? ghast.isCharging() : false;
   }

   @Override
   protected String getPropertyName() {
      return "charging fireball";
   }

   static {
      register(CondIsChargingFireball.class, "charging [a] fireball", "livingentities");
   }
}
