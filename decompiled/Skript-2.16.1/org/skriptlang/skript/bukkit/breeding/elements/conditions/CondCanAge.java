package org.skriptlang.skript.bukkit.breeding.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Can Age")
@Description({"Checks whether or not an entity will be able to age/grow up."})
@Example("on breeding:\n\tentity can't age\n\tbroadcast \"An immortal has been born!\" to player\n")
@Since({"2.10"})
public class CondCanAge extends PropertyCondition<LivingEntity> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         infoBuilder(CondCanAge.class, PropertyCondition.PropertyType.CAN, "(age|grow (up|old[er]))", "livingentities").supplier(CondCanAge::new).build()
      );
   }

   public boolean check(LivingEntity entity) {
      return entity instanceof Breedable breedable && !breedable.getAgeLock();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.CAN;
   }

   @Override
   protected String getPropertyName() {
      return "age";
   }
}
