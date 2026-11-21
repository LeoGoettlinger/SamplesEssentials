package org.skriptlang.skript.bukkit.breeding.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is In Love")
@Description({"Checks whether or not a living entity is in love."})
@Example("on spawn of living entity:\n\tif entity is in love:\n\t\tbroadcast \"That was quick!\"\n")
@Since({"2.10"})
public class CondIsInLove extends PropertyCondition<LivingEntity> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         infoBuilder(CondIsInLove.class, PropertyCondition.PropertyType.BE, "in lov(e|ing) [state|mode]", "livingentities").supplier(CondIsInLove::new).build()
      );
   }

   public boolean check(LivingEntity entity) {
      return entity instanceof Animals animals ? animals.isLoveMode() : false;
   }

   @Override
   protected String getPropertyName() {
      return "in love";
   }
}
