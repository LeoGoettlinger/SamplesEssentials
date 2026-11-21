package org.skriptlang.skript.bukkit.potion.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect - Has Particles")
@Description({"Checks whether a potion effect has particles."})
@Example("on entity potion effect modification:\n\tif the potion effect has particles:\n\t\t hide the particles of event-potioneffecttype for event-entity\n")
@Since({"2.14"})
public class CondPotionHasParticles extends PropertyCondition<SkriptPotionEffect> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         infoBuilder(CondPotionHasParticles.class, PropertyCondition.PropertyType.HAVE, "particles", "skriptpotioneffects")
            .supplier(CondPotionHasParticles::new)
            .build()
      );
   }

   public boolean check(SkriptPotionEffect potionEffect) {
      return potionEffect.particles();
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.HAVE;
   }

   @Override
   protected String getPropertyName() {
      return "particles";
   }
}
